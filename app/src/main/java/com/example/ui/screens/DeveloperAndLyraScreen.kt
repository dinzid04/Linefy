package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.LyrafyGoldHarpIcon
import com.example.ui.components.lyraGlassCard
import com.example.ui.theme.PlusJakartaSans

private val DevScreenBg = Color(0xFF0D0D0D)
private val DevCardBg = Color(0xFF161618)
private val DevInnerCardBg = Color(0xFF1C1C20)
private val DevCardBorder = Color(0xFF26262A)
private val DevGoldAccent = Color(0xFFE2B963)
private val DevTextPrimary = Color(0xFFFFFFFF)
private val DevTextSecondary = Color(0xFFA0A0A5)
private val DevTextBody = Color(0xFFEAEAEA)

private const val DEV_BANNER_URL = "https://url.dinzid.my.id/OiHa2Ll"
private const val DEV_AVATAR_URL = "https://url.dinzid.my.id/HOjKIXC"
private const val DEV_GITHUB_REPO_URL = "https://github.com/dinzid04/Linefy"
private const val DEV_X_URL = "https://x.com/lyrafy"
private const val DEV_EMAIL_URI = "mailto:gadingkencana05@gmail.com"

private const val SVG_PATH_X_LOGO =
    "M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"

private const val SVG_PATH_GITHUB_OCTOCAT =
    "M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"

private const val SVG_PATH_MAIL_SOLID =
    "M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"

private const val SVG_PATH_GRADUATION_CAP =
    "M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"

private const val SVG_PATH_LOCATION_PIN =
    "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"

@Composable
fun DeveloperAndLyraScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    fun openExternalLink(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DevScreenBg)
            .testTag("developer_and_lyra_screen"),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusTop + 12.dp,
                bottom = navBottom + 120.dp,
            ),
        ) {
            // 1. Top Header (Back Button + "Developer" + Subtitle)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C1E))
                            .border(1.dp, DevCardBorder, CircleShape)
                            .clickable(onClick = onBack)
                            .testTag("developer_back_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DevTextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Developer",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = DevTextPrimary,
                        letterSpacing = (-0.4).sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Di balik LYRA, ada satu orang yang percaya bahwa musik bisa membuat hidup lebih bermakna.",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = DevTextSecondary,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 2. Cat Banner & Overlapping Left-Aligned Circular Profile Photo + Bio + 3 Official Social Icons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("developer_identity_card"),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(227.dp),
                    ) {
                        // Cat Banner with CenterEnd alignment so the cat on the right is clearly visible
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(DEV_BANNER_URL)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Developer Banner",
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterEnd,
                                modifier = Modifier.fillMaxSize(),
                            )

                            // 1.dp white/grey divider line at the bottom edge of the banner
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.25f)),
                            )
                        }

                        // Overlapping Circular Profile Picture (104.dp) with 2.5.dp solid White border on the left
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 123.dp)
                                .size(104.dp)
                                .clip(CircleShape)
                                .background(DevScreenBg)
                                .border(2.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(DEV_AVATAR_URL)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Dinz Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Left-aligned Identity Text & Social Row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = "Dinz",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DevTextPrimary,
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "@GadingKecana",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = DevTextSecondary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "\"Not every inspiration needs to be known. Some are simply meant to live on through what we create.\"",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = DevTextPrimary,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row of 3 Pure White Official Vector Icons (X, GitHub, Mail) - no box backgrounds
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PureVectorSocialIconButton(
                                svgPath = SVG_PATH_X_LOGO,
                                contentDescription = "X Profile",
                                tint = Color.White,
                                tag = "dev_social_x",
                                onClick = { openExternalLink(DEV_X_URL) },
                            )

                            PureVectorSocialIconButton(
                                svgPath = SVG_PATH_GITHUB_OCTOCAT,
                                contentDescription = "GitHub Repository",
                                tint = Color.White,
                                tag = "dev_social_github",
                                onClick = { openExternalLink(DEV_GITHUB_REPO_URL) },
                            )

                            PureVectorSocialIconButton(
                                svgPath = SVG_PATH_MAIL_SOLID,
                                contentDescription = "Send Email",
                                tint = Color.White,
                                tag = "dev_social_email",
                                onClick = { openExternalLink(DEV_EMAIL_URI) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // 3. Two Full-Width Pill Badges (Education & Location)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    InfoPillBadgeRow(
                        svgPath = SVG_PATH_GRADUATION_CAP,
                        text = "SMAN 1 Pabuaran",
                    )

                    InfoPillBadgeRow(
                        svgPath = SVG_PATH_LOCATION_PIN,
                        text = "Indonesia",
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // 4. "ABOUT LYRA MUSIC" Card (with 92.dp Golden Harp Logo without dark box + 46.sp "LYRA")
            item {
                val aboutShape = RoundedCornerShape(24.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .lyraGlassCard(shape = aboutShape, fallbackColor = DevCardBg)
                        .border(1.dp, DevCardBorder, aboutShape)
                        .padding(20.dp)
                        .testTag("about_lyra_music_card"),
                ) {
                    Text(
                        text = "ABOUT LYRA MUSIC",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        color = DevTextSecondary,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left: Gold & Cream Harp Logo from svg_3.txt (92.dp, no box background)
                        LyrafyGoldHarpIcon(
                            modifier = Modifier.size(92.dp),
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Huge 46.sp ExtraBold "LYRA" + 15.sp Bold Gold "Music for a brighter you"
                        Column {
                            Text(
                                text = "LYRA",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 46.sp,
                                lineHeight = 48.sp,
                                letterSpacing = 1.sp,
                                color = DevTextPrimary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Music for a brighter you",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DevGoldAccent,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "LYRA bukan sekadar nama sebuah aplikasi musik. Di baliknya, ada cerita sederhana tentang cahaya, kekaguman, dan seseorang yang menginspirasi tanpa pernah harus menjadi pusat perhatian.",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 20.5.sp,
                        color = DevTextBody,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5. Three Separate Meaning Cards (Stacked Cards, 10.dp spacing)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MeaningStackedCard(
                        title = "L — Luminous",
                        body = "Melambangkan cahaya, keindahan, dan harapan yang membuat sesuatu terasa lebih berarti.",
                    )

                    MeaningStackedCard(
                        title = "Y · RA (D)",
                        body = "Sebuah rangkaian inisial yang menyimpan cerita pribadi. Tidak semua maknanya perlu dijelaskan kepada dunia.",
                    )

                    MeaningStackedCard(
                        title = null,
                        body = "Bagi saya, LYRA adalah cara untuk menyimpan sebuah cerita dalam sesuatu yang nyata. Seperti musik, ada hal-hal yang tidak selalu bisa dijelaskan dengan kata-kata, tetapi tetap memiliki arti.",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. "Hubungi Developer" Card at the bottom
            item {
                val contactShape = RoundedCornerShape(20.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .lyraGlassCard(shape = contactShape, fallbackColor = DevCardBg)
                        .border(1.dp, DevCardBorder, contactShape)
                        .padding(18.dp),
                ) {
                    Text(
                        text = "Hubungi Developer",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DevTextPrimary,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DevInnerCardBg)
                            .border(1.dp, Color(0xFF28282D), RoundedCornerShape(12.dp))
                            .clickable { openExternalLink(DEV_GITHUB_REPO_URL) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("dev_contact_github_row"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SvgPathVectorIcon(
                            svgPath = SVG_PATH_GITHUB_OCTOCAT,
                            tint = DevGoldAccent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "github.com/dinzid04/Linefy",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = DevTextPrimary,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DevInnerCardBg)
                            .border(1.dp, Color(0xFF28282D), RoundedCornerShape(12.dp))
                            .clickable { openExternalLink(DEV_EMAIL_URI) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("dev_contact_email_row"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SvgPathVectorIcon(
                            svgPath = SVG_PATH_MAIL_SOLID,
                            tint = DevGoldAccent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "gadingkencana05@gmail.com",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = DevTextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PureVectorSocialIconButton(
    svgPath: String,
    contentDescription: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        SvgPathVectorIcon(
            svgPath = svgPath,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun InfoPillBadgeRow(
    svgPath: String,
    text: String,
) {
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .lyraGlassCard(shape = pillShape, fallbackColor = DevCardBg)
            .border(1.dp, DevCardBorder, pillShape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SvgPathVectorIcon(
            svgPath = svgPath,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DevTextPrimary,
        )
    }
}

@Composable
private fun MeaningStackedCard(
    title: String?,
    body: String,
) {
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lyraGlassCard(shape = cardShape, fallbackColor = DevCardBg)
            .border(1.dp, DevCardBorder, cardShape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DevGoldAccent,
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = body,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 13.8.sp,
            lineHeight = 20.sp,
            color = DevTextBody,
        )
    }
}

@Composable
private fun SvgPathVectorIcon(
    svgPath: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val parsedPath: Path = remember(svgPath) {
        PathParser().parsePathString(svgPath).toPath()
    }
    Canvas(modifier = modifier) {
        val scaleX = size.width / 24f
        val scaleY = size.height / 24f
        withTransform({
            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
        }) {
            drawPath(
                path = parsedPath,
                color = tint,
            )
        }
    }
}
