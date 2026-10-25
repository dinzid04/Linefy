package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.remote.AppUpdateChecker
import com.example.data.remote.ReleaseUpdateInfo
import com.example.ui.theme.PlusJakartaSans

private val DialogBg = Color(0xFF1E1E22)
private val DialogBorder = Color(0xFF34343C)
private val DividerLineColor = Color.White.copy(alpha = 0.13f)
private val LinkBlue = Color(0xFF4DA3FF)

private sealed interface ParsedMarkdownBlock {
    data class Heading(val level: Int, val text: String) : ParsedMarkdownBlock
    data class Bullet(val text: String) : ParsedMarkdownBlock
    data class Paragraph(val text: String) : ParsedMarkdownBlock
    data class ImageBlock(val imageUrl: String, val altText: String) : ParsedMarkdownBlock
    data object Divider : ParsedMarkdownBlock
}

@Composable
fun UpdateAvailableDialog(
    updateInfo: ReleaseUpdateInfo,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isDownloading by AppUpdateChecker.isDownloading.collectAsStateWithLifecycle()
    val downloadProgress by AppUpdateChecker.downloadProgress.collectAsStateWithLifecycle()
    val errorMessage by AppUpdateChecker.downloadError.collectAsStateWithLifecycle()

    val cleanVersion = remember(updateInfo.versionTag) {
        updateInfo.cleanVersionNumber
    }

    val parsedBlocks = remember(updateInfo.combinedMarkdown) {
        parseReadmeAndReleaseMarkdown(updateInfo.combinedMarkdown)
    }

    fun openUrl(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    // Dismissing on click outside or Back closes the dialog while background download continues seamlessly
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        val dialogShape = RoundedCornerShape(24.dp)
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .lyraGlassCard(shape = dialogShape, fallbackColor = DialogBg)
                .border(1.dp, DialogBorder, dialogShape)
                .testTag("software_update_dialog"),
            shape = dialogShape,
            color = DialogBg.copy(alpha = 0.94f),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 1. Centered Header Area (No green icons or green colors)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Software Update",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isDownloading) {
                        Text(
                            text = "Downloading Lyra - Fy $cleanVersion... ($downloadProgress%)",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (downloadProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.20f),
                            strokeCap = StrokeCap.Round,
                        )
                    } else {
                        Text(
                            text = "Lyra - Fy $cleanVersion is available to download.",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.5.sp,
                            color = Color.White.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                HorizontalDivider(color = DividerLineColor, thickness = 0.8.dp)

                // 2. Scrollable "What's new" Content Area (Banner + Bulleted Changelog + README.md)
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 380.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "What's new",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                    )

                    parsedBlocks.forEach { block ->
                        when (block) {
                            is ParsedMarkdownBlock.Heading -> {
                                val cleanedHeading = cleanInlineMarkdownMarkers(block.text)
                                if (!cleanedHeading.equals("What's new", ignoreCase = true)) {
                                    val fontSize = when (block.level) {
                                        1 -> 16.sp
                                        2 -> 15.sp
                                        else -> 14.sp
                                    }
                                    Text(
                                        text = cleanedHeading,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = fontSize,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                            }

                            is ParsedMarkdownBlock.Bullet -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text(
                                        text = "•",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.88f),
                                        modifier = Modifier.padding(end = 8.dp, top = 1.dp),
                                    )
                                    ClickableMarkdownText(
                                        rawText = block.text,
                                        onOpenUrl = ::openUrl,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            is ParsedMarkdownBlock.Paragraph -> {
                                ClickableMarkdownText(
                                    rawText = block.text,
                                    onOpenUrl = ::openUrl,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            is ParsedMarkdownBlock.ImageBlock -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF141418))
                                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp)),
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(block.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = block.altText.ifBlank { "Update Preview Image" },
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp)),
                                    )
                                }
                            }

                            ParsedMarkdownBlock.Divider -> {
                                HorizontalDivider(
                                    color = DividerLineColor,
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = Color(0xFFF87171),
                        )
                    }
                }

                // 3. Full-Width Stacked Footer Actions (iOS/macOS Alert Style)
                HorizontalDivider(color = DividerLineColor, thickness = 0.8.dp)

                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                AppUpdateChecker.cancelBackgroundDownload()
                            }
                            .testTag("update_dialog_cancel_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = Color.White,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                val apkUrl = updateInfo.apkDownloadUrl
                                if (!apkUrl.isNullOrBlank()) {
                                    AppUpdateChecker.startBackgroundDownloadAndInstall(
                                        context = context,
                                        downloadUrl = apkUrl,
                                    )
                                } else {
                                    openUrl(updateInfo.releaseHtmlUrl)
                                }
                            }
                            .testTag("update_dialog_confirm_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Download Now",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = Color.White,
                        )
                    }

                    HorizontalDivider(color = DividerLineColor, thickness = 0.8.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable(onClick = onDismiss)
                            .testTag("update_dialog_later_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Remind Me Later",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClickableMarkdownText(
    rawText: String,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotated = remember(rawText) { buildMarkdownAnnotatedString(rawText) }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.5.sp,
        color = Color.White.copy(alpha = 0.86f),
        onTextLayout = { layoutResult = it },
        modifier = modifier.pointerInput(annotated) {
            detectTapGestures { pos ->
                val layout = layoutResult ?: return@detectTapGestures
                val offset = layout.getOffsetForPosition(pos)
                annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()
                    ?.let { ann -> onOpenUrl(ann.item) }
            }
        },
    )
}

private fun parseReadmeAndReleaseMarkdown(markdown: String): List<ParsedMarkdownBlock> {
    val blocks = mutableListOf<ParsedMarkdownBlock>()
    val mdImageRegex = Regex("""!\[([^\]]*)\]\((https?://[^)\s]+)\)""")
    val htmlImgRegex = Regex("""<img[^>]+src=["'](https?://[^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
    val standaloneImageRegex = Regex(
        """^(https?://(?:url\.dinzid\.my\.id/\S+|\S+\.(?:png|jpg|jpeg|webp|gif)(?:\?\S*)?))$""",
        RegexOption.IGNORE_CASE,
    )

    val lines = markdown.lines()
    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue

        if (line == "---" || line == "***" || line == "___") {
            blocks.add(ParsedMarkdownBlock.Divider)
            continue
        }

        val htmlMatches = htmlImgRegex.findAll(line).toList()
        if (htmlMatches.isNotEmpty()) {
            htmlMatches.forEach { m ->
                blocks.add(ParsedMarkdownBlock.ImageBlock(imageUrl = m.groupValues[1], altText = "Image"))
            }
            val remainingText = line.replace(htmlImgRegex, "").replace(Regex("""<[^>]+>"""), "").trim()
            if (remainingText.isNotEmpty()) {
                blocks.add(ParsedMarkdownBlock.Paragraph(remainingText))
            }
            continue
        }

        val mdImgMatches = mdImageRegex.findAll(line).toList()
        if (mdImgMatches.isNotEmpty()) {
            mdImgMatches.forEach { m ->
                blocks.add(
                    ParsedMarkdownBlock.ImageBlock(
                        imageUrl = m.groupValues[2],
                        altText = m.groupValues[1],
                    ),
                )
            }
            val remainingText = line.replace(mdImageRegex, "").trim()
            if (remainingText.isNotEmpty()) {
                blocks.add(ParsedMarkdownBlock.Paragraph(remainingText))
            }
            continue
        }

        val standaloneImg = standaloneImageRegex.matchEntire(line)
        if (standaloneImg != null) {
            blocks.add(ParsedMarkdownBlock.ImageBlock(imageUrl = standaloneImg.groupValues[1], altText = "Preview"))
            continue
        }

        val strippedHtmlLine = line.replace(
            Regex("""</?(?:p|div|br|center|h[1-6]|span)[^>]*>""", RegexOption.IGNORE_CASE),
            "",
        ).trim()
        if (strippedHtmlLine.isEmpty()) continue

        when {
            strippedHtmlLine.startsWith("### ") -> {
                blocks.add(ParsedMarkdownBlock.Heading(3, strippedHtmlLine.removePrefix("### ").trim()))
            }
            strippedHtmlLine.startsWith("## ") -> {
                blocks.add(ParsedMarkdownBlock.Heading(2, strippedHtmlLine.removePrefix("## ").trim()))
            }
            strippedHtmlLine.startsWith("# ") -> {
                blocks.add(ParsedMarkdownBlock.Heading(1, strippedHtmlLine.removePrefix("# ").trim()))
            }
            strippedHtmlLine.startsWith("- ") || strippedHtmlLine.startsWith("* ") || strippedHtmlLine.startsWith("• ") -> {
                val bulletText = strippedHtmlLine.drop(2).trim()
                blocks.add(ParsedMarkdownBlock.Bullet(bulletText))
            }
            else -> {
                blocks.add(ParsedMarkdownBlock.Paragraph(strippedHtmlLine))
            }
        }
    }
    return blocks
}

private fun cleanInlineMarkdownMarkers(text: String): String =
    text.replace("**", "").replace("__", "").replace("`", "")

private fun buildMarkdownAnnotatedString(rawText: String): AnnotatedString {
    val tokenRegex = Regex("""\[([^\]]+)\]\((https?://[^)\s]+)\)|\*\*([^*]+)\*\*|(https?://\S+)""")
    return buildAnnotatedString {
        var cursor = 0
        for (match in tokenRegex.findAll(rawText)) {
            if (match.range.first > cursor) {
                append(rawText.substring(cursor, match.range.first))
            }
            val linkLabel = match.groupValues[1]
            val linkUrl = match.groupValues[2]
            val boldContent = match.groupValues[3]
            val bareUrl = match.groupValues[4]

            when {
                linkUrl.isNotEmpty() -> {
                    val start = length
                    append(linkLabel)
                    val end = length
                    addStyle(
                        style = SpanStyle(
                            color = LinkBlue,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline,
                        ),
                        start = start,
                        end = end,
                    )
                    addStringAnnotation(tag = "URL", annotation = linkUrl, start = start, end = end)
                }

                boldContent.isNotEmpty() -> {
                    val start = length
                    append(boldContent)
                    val end = length
                    addStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        ),
                        start = start,
                        end = end,
                    )
                }

                bareUrl.isNotEmpty() -> {
                    val cleanUrl = bareUrl.trimEnd('.', ',', ';', ')')
                    val start = length
                    append(cleanUrl)
                    val end = length
                    addStyle(
                        style = SpanStyle(
                            color = LinkBlue,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                        ),
                        start = start,
                        end = end,
                    )
                    addStringAnnotation(tag = "URL", annotation = cleanUrl, start = start, end = end)
                }
            }
            cursor = match.range.last + 1
        }
        if (cursor < rawText.length) {
            append(rawText.substring(cursor))
        }
    }
}
