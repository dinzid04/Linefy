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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
    val cleanVersionNumber: String
        get() = AppUpdateChecker.extractCleanVersionString(versionTag).ifBlank {
            versionTag.removePrefix("v").removePrefix("V")
        }

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
            "Pembaruan aplikasi Lyra - Fy tersedia di repositori resmi GitHub dinzid04/Linefy."
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

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    private var activeDownloadJob: Job? = null

    fun openUpdateDialog() {
        val currentInfo = _updateInfo.value
        if (currentInfo == null) {
            checkForUpdates(manualTrigger = true)
        } else if (currentInfo.isNewerThanInstalled) {
            _showUpdateDialog.value = true
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun checkForUpdates(manualTrigger: Boolean = false) {
        if (_isChecking.value) {
            if (manualTrigger && _updateInfo.value?.isNewerThanInstalled == true) {
                _showUpdateDialog.value = true
            }
            return
        }
        scope.launch {
            _isChecking.value = true
            try {
                val info = fetchLatestUpdateInfo()
                _updateInfo.value = info
                _hasNewerRelease.value = info.isNewerThanInstalled
                if (manualTrigger && info.isNewerThanInstalled) {
                    _showUpdateDialog.value = true
                } else if (!info.isNewerThanInstalled) {
                    _showUpdateDialog.value = false
                }
            } catch (_: Exception) {
                _hasNewerRelease.value = false
                _showUpdateDialog.value = false
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
        val publishedAtIso = releaseJson?.optString("published_at", "")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: releaseJson?.optString("created_at", "")?.trim().orEmpty()
        val htmlUrl = releaseJson?.optString("html_url", "")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: GITHUB_RELEASES_WEB_URL

        var releaseApkUrl: String? = null
        var fallbackApkUrl: String? = null
        val assets = releaseJson?.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val assetName = asset.optString("name", "").lowercase()
                val downloadUrl = asset.optString("browser_download_url", "")
                if (downloadUrl.isNotBlank() && assetName.endsWith(".apk")) {
                    if (!assetName.contains("debug")) {
                        releaseApkUrl = downloadUrl
                        break
                    } else if (fallbackApkUrl == null) {
                        fallbackApkUrl = downloadUrl
                    }
                }
            }
        }
        val apkUrl = releaseApkUrl ?: fallbackApkUrl

        val effectiveTag = when {
            rawTag.isNotBlank() -> rawTag
            else -> "v$currentVersion"
        }

        val versionIsStrictlyGreater = if (rawTag.isNotBlank()) {
            isRemoteVersionNewer(remoteTag = rawTag, currentVersion = currentVersion)
        } else {
            false
        }

        // Also verify whether the GitHub release was published AFTER this APK was compiled
        // (with a 5-minute grace window so the same CI run's release step never triggers self-update)
        val publishedAtMs = parseIso8601ToMillis(publishedAtIso)
        val isPublishedAfterThisBuild = if (publishedAtMs > 0L) {
            publishedAtMs > (BuildConfig.BUILD_TIME_MS + 5 * 60 * 1000L)
        } else {
            true
        }

        val isNewer = versionIsStrictlyGreater && isPublishedAfterThisBuild

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

    private fun parseIso8601ToMillis(iso: String): Long {
        if (iso.isBlank()) return 0L
        return runCatching {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(iso)?.time ?: 0L
        }.getOrDefault(0L)
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

    fun extractCleanVersionString(rawVersion: String): String {
        val regex = Regex("""(\d+(?:\.\d+)+|\d+)""")
        return regex.find(rawVersion.trim())?.value.orEmpty()
    }

    fun isRemoteVersionNewer(remoteTag: String, currentVersion: String): Boolean {
        val cleanRemote = extractCleanVersionString(remoteTag)
        val cleanCurrent = extractCleanVersionString(currentVersion)
        if (cleanRemote.isEmpty() || cleanCurrent.isEmpty()) {
            return false
        }
        if (cleanRemote == cleanCurrent) {
            return false
        }

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        if (remoteParts.isEmpty() || currentParts.isEmpty()) {
            return false
        }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    fun startBackgroundDownloadAndInstall(
        context: Context,
        downloadUrl: String,
    ) {
        if (_isDownloading.value) return
        val appContext = context.applicationContext
        _downloadError.value = null
        _isDownloading.value = true
        _downloadProgress.value = 0

        activeDownloadJob = scope.launch {
            downloadAndInstallApk(
                context = appContext,
                downloadUrl = downloadUrl,
                onProgress = { pct ->
                    _downloadProgress.value = pct
                },
                onError = { err ->
                    _isDownloading.value = false
                    _downloadError.value = err
                },
                onComplete = {
                    _isDownloading.value = false
                    _downloadProgress.value = 100
                    _showUpdateDialog.value = false
                },
            )
        }
    }

    fun cancelBackgroundDownload() {
        activeDownloadJob?.cancel()
        activeDownloadJob = null
        _isDownloading.value = false
        _downloadProgress.value = 0
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit,
    ) = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        val targetFile = File(context.cacheDir, "lyrafy_update_latest.apk")
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
            if (targetFile.exists()) {
                targetFile.delete()
            }

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloadedBytes = 0L
                    var lastReportedPct = -1
                    var bytesRead = 0
                    while (isActive && input.read(buffer).also { bytesRead = it } != -1) {
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

            if (!isActive) {
                if (targetFile.exists()) targetFile.delete()
                return@withContext
            }

            if (!targetFile.exists() || targetFile.length() < 100_000L) {
                if (targetFile.exists()) targetFile.delete()
                withContext(Dispatchers.Main) {
                    onError("File APK tidak lengkap atau rusak. Silakan coba unduh ulang.")
                }
                return@withContext
            }

            val archiveCheckError = verifyApkArchiveCompatibility(context, targetFile)
            if (archiveCheckError != null) {
                withContext(Dispatchers.Main) {
                    onError(archiveCheckError)
                }
                return@withContext
            }

            withContext(Dispatchers.Main) {
                onProgress(100)
                onComplete()
                installDownloadedApk(context, targetFile)
            }
        } catch (e: CancellationException) {
            if (targetFile.exists()) targetFile.delete()
            throw e
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "Gagal mengunduh pembaruan")
            }
        } finally {
            conn?.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun verifyApkArchiveCompatibility(context: Context, apkFile: File): String? {
        return runCatching {
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                android.content.pm.PackageManager.GET_SIGNATURES
            }
            val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
                ?: return@runCatching "File APK tidak valid atau korup saat diunduh."

            if (archiveInfo.packageName != context.packageName) {
                return@runCatching "Package ID APK (${archiveInfo.packageName}) berbeda dengan aplikasi terpasang (${context.packageName})."
            }

            val installedInfo = pm.getPackageInfo(context.packageName, flags)
            val archiveCerts: Set<ByteArray> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = archiveInfo.signingInfo
                val sigs = if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
                } else null
                sigs?.map { it.toByteArray() }?.toSet().orEmpty()
            } else {
                archiveInfo.signatures?.map { it.toByteArray() }?.toSet().orEmpty()
            }

            val installedCerts: Set<ByteArray> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = installedInfo.signingInfo
                val sigs = if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
                } else null
                sigs?.map { it.toByteArray() }?.toSet().orEmpty()
            } else {
                installedInfo.signatures?.map { it.toByteArray() }?.toSet().orEmpty()
            }

            if (archiveCerts.isNotEmpty() && installedCerts.isNotEmpty()) {
                val hasMatchingCert = archiveCerts.any { a ->
                    installedCerts.any { b -> a.contentEquals(b) }
                }
                if (!hasMatchingCert) {
                    return@runCatching "Tanda tangan digital (Keystore Signature) pada rilis APK ini berbeda dengan versi yang terpasang. Harap tunggu build rilis terbaru atau pasang ulang aplikasi."
                }
            }
            null
        }.getOrNull()
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
                    return
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
