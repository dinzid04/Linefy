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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.remote.AppUpdateChecker
import com.example.data.remote.ReleaseUpdateInfo
import com.example.ui.theme.PlusJakartaSans
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val DialogBg = Color(0xFF18181B)
private val DialogBorder = Color(0xFF2C2C32)
private val UpdateGreenTint = Color(0xFF34D399)
private val UpdateGreenCircleBg = Color(0xFF132A22)
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
    val coroutineScope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }

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

    Dialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, DialogBorder, RoundedCornerShape(28.dp))
                .testTag("software_update_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = DialogBg,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
            ) {
                // Header: Circular green/dark icon with Upward Arrow (↑), title "Software Update", and version tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(UpdateGreenCircleBg)
                            .border(1.dp, UpdateGreenTint.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Software Update",
                            tint = UpdateGreenTint,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Software Update",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = updateInfo.versionTag,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFFA1A1AA),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFF27272C), thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Markdown Content (Changelog + Live README.md)
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 380.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    parsedBlocks.forEach { block ->
                        when (block) {
                            is ParsedMarkdownBlock.Heading -> {
                                val fontSize = when (block.level) {
                                    1 -> 17.sp
                                    2 -> 15.5.sp
                                    else -> 14.5.sp
                                }
                                Text(
                                    text = cleanInlineMarkdownMarkers(block.text),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fontSize,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
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
                                        color = Color(0xFFD4D4D8),
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
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF222227))
                                        .border(1.dp, Color(0xFF2E2E36), RoundedCornerShape(14.dp)),
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
                                            .clip(RoundedCornerShape(14.dp)),
                                    )
                                }
                            }

                            ParsedMarkdownBlock.Divider -> {
                                HorizontalDivider(
                                    color = Color(0xFF2A2A30),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = Color(0xFFF87171),
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Area: Download Progress Bar OR "Later" + "Update" buttons
                if (isDownloading) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Downloading update...",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFFD4D4D8),
                            )
                            Text(
                                text = "$downloadProgress%",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (downloadProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(50)),
                            color = UpdateGreenTint,
                            trackColor = Color(0xFF27272C),
                            strokeCap = StrokeCap.Round,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    downloadJob?.cancel()
                                    isDownloading = false
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = Color(0xFFA1A1AA),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF232329))
                                .border(1.dp, Color(0xFF32323A), RoundedCornerShape(50))
                                .clickable(onClick = onDismiss)
                                .testTag("update_dialog_later_button"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Later",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp,
                                color = Color(0xFFE4E4E7),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .clickable {
                                    errorMessage = null
                                    val apkUrl = updateInfo.apkDownloadUrl
                                    if (!apkUrl.isNullOrBlank()) {
                                        isDownloading = true
                                        downloadProgress = 0
                                        downloadJob = coroutineScope.launch {
                                            AppUpdateChecker.downloadAndInstallApk(
                                                context = context,
                                                downloadUrl = apkUrl,
                                                onProgress = { pct -> downloadProgress = pct },
                                                onError = { err ->
                                                    isDownloading = false
                                                    errorMessage = err
                                                },
                                                onComplete = {
                                                    isDownloading = false
                                                    onDismiss()
                                                },
                                            )
                                        }
                                    } else {
                                        openUrl(updateInfo.releaseHtmlUrl)
                                    }
                                }
                                .testTag("update_dialog_confirm_button"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Update",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color(0xFF0E0E10),
                            )
                        }
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
        lineHeight = 20.sp,
        color = Color(0xFFD4D4D8),
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

        // Check for HTML <img> tags on the line
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

        // Check for Markdown ![alt](url) images on the line
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

        // Check for standalone image/CDN URL line
        val standaloneImg = standaloneImageRegex.matchEntire(line)
        if (standaloneImg != null) {
            blocks.add(ParsedMarkdownBlock.ImageBlock(imageUrl = standaloneImg.groupValues[1], altText = "Preview"))
            continue
        }

        // Strip simple HTML wrapper tags like <p align="center">, </p>, <br>, <div>
        val strippedHtmlLine = line.replace(Regex("""</?(?:p|div|br|center|h[1-6]|span)[^>]*>""", RegexOption.IGNORE_CASE), "").trim()
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
