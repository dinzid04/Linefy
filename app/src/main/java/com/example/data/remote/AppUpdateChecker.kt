package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ReleaseUpdateInfo(
    val versionTag: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val readmeMarkdown: String,
    val apkDownloadUrl: String?,
    val releaseHtmlUrl: String,
    val isNewerThanInstalled: Boolean,
) {
    val combinedMarkdown: String
        get() = buildString {
            if (releaseNotes.isNotBlank()) {
                append(releaseNotes.trim())
                if (readmeMarkdown.isNotBlank()) {
                    append("\n\n---\n\n")
                }
            }
            if (readmeMarkdown.isNotBlank()) {
                append(readmeMarkdown.trim())
            }
        }.ifBlank {
            "Pembaruan aplikasi Lyrafy tersedia di repositori resmi GitHub dinzid04/Linefy."
        }
}

object AppUpdateChecker {
    private const val REPO_OWNER = "dinzid04"
    private const val REPO_NAME = "Linefy"
    private const val LATEST_RELEASE_API = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
    private const val ALL_RELEASES_API = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases"
    private const val RAW_README_MAIN = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/main/README.md"
    private const val RAW_README_MASTER = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/master/README.md"
    const val GITHUB_REPO_WEB_URL = "https://github.com/$REPO_OWNER/$REPO_NAME"
    const val GITHUB_RELEASES_WEB_URL = "https://github.com/$REPO_OWNER/$REPO_NAME/releases"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _updateInfo = MutableStateFlow<ReleaseUpdateInfo?>(null)
    val updateInfo: StateFlow<ReleaseUpdateInfo?> = _updateInfo.asStateFlow()

    private val _hasNewerRelease = MutableStateFlow(false)
    val hasNewerRelease: StateFlow<Boolean> = _hasNewerRelease.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    fun openUpdateDialog() {
        if (_updateInfo.value == null) {
            checkForUpdates(manualTrigger = true)
        } else {
            _showUpdateDialog.value = true
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun checkForUpdates(manualTrigger: Boolean = false) {
        if (_isChecking.value) {
            if (manualTrigger) _showUpdateDialog.value = true
            return
        }
        scope.launch {
            _isChecking.value = true
            try {
                val info = fetchLatestUpdateInfo()
                _updateInfo.value = info
                _hasNewerRelease.value = info.isNewerThanInstalled
                if (manualTrigger) {
                    _showUpdateDialog.value = true
                }
            } catch (_: Exception) {
                if (manualTrigger && _updateInfo.value == null) {
                    _updateInfo.value = ReleaseUpdateInfo(
                        versionTag = "v${BuildConfig.VERSION_NAME}",
                        releaseTitle = "Lyrafy ${BuildConfig.VERSION_NAME}",
                        releaseNotes = "",
                        readmeMarkdown = "# Lyrafy (Lyra - Fy)\nMusic for a brighter you.\n\nRepository: [github.com/dinzid04/Linefy](https://github.com/dinzid04/Linefy.git)",
                        apkDownloadUrl = null,
                        releaseHtmlUrl = GITHUB_RELEASES_WEB_URL,
                        isNewerThanInstalled = false,
                    )
                    _showUpdateDialog.value = true
                }
            } finally {
                _isChecking.value = false
            }
        }
    }

    suspend fun fetchLatestUpdateInfo(): ReleaseUpdateInfo = withContext(Dispatchers.IO) {
        val releaseJson = fetchReleaseJsonObject()
        val readmeContent = fetchReadmeMarkdown()

        val currentVersion = BuildConfig.VERSION_NAME
        val rawTag = releaseJson?.optString("tag_name", "")?.trim().orEmpty()
        val releaseName = releaseJson?.optString("name", "")?.trim().orEmpty()
        val releaseBody = releaseJson?.optString("body", "")?.trim().orEmpty()
        val htmlUrl = releaseJson?.optString("html_url", "")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: GITHUB_RELEASES_WEB_URL

        var apkUrl: String? = null
        val assets = releaseJson?.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val assetName = asset.optString("name", "").lowercase()
                val downloadUrl = asset.optString("browser_download_url", "")
                if (downloadUrl.isNotBlank() && (assetName.endsWith(".apk") || apkUrl == null)) {
                    apkUrl = downloadUrl
                    if (assetName.endsWith(".apk")) break
                }
            }
        }

        val effectiveTag = when {
            rawTag.isNotBlank() -> rawTag
            else -> extractVersionFromReadme(readmeContent) ?: "v$currentVersion"
        }

        val isNewer = if (rawTag.isNotBlank()) {
            isRemoteVersionNewer(remoteTag = rawTag, currentVersion = currentVersion)
        } else {
            val readmeVer = extractVersionFromReadme(readmeContent)
            readmeVer != null && isRemoteVersionNewer(remoteTag = readmeVer, currentVersion = currentVersion)
        }

        ReleaseUpdateInfo(
            versionTag = if (effectiveTag.startsWith("v", ignoreCase = true)) effectiveTag else "v$effectiveTag",
            releaseTitle = releaseName.ifBlank { "Software Update" },
            releaseNotes = releaseBody,
            readmeMarkdown = readmeContent,
            apkDownloadUrl = apkUrl,
            releaseHtmlUrl = htmlUrl,
            isNewerThanInstalled = isNewer,
        )
    }

    private fun fetchReleaseJsonObject(): JSONObject? {
        runCatching {
            val latestText = httpGetText(LATEST_RELEASE_API, acceptJson = true)
            if (!latestText.isNullOrBlank()) {
                val obj = JSONObject(latestText)
                if (obj.has("tag_name")) return obj
            }
        }
        runCatching {
            val allText = httpGetText(ALL_RELEASES_API, acceptJson = true)
            if (!allText.isNullOrBlank()) {
                val arr = JSONArray(allText)
                if (arr.length() > 0) {
                    return arr.optJSONObject(0)
                }
            }
        }
        return null
    }

    private fun fetchReadmeMarkdown(): String {
        val mainReadme = runCatching { httpGetText(RAW_README_MAIN, acceptJson = false) }.getOrNull()
        if (!mainReadme.isNullOrBlank()) return mainReadme
        val masterReadme = runCatching { httpGetText(RAW_README_MASTER, acceptJson = false) }.getOrNull()
        return masterReadme.orEmpty()
    }

    private fun httpGetText(urlString: String, acceptJson: Boolean): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Lyrafy-Android/${BuildConfig.VERSION_NAME}")
                if (acceptJson) {
                    setRequestProperty("Accept", "application/vnd.github+json")
                }
            }
            val code = conn.responseCode
            if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun extractVersionFromReadme(readme: String): String? {
        if (readme.isBlank()) return null
        val regex = Regex("""\bv?(\d+\.\d+(?:\.\d+)*(?:-[A-Za-z0-9.]+)?)\b""")
        val lines = readme.lines().take(25)
        for (line in lines) {
            if (line.contains("version", ignoreCase = true) ||
                line.contains("release", ignoreCase = true) ||
                line.startsWith("#")
            ) {
                val match = regex.find(line)
                if (match != null) return match.groupValues[1]
            }
        }
        return null
    }

    fun isRemoteVersionNewer(remoteTag: String, currentVersion: String): Boolean {
        val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V").trim()
        val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V").trim()
        if (cleanRemote.isEmpty() || cleanRemote.equals(cleanCurrent, ignoreCase = true)) {
            return false
        }

        val remoteParts = cleanRemote.split(Regex("""[^0-9]+""")).filter { it.isNotEmpty() }.mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(Regex("""[^0-9]+""")).filter { it.isNotEmpty() }.mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return cleanRemote != cleanCurrent && !cleanCurrent.contains(cleanRemote)
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit,
    ) = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            withContext(Dispatchers.Main) { onProgress(0) }
            val url = URL(downloadUrl)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Lyrafy-Android/${BuildConfig.VERSION_NAME}")
            }
            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                withContext(Dispatchers.Main) {
                    onError("Gagal mengunduh APK (HTTP $responseCode)")
                }
                return@withContext
            }

            val totalBytes = conn.contentLengthLong
            val targetFile = File(context.cacheDir, "lyrafy_update_latest.apk")
            if (targetFile.exists()) {
                targetFile.delete()
            }

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloadedBytes = 0L
                    var lastReportedPct = -1
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val pct = if (totalBytes > 0L) {
                            ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            ((downloadedBytes / (1024 * 250)).toInt()).coerceIn(1, 95)
                        }
                        if (pct != lastReportedPct) {
                            lastReportedPct = pct
                            withContext(Dispatchers.Main) {
                                onProgress(pct)
                            }
                        }
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(100)
                onComplete()
                installDownloadedApk(context, targetFile)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "Gagal mengunduh pembaruan")
            }
        } finally {
            conn?.disconnect()
        }
    }

    fun installDownloadedApk(context: Context, apkFile: File) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }
            }
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile,
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        }.onFailure {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_WEB_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(webIntent) }
        }
    }
}
