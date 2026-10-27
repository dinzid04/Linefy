package com.example.recognition

import kotlin.math.roundToInt

/**
 * Pure Kotlin AudioResampler from Metrolist recognition engine.
 * Converts arbitrary PCM 16-bit mono/stereo audio to 16000Hz mono 16-bit PCM required by Shazam signature generator.
 */
object AudioResampler {

    const val TARGET_SAMPLE_RATE = 16000

    fun resampleTo16kHzMono(
        inputSamples: ShortArray,
        inputSampleRate: Int,
        channelCount: Int = 1
    ): ShortArray {
        if (inputSamples.isEmpty()) return ShortArray(0)

        val monoSamples = if (channelCount <= 1) {
            inputSamples
        } else {
            val frames = inputSamples.size / channelCount
            ShortArray(frames) { frameIdx ->
                var sum = 0
                val base = frameIdx * channelCount
                for (ch in 0 until channelCount) {
                    sum += inputSamples[base + ch].toInt()
                }
                (sum / channelCount).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        if (inputSampleRate == TARGET_SAMPLE_RATE) {
            return monoSamples
        }

        val ratio = inputSampleRate.toDouble() / TARGET_SAMPLE_RATE.toDouble()
        val outputLength = (monoSamples.size / ratio).roundToInt().coerceAtLeast(1)
        val output = ShortArray(outputLength)

        for (i in 0 until outputLength) {
            val srcPos = i * ratio
            val index = srcPos.toInt()
            val frac = srcPos - index
            val s0 = monoSamples[index.coerceIn(0, monoSamples.lastIndex)].toDouble()
            val s1 = monoSamples[(index + 1).coerceIn(0, monoSamples.lastIndex)].toDouble()
            val interpolated = s0 + (s1 - s0) * frac
            output[i] = interpolated.roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }

        return output
    }
}
