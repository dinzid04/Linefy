package com.example.recognition

/**
 * ShazamSignatureGenerator from Metrolist recognition engine.
 * Accumulates 16kHz mono 16-bit PCM frames and generates a Shazam signature Data URI.
 */
class ShazamSignatureGenerator {

    private val extractor = VibraSignature.PeakExtractor()

    fun feedPcm(samples16kHzMono: ShortArray) {
        if (samples16kHzMono.isNotEmpty()) {
            extractor.feed(samples16kHzMono)
        }
    }

    fun clear() {
        // Re-instantiating ShazamSignatureGenerator resets the buffer cleanly
    }

    fun createSignatureDataUri(): String {
        return VibraSignature.encodePeaksToDataUri(
            sampleCount = extractor.totalSamples(),
            bandPeaks = extractor.bandPeaks
        )
    }
}
