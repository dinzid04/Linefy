package com.example.auth

/**
 * Pure parser for YouTube's `DATASYNC_ID` (from `ytcfg.get('DATASYNC_ID')`).
 *
 * Per NewPipeExtractor / SmartTube / OuterTune:
 *  - Brand Account: `"112451090335280957933||100643850966549257849"` — first
 *    part is the channel's `onBehalfOfUser` pageId, second is the Google owner.
 *  - Primary / personal account: `"109457468941272971283||"` — trailing `||`
 *    with empty second token means no delegation needed; send no `onBehalfOfUser`
 *    and use `raw.substringBefore("||")` as `context.user.dataSyncId`.
 *  - Single token (no `||`): already clean.
 */
object DataSyncId {

    data class Parsed(
        /** Clean token suitable for `context.user.dataSyncId` (no `||`). */
        val dataSyncId: String?,
        /** Brand-channel `onBehalfOfUser` ID, if the active identity is a Brand Account. */
        val pageId: String?
    )

    fun parse(raw: String?): Parsed {
        val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return Parsed(null, null)
        if (!trimmed.contains("||")) return Parsed(trimmed, null)
        val parts = trimmed.split("||")
        val first = parts.getOrNull(0)?.takeIf { it.isNotEmpty() }
        val second = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }
        return if (first != null && second != null) {
            Parsed(dataSyncId = first, pageId = first)
        } else {
            Parsed(dataSyncId = first ?: second, pageId = null)
        }
    }
}
