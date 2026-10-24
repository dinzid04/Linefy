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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
private val DevGoldSoftBg = Color(0xFF231E14)
private val DevGoldBorder = Color(0xFF4B3D22)
private val DevTextPrimary = Color(0xFFF5F5F7)
private val DevTextSecondary = Color(0xFFA0A0A8)
private val DevTextMuted = Color(0xFF75757E)

private const val DEV_BANNER_URL = "https://url.dinzid.my.id/OiHa2Ll"
private const val DEV_AVATAR_URL = "https://url.dinzid.my.id/HOjKIXC"
private const val DEV_GITHUB_REPO_URL = "https://github.com/dinzid04/Linefy.git"
private const val DEV_GITHUB_PROFILE_URL = "https://github.com/dinzid04"
private const val DEV_X_URL = "https://x.com/lyrafy"
private const val DEV_EMAIL_URI = "mailto:gadingkencana05@gmail.com"

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
                top = statusTop + 14.dp,
                bottom = navBottom + 120.dp,
                start = 18.dp,
                end = 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Top Bar + Title & Subtitle
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1E))
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

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Developer",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = DevTextPrimary,
                        letterSpacing = (-0.5).sp,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Di balik LYRA, ada satu orang yang percaya bahwa musik bisa membuat hidup lebih bermakna.",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = DevTextSecondary,
                    )
                }
            }

            // Card 1: Identitas Developer (Banner + Foto Fokus Utama)
            item {
                val cardShape = RoundedCornerShape(22.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .lyraGlassCard(shape = cardShape, fallbackColor = DevCardBg)
                        .border(1.dp, DevCardBorder, cardShape)
                        .testTag("developer_identity_card"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Banner + Overlapping Circular Avatar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(186.dp),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(DEV_BANNER_URL)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Developer Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        )

                        // Bottom gradient fade on the banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0x660D0D0D),
                                            DevCardBg,
                                        ),
                                    ),
                                ),
                        )

                        // Developer circular avatar overlapping banner bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(DevCardBg)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            DevGoldAccent,
                                            Color.White.copy(alpha = 0.85f),
                                            DevGoldAccent,
                                        ),
                                    ),
                                    shape = CircleShape,
                                )
                                .padding(3.dp),
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

                    // Developer Name
                    Text(
                        text = "Dinz",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = DevTextPrimary,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Username
                    Text(
                        text = "@GadingKecana",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = DevTextSecondary,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(DevInnerCardBg)
                            .border(1.dp, Color(0xFF2E2E34), RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "Full Stack Developer",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = DevGoldAccent,
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bio Quote
                    Text(
                        text = "\"Not every inspiration needs to be known. Some are simply meant to live on through what we create.\"",
                        fontFamily = PlusJakartaSans,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFD0D0D8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row of 4 Social Buttons (42.dp rounded squares, minimum 48.dp touch target)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SocialSquareButton(
                            tag = "dev_social_github",
                            contentDescription = "GitHub Repository",
                            onClick = { openExternalLink(DEV_GITHUB_REPO_URL) },
                        ) {
                            GitHubVectorIcon(tint = DevTextPrimary, modifier = Modifier.size(19.dp))
                        }

                        SocialSquareButton(
                            tag = "dev_social_x",
                            contentDescription = "X Profile",
                            onClick = { openExternalLink(DEV_X_URL) },
                        ) {
                            XTwitterVectorIcon(tint = DevTextPrimary, modifier = Modifier.size(17.dp))
                        }

                        SocialSquareButton(
                            tag = "dev_social_email",
                            contentDescription = "Send Email",
                            onClick = { openExternalLink(DEV_EMAIL_URI) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = DevTextPrimary,
                                modifier = Modifier.size(19.dp),
                            )
                        }

                        SocialSquareButton(
                            tag = "dev_social_website",
                            contentDescription = "Developer Profile",
                            onClick = { openExternalLink(DEV_GITHUB_PROFILE_URL) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = DevTextPrimary,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Compact Info Box below social buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DevInnerCardBg)
                            .border(1.dp, Color(0xFF28282D), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CompactJourneyInfoRow(
                            icon = Icons.Outlined.School,
                            label = "Education",
                            value = "Grade 11 · SMAN 1 Pabuaran",
                        )
                        HorizontalDivider(color = Color(0xFF26262B), thickness = 0.7.dp)
                        CompactJourneyInfoRow(
                            icon = Icons.Outlined.LocationOn,
                            label = "Location",
                            value = "Indonesia",
                        )
                        HorizontalDivider(color = Color(0xFF26262B), thickness = 0.7.dp)
                        CompactJourneyInfoRow(
                            icon = Icons.Outlined.Code,
                            label = "Focus",
                            value = "Web, Mobile, Cloud, Music Tech",
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Card 2: "TENTANG LYRA MUSIC" & Filosofi Nama
            item {
                val cardShape = RoundedCornerShape(22.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .lyraGlassCard(shape = cardShape, fallbackColor = DevCardBg)
                        .border(1.dp, DevCardBorder, cardShape)
                        .padding(20.dp)
                        .testTag("about_lyra_music_card"),
                ) {
                    Text(
                        text = "TENTANG LYRA MUSIC",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = DevTextMuted,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Identity Row: 64.dp Gold Harp Logo Box + LYRA & Subtitle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF101012))
                                .border(1.dp, DevGoldBorder, RoundedCornerShape(16.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            LyrafyGoldHarpIcon(modifier = Modifier.fillMaxSize())
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "LYRA",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = DevTextPrimary,
                                letterSpacing = 0.8.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Music for a brighter you",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = DevGoldAccent,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nama LYRA diambil dari salah satu rasi bintang paling kuno di langit utara, tempat bersinarnya Vega — bintang paling terang kelima di malam hari. Dalam mitologi Yunani, Lyra adalah harpa milik Orpheus, musisi legendaris yang alunan nadanya mampu meluluhkan batu, menenangkan badai, dan menyentuh jiwa siapa pun yang mendengarnya. Dari kata 'Lyra' pula lahir kata 'Lyric' (lirik) — simbol abadi dari ekspresi perasaan manusia melalui musik.",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 20.5.sp,
                        color = Color(0xFFD2D2DA),
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Inner Box: ✦ Filosofi & Makna Nama
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF131210))
                            .border(1.dp, DevGoldBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                    ) {
                        Text(
                            text = "✦ Filosofi & Makna Nama",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DevGoldAccent,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Two side-by-side blocks
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF1A1814))
                                    .border(0.8.dp, Color(0xFF332B1B), RoundedCornerShape(13.dp))
                                    .padding(12.dp),
                            ) {
                                Text(
                                    text = "L — Luminous",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DevGoldAccent,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Cahaya penuntun di tengah gelap, seperti bintang Vega yang tak pernah redup.",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFFCBCBD2),
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF1A1814))
                                    .border(0.8.dp, Color(0xFF332B1B), RoundedCornerShape(13.dp))
                                    .padding(12.dp),
                            ) {
                                Text(
                                    text = "Y · RA (D)",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DevGoldAccent,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Harmoni, rasa, dan dedikasi — mengubah setiap detik keseharian menjadi simfoni yang hidup.",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFFCBCBD2),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Lebih dari sekadar rasi bintang di langit malam, nama LYRA juga menyimpan potongan cerita dan dedikasi personal dari sang pengembang. Ada inspirasi berharga yang sengaja dirangkai secara samar ke dalam setiap hurufnya — sebuah bentuk apresiasi yang tidak perlu diceritakan dengan suara keras, namun abadi di dalam karya.",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
                            color = DevTextSecondary,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "“Some stories are better kept in the things we create.”",
                            fontFamily = PlusJakartaSans,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = DevGoldAccent,
                        )
                    }
                }
            }

            // Card 3: Tiga Pilar Filosofi (3 Baris Kartu dengan Ikon Emas)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PhilosophyPillarCard(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Keabadian (The Constellation)",
                        description = "Seperti rasi bintang yang selalu ada di langit malam, LYRA dirancang menjadi ruang musik yang setia menemani setiap fase perjalanan hidupmu.",
                    )
                    PhilosophyPillarCard(
                        icon = Icons.Outlined.GraphicEq,
                        title = "Kemurnian Suara (Pristine Audio)",
                        description = "Terinspirasi dari dawai harpa Orpheus, setiap detail antarmuka dan kualitas pemutaran dibuat jernih, tanpa gangguan, dan fokus sepenuhnya pada emosi lagu.",
                    )
                    PhilosophyPillarCard(
                        icon = Icons.Outlined.StarOutline,
                        title = "Penemuan Berharga (Hidden Gems)",
                        description = "Menghubungkanmu kembali dengan lagu-lagu yang mungkin terlupakan, sekaligus membuka cakrawala baru menuju melodi yang belum pernah kamu temui.",
                    )
                }
            }

            // Card 4: Keterangan Keahlian & Kontak Developer
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Badge Keahlian
                    val skillShape = RoundedCornerShape(18.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .lyraGlassCard(shape = skillShape, fallbackColor = DevCardBg)
                            .border(1.dp, DevCardBorder, skillShape)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DevGoldSoftBg)
                                .border(1.dp, DevGoldBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "</>",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DevGoldAccent,
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Full Stack Developer",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DevTextPrimary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Frontend • Backend • Mobile • DevOps",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = DevTextSecondary,
                            )
                        }
                    }

                    // Kartu Kontak ("Hubungi Developer")
                    val contactShape = RoundedCornerShape(18.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            GitHubVectorIcon(
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
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Email",
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
}

@Composable
private fun SocialSquareButton(
    tag: String,
    contentDescription: String,
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DevInnerCardBg)
                .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            iconContent()
        }
    }
}

@Composable
private fun CompactJourneyInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = DevGoldAccent,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = DevTextSecondary,
            modifier = Modifier.width(74.dp),
        )
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            color = DevTextPrimary,
        )
    }
}

@Composable
private fun PhilosophyPillarCard(
    icon: ImageVector,
    title: String,
    description: String,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .lyraGlassCard(shape = shape, fallbackColor = DevCardBg)
            .border(1.dp, DevCardBorder, shape)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DevGoldSoftBg)
                .border(1.dp, DevGoldBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = DevGoldAccent,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = DevTextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.8.sp,
                lineHeight = 18.5.sp,
                color = DevTextSecondary,
            )
        }
    }
}

@Composable
private fun GitHubVectorIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Clean recognizable GitHub mark + code bracket glyph
        drawCircle(
            color = tint,
            radius = minOf(w, h) * 0.42f,
            center = Offset(w * 0.5f, h * 0.46f),
            style = Stroke(width = w * 0.11f),
        )
        val earPath = Path().apply {
            moveTo(w * 0.24f, h * 0.26f)
            lineTo(w * 0.18f, h * 0.10f)
            lineTo(w * 0.36f, h * 0.18f)
            moveTo(w * 0.76f, h * 0.26f)
            lineTo(w * 0.82f, h * 0.10f)
            lineTo(w * 0.64f, h * 0.18f)
            moveTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.50f, h * 0.92f)
        }
        drawPath(
            path = earPath,
            color = tint,
            style = Stroke(
                width = w * 0.10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun XTwitterVectorIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(
            color = tint,
            start = Offset(w * 0.16f, h * 0.16f),
            end = Offset(w * 0.84f, h * 0.84f),
            strokeWidth = w * 0.13f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.84f, h * 0.16f),
            end = Offset(w * 0.16f, h * 0.84f),
            strokeWidth = w * 0.13f,
            cap = StrokeCap.Round,
        )
    }
}
