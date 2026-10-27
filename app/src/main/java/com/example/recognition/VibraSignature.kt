package com.example.recognition

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure-Kotlin Shazam DATA URI Signature encoder (Vibra / Shazam v2 signature format)
 * ported from Metrolist recognition engine.
 * Generates `data:audio/vnd.shazam.sig;base64,...` from 16kHz 16-bit mono PCM samples.
 */
object VibraSignature {

    private const val SAMPLE_RATE_ID_16000 = 3 // 16000 Hz in Shazam signature header
    private const val SIGNATURE_MAGIC_1 = 0xCAFE2580.toInt()
    private const val SIGNATURE_MAGIC_2 = 0x94119C00.toInt()
    private const val FFT_SIZE = 2048
    private const val STEP_SIZE = 128 // 16000 / 128 = 125 frames/sec

    enum class FrequencyBand(val index: Int, val minBin: Int, val maxBin: Int) {
        BAND_250_520(0, 32, 66),
        BAND_520_1450(1, 66, 185),
        BAND_1450_3500(2, 185, 448),
        BAND_3500_5500(3, 448, 704)
    }

    internal data class FrequencyPeak(
        val fftPassNumber: Int,
        val peakMagnitude: Int,
        val correctedPeakFrequencyBin: Int
    )

    private val hanningWindow = DoubleArray(FFT_SIZE) { i ->
        0.5 * (1.0 - cos(2.0 * PI * i / (FFT_SIZE - 1)))
    }

    fun fromI16(samples16kHzMono: ShortArray): String {
        val generator = ShazamSignatureGenerator()
        generator.feedPcm(samples16kHzMono)
        return generator.createSignatureDataUri()
    }

    internal fun encodePeaksToDataUri(
        sampleCount: Int,
        bandPeaks: Map<FrequencyBand, List<FrequencyPeak>>
    ): String {
        val contentsByBand = LinkedHashMap<FrequencyBand, ByteArray>()
        for (band in FrequencyBand.entries) {
            val peaks = bandPeaks[band].orEmpty()
            if (peaks.isEmpty()) continue
            val buf = ArrayList<Byte>(peaks.size * 6)
            var previousPass = 0
            for (peak in peaks) {
                val passDelta = peak.fftPassNumber - previousPass
                if (passDelta >= 255) {
                    buf.add(0xFF.toByte())
                    val b0 = (peak.fftPassNumber and 0xFF).toByte()
                    val b1 = ((peak.fftPassNumber ushr 8) and 0xFF).toByte()
                    val b2 = ((peak.fftPassNumber ushr 16) and 0xFF).toByte()
                    val b3 = ((peak.fftPassNumber ushr 24) and 0xFF).toByte()
                    buf.add(b0)
                    buf.add(b1)
                    buf.add(b2)
                    buf.add(b3)
                } else {
                    buf.add(passDelta.toByte())
                }
                val magLo = (peak.peakMagnitude and 0xFF).toByte()
                val magHi = ((peak.peakMagnitude ushr 8) and 0xFF).toByte()
                val binLo = (peak.correctedPeakFrequencyBin and 0xFF).toByte()
                val binHi = ((peak.correctedPeakFrequencyBin ushr 8) and 0xFF).toByte()
                buf.add(magLo)
                buf.add(magHi)
                buf.add(binLo)
                buf.add(binHi)
                previousPass = peak.fftPassNumber
            }
            contentsByBand[band] = buf.toByteArray()
        }

        var payloadSize = 0
        for ((_, bytes) in contentsByBand) {
            val paddedLen = (bytes.size + 3) and -4
            payloadSize += 8 + paddedLen
        }

        val totalSize = 48 + 8 + payloadSize
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        // Header (48 bytes)
        buffer.putInt(SIGNATURE_MAGIC_1)
        buffer.putInt(0) // crc32 placeholder at offset 4
        buffer.putInt(totalSize - 48) // size_minus_header at offset 8
        buffer.putInt(SIGNATURE_MAGIC_2)
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(SAMPLE_RATE_ID_16000 shl 27)
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt((sampleCount + (16000 * 0.24).toInt()))
        buffer.putInt((0x007C0000) or (SAMPLE_RATE_ID_16000 shl 27))

        // Top-level TLV header (0x40000000)
        buffer.putInt(0x40000000)
        buffer.putInt(payloadSize + 8)

        // Band TLVs
        for ((band, bytes) in contentsByBand) {
            val paddedLen = (bytes.size + 3) and -4
            buffer.putInt(0x60030040 + band.index)
            buffer.putInt(bytes.size)
            buffer.put(bytes)
            for (pad in 0 until (paddedLen - bytes.size)) {
                buffer.put(0.toByte())
            }
        }

        val fullBytes = buffer.array()
        val crc = CRC32()
        crc.update(fullBytes, 8, fullBytes.size - 8)
        val crcValue = crc.value.toInt()
        ByteBuffer.wrap(fullBytes).order(ByteOrder.LITTLE_ENDIAN).putInt(4, crcValue)

        val b64 = Base64.encodeToString(fullBytes, Base64.NO_WRAP)
        return "data:audio/vnd.shazam.sig;base64,$b64"
    }

    internal class PeakExtractor {
        private val ringBuffer = ShortArray(FFT_SIZE)
        private var ringWritePos = 0
        private var totalSamplesWritten = 0
        private var fftPassCount = 0

        // Keep rolling history of log magnitudes for local peak detection (128 passes x 1025 bins)
        private val historyDepth = 16
        private val logSpectrumHistory = Array(historyDepth) { DoubleArray(FFT_SIZE / 2 + 1) }

        val bandPeaks = LinkedHashMap<FrequencyBand, MutableList<FrequencyPeak>>().apply {
            for (b in FrequencyBand.entries) put(b, ArrayList())
        }

        fun totalSamples(): Int = totalSamplesWritten

        fun feed(samples: ShortArray) {
            for (s in samples) {
                ringBuffer[ringWritePos] = s
                ringWritePos = (ringWritePos + 1) % FFT_SIZE
                totalSamplesWritten++
                if (totalSamplesWritten >= FFT_SIZE && (totalSamplesWritten % STEP_SIZE == 0)) {
                    processFftFrame()
                }
            }
        }

        private fun processFftFrame() {
            val real = DoubleArray(FFT_SIZE)
            val imag = DoubleArray(FFT_SIZE)
            for (i in 0 until FFT_SIZE) {
                val sampleIdx = (ringWritePos + i) % FFT_SIZE
                val normalized = ringBuffer[sampleIdx] / 32768.0
                real[i] = normalized * hanningWindow[i]
                imag[i] = 0.0
            }

            fftInPlace(real, imag)

            val currSlot = fftPassCount % historyDepth
            val currMagnitudes = logSpectrumHistory[currSlot]
            for (bin in 0..(FFT_SIZE / 2)) {
                val r = real[bin]
                val im = imag[bin]
                val energy = (r * r + im * im) / (1 shl 17)
                val clamped = max(energy, 0.0000000001)
                currMagnitudes[bin] = 1024.0 * max(ln(clamped) / ln(2.0) + 64.0, 0.0)
            }

            if (fftPassCount >= 6) {
                val targetPass = fftPassCount - 3
                val targetSlot = targetPass % historyDepth
                val spectrum = logSpectrumHistory[targetSlot]

                for (band in FrequencyBand.entries) {
                    var bestBin = -1
                    var bestVal = 0.0
                    for (bin in band.minBin.coerceAtLeast(2)..band.maxBin.coerceAtMost(FFT_SIZE / 2 - 2)) {
                        val v = spectrum[bin]
                        if (v <= 4600.0) continue
                        if (v > spectrum[bin - 1] && v >= spectrum[bin + 1] && v > bestVal) {
                            // Verify time-axis local maximum across +/- 3 frames
                            var isTemporalPeak = true
                            for (dt in -3..3) {
                                if (dt == 0) continue
                                val neighborSlot = (targetPass + dt + historyDepth) % historyDepth
                                if (logSpectrumHistory[neighborSlot][bin] >= v) {
                                    isTemporalPeak = false
                                    break
                                }
                            }
                            if (isTemporalPeak) {
                                bestVal = v
                                bestBin = bin
                            }
                        }
                    }

                    if (bestBin != -1) {
                        val prevVal = spectrum[bestBin - 1]
                        val nextVal = spectrum[bestBin + 1]
                        val denom = prevVal - (2.0 * bestVal) + nextVal
                        val shift = if (denom != 0.0) 0.5 * (prevVal - nextVal) / denom else 0.0
                        val correctedBin = ((bestBin + shift) * 64.0).toInt().coerceIn(0, 65535)
                        val magnitudeInt = bestVal.toInt().coerceIn(0, 65535)
                        bandPeaks[band]?.add(
                            FrequencyPeak(
                                fftPassNumber = targetPass,
                                peakMagnitude = magnitudeInt,
                                correctedPeakFrequencyBin = correctedBin
                            )
                        )
                    }
                }
            }

            fftPassCount++
        }

        private fun fftInPlace(real: DoubleArray, imag: DoubleArray) {
            val n = real.size
            var j = 0
            for (i in 1 until n) {
                var bit = n shr 1
                while (j and bit != 0) {
                    j = j xor bit
                    bit = bit shr 1
                }
                j = j xor bit
                if (i < j) {
                    val tr = real[i]
                    real[i] = real[j]
                    real[j] = tr
                    val ti = imag[i]
                    imag[i] = imag[j]
                    imag[j] = ti
                }
            }
            var len = 2
            while (len <= n) {
                val halfLen = len shr 1
                val angle = -2.0 * PI / len
                val wlenReal = cos(angle)
                val wlenImag = sin(angle)
                var i = 0
                while (i < n) {
                    var wReal = 1.0
                    var wImag = 0.0
                    for (k in 0 until halfLen) {
                        val uReal = real[i + k]
                        val uImag = imag[i + k]
                        val vReal = real[i + k + halfLen] * wReal - imag[i + k + halfLen] * wImag
                        val vImag = real[i + k + halfLen] * wImag + imag[i + k + halfLen] * wReal
                        real[i + k] = uReal + vReal
                        imag[i + k] = uImag + vImag
                        real[i + k + halfLen] = uReal - vReal
                        imag[i + k + halfLen] = uImag - vImag
                        val nextWReal = wReal * wlenReal - wImag * wlenImag
                        wImag = wReal * wlenImag + wImag * wlenReal
                        wReal = nextWReal
                    }
                    i += len
                }
                len = len shl 1
            }
        }
    }
}
