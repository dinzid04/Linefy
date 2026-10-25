package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Foreground MediaPlaybackService that keeps music playing when the app is minimized or closed
 * and publishes a system-wide MediaSession + MediaStyle Notification with:
 * - Monochrome white Lyra Harp small icon (`R.drawable.ic_notification_logo`)
 * - High-res album cover artwork
 * - Song title & artist
 * - Interactive seekbar
 * - 5 transport actions: [Love (♡/♥)] [Previous (|◀)] [Play/Pause (▶/||)] [Next (▶|)] [Repeat (Off/All/One)]
 */
class MusicPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "bitchord_media_playback_channel"
        const val NOTIFICATION_ID = 10042

        const val ACTION_UPDATE = "com.example.player.action.UPDATE"
        const val ACTION_TOGGLE_PLAY = "com.example.player.action.TOGGLE_PLAY"
        const val ACTION_PREV = "com.example.player.action.PREV"
        const val ACTION_NEXT = "com.example.player.action.NEXT"
        const val ACTION_TOGGLE_LIKE = "com.example.player.action.TOGGLE_LIKE"
        const val ACTION_TOGGLE_REPEAT = "com.example.player.action.TOGGLE_REPEAT"
        const val ACTION_TOGGLE_SHUFFLE = "com.example.player.action.TOGGLE_SHUFFLE"
        const val ACTION_STOP = "com.example.player.action.STOP"

        private const val CUSTOM_ACTION_LIKE = "com.example.player.custom.LIKE"
        private const val CUSTOM_ACTION_REPEAT = "com.example.player.custom.REPEAT"

        fun startOrUpdate(context: Context) {
            val appCtx = context.applicationContext
            val intent = Intent(appCtx, MusicPlaybackService::class.java).apply {
                action = ACTION_UPDATE
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appCtx.startForegroundService(intent)
                } else {
                    appCtx.startService(intent)
                }
            } catch (_: Exception) {
                try {
                    appCtx.startService(intent)
                } catch (_: Exception) {
                }
            }
        }

        fun stop(context: Context) {
            val appCtx = context.applicationContext
            try {
                appCtx.stopService(Intent(appCtx, MusicPlaybackService::class.java))
            } catch (_: Exception) {
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaSession: MediaSession? = null
    private val artworkCache = ConcurrentHashMap<String, Bitmap>()
    private var isForegroundStarted = false

    private val iconLikeOutline by lazy { createNotificationIconBitmap(IconKind.LIKE_OUTLINE) }
    private val iconLikeFilled by lazy { createNotificationIconBitmap(IconKind.LIKE_FILLED) }
    private val iconPrev by lazy { createNotificationIconBitmap(IconKind.PREV) }
    private val iconPlay by lazy { createNotificationIconBitmap(IconKind.PLAY) }
    private val iconPause by lazy { createNotificationIconBitmap(IconKind.PAUSE) }
    private val iconNext by lazy { createNotificationIconBitmap(IconKind.NEXT) }
    private val iconRepeatOff by lazy { createNotificationIconBitmap(IconKind.REPEAT_OFF) }
    private val iconRepeatAll by lazy { createNotificationIconBitmap(IconKind.REPEAT_ALL) }
    private val iconRepeatOne by lazy { createNotificationIconBitmap(IconKind.REPEAT_ONE) }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        val controller = AudioPlayerController.getInstance(applicationContext)
        val session = MediaSession(this, "LyrafyMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    if (!controller.playerState.value.isPlaying) {
                        controller.togglePlayPause()
                    }
                }

                override fun onPause() {
                    if (controller.playerState.value.isPlaying) {
                        controller.togglePlayPause()
                    }
                }

                override fun onSkipToNext() {
                    controller.playNext()
                }

                override fun onSkipToPrevious() {
                    controller.playPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    controller.seekToPositionMs(pos)
                }

                override fun onCustomAction(action: String, extras: Bundle?) {
                    when (action) {
                        CUSTOM_ACTION_LIKE -> controller.triggerToggleFavoriteFromNotification()
                        CUSTOM_ACTION_REPEAT -> controller.toggleRepeat()
                    }
                }

                override fun onStop() {
                    controller.dismissPlayer()
                    stopSelf()
                }
            })
            isActive = true
        }
        mediaSession = session
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val controller = AudioPlayerController.getInstance(applicationContext)
        when (intent?.action) {
            ACTION_TOGGLE_PLAY -> controller.togglePlayPause()
            ACTION_PREV -> controller.playPrevious()
            ACTION_NEXT -> controller.playNext()
            ACTION_TOGGLE_LIKE -> controller.triggerToggleFavoriteFromNotification()
            ACTION_TOGGLE_REPEAT -> controller.toggleRepeat()
            ACTION_TOGGLE_SHUFFLE -> controller.toggleShuffle()
            ACTION_STOP -> {
                controller.dismissPlayer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        refreshNotificationAndSession()
        return START_STICKY
    }

    private fun refreshNotificationAndSession() {
        val controller = AudioPlayerController.getInstance(applicationContext)
        val state = controller.playerState.value
        val track = state.currentTrack ?: return
        val isLiked = controller.isCurrentTrackFavorite.value

        val coverUrl = track.ultraResThumbnail.ifBlank { track.thumbnailUrl }
        val cachedBmp = artworkCache[coverUrl]

        updateMediaSessionAndPostNotification(state, isLiked, cachedBmp)

        if (cachedBmp == null && coverUrl.isNotBlank()) {
            serviceScope.launch {
                val fetched = fetchArtworkBitmap(coverUrl)
                if (fetched != null) {
                    artworkCache[coverUrl] = fetched
                    val latestState = controller.playerState.value
                    val latestLiked = controller.isCurrentTrackFavorite.value
                    if (latestState.currentTrack?.id == track.id) {
                        updateMediaSessionAndPostNotification(latestState, latestLiked, fetched)
                    }
                }
            }
        }
    }

    private fun updateMediaSessionAndPostNotification(
        state: PlayerUiState,
        isLiked: Boolean,
        artwork: Bitmap?
    ) {
        val track = state.currentTrack ?: return
        val session = mediaSession ?: return
        val durationMs = (state.durationSec.coerceAtLeast(1) * 1000L)

        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.cleanArtist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, state.playingFromSource)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)

        if (artwork != null) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artwork)
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, artwork)
        }
        session.setMetadata(metadataBuilder.build())

        val playbackStateCode = when {
            state.isLoading -> PlaybackState.STATE_BUFFERING
            state.isPlaying -> PlaybackState.STATE_PLAYING
            else -> PlaybackState.STATE_PAUSED
        }

        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SEEK_TO or
            PlaybackState.ACTION_STOP

        val pbBuilder = PlaybackState.Builder()
            .setActions(actions)
            .setState(
                playbackStateCode,
                state.currentPositionMs,
                if (state.isPlaying) 1.0f else 0.0f
            )

        session.setPlaybackState(pbBuilder.build())

        val notification = buildMediaNotification(state, isLiked, artwork, session)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        try {
            if (!isForegroundStarted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                isForegroundStarted = true
            } else {
                nm?.notify(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            nm?.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildMediaNotification(
        state: PlayerUiState,
        isLiked: Boolean,
        artwork: Bitmap?,
        session: MediaSession
    ): Notification {
        val track = state.currentTrack!!
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val likeAction = Notification.Action.Builder(
            Icon.createWithBitmap(if (isLiked) iconLikeFilled else iconLikeOutline),
            if (isLiked) "Disukai" else "Suka",
            servicePendingIntent(ACTION_TOGGLE_LIKE, 1)
        ).build()

        val prevAction = Notification.Action.Builder(
            Icon.createWithBitmap(iconPrev),
            "Sebelumnya",
            servicePendingIntent(ACTION_PREV, 2)
        ).build()

        val playPauseAction = Notification.Action.Builder(
            Icon.createWithBitmap(if (state.isPlaying) iconPause else iconPlay),
            if (state.isPlaying) "Jeda" else "Putar",
            servicePendingIntent(ACTION_TOGGLE_PLAY, 3)
        ).build()

        val nextAction = Notification.Action.Builder(
            Icon.createWithBitmap(iconNext),
            "Berikutnya",
            servicePendingIntent(ACTION_NEXT, 4)
        ).build()

        val repeatBitmap = when (state.repeatMode) {
            RepeatMode.OFF -> iconRepeatOff
            RepeatMode.ALL -> iconRepeatAll
            RepeatMode.ONE -> iconRepeatOne
        }
        val repeatTitle = when (state.repeatMode) {
            RepeatMode.OFF -> "Ulangi Mati"
            RepeatMode.ALL -> "Ulangi Semua"
            RepeatMode.ONE -> "Ulangi Satu"
        }
        val repeatAction = Notification.Action.Builder(
            Icon.createWithBitmap(repeatBitmap),
            repeatTitle,
            servicePendingIntent(ACTION_TOGGLE_REPEAT, 5)
        ).build()

        val mediaStyle = Notification.MediaStyle()
            .setMediaSession(session.sessionToken)
            .setShowActionsInCompactView(1, 2, 3)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        builder
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(track.title)
            .setContentText(track.cleanArtist)
            .setSubText("Lyra - Fy")
            .setContentIntent(contentPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(state.isPlaying)
            .setOnlyAlertOnce(true)
            .setColorized(true)
            .setColor(Color.parseColor("#2A1F1D"))
            .setStyle(mediaStyle)
            .addAction(likeAction)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(repeatAction)

        if (artwork != null) {
            builder.setLargeIcon(artwork)
        }

        return builder.build()
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MusicPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun fetchArtworkBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val req = ImageRequest.Builder(applicationContext)
                .data(url)
                .size(512, 512)
                .allowHardware(false)
                .build()
            val result = applicationContext.imageLoader.execute(req)
            (result as? SuccessResult)?.drawable?.let { it as? BitmapDrawable }?.bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemutaran Musik Lyra - Fy",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kontrol pemutaran musik latar belakang Lyra - Fy"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }

    private enum class IconKind {
        LIKE_OUTLINE, LIKE_FILLED, PREV, PLAY, PAUSE, NEXT, REPEAT_OFF, REPEAT_ALL, REPEAT_ONE
    }

    private fun createNotificationIconBitmap(kind: IconKind): Bitmap {
        val size = 72
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 5.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        when (kind) {
            IconKind.LIKE_OUTLINE, IconKind.LIKE_FILLED -> {
                paint.style = if (kind == IconKind.LIKE_FILLED) Paint.Style.FILL else Paint.Style.STROKE
                val path = Path().apply {
                    moveTo(36f, 58f)
                    cubicTo(14f, 43f, 12f, 22f, 24f, 17f)
                    cubicTo(31f, 14f, 36f, 21f, 36f, 25f)
                    cubicTo(36f, 21f, 41f, 14f, 48f, 17f)
                    cubicTo(60f, 22f, 58f, 43f, 36f, 58f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            IconKind.PREV -> {
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(16f, 18f, 22f, 54f, 3f, 3f, paint)
                val path = Path().apply {
                    moveTo(25f, 36f)
                    lineTo(54f, 18f)
                    lineTo(54f, 54f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            IconKind.PLAY -> {
                paint.style = Paint.Style.FILL
                val path = Path().apply {
                    moveTo(22f, 16f)
                    lineTo(56f, 36f)
                    lineTo(22f, 56f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            IconKind.PAUSE -> {
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(20f, 16f, 31f, 56f, 4f, 4f, paint)
                canvas.drawRoundRect(41f, 16f, 52f, 56f, 4f, 4f, paint)
            }
            IconKind.NEXT -> {
                paint.style = Paint.Style.FILL
                val path = Path().apply {
                    moveTo(18f, 18f)
                    lineTo(47f, 36f)
                    lineTo(18f, 54f)
                    close()
                }
                canvas.drawPath(path, paint)
                canvas.drawRoundRect(50f, 18f, 56f, 54f, 3f, 3f, paint)
            }
            IconKind.REPEAT_OFF, IconKind.REPEAT_ALL, IconKind.REPEAT_ONE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5.0f
                paint.alpha = if (kind == IconKind.REPEAT_OFF) 150 else 255

                // Top loop arrow (left to right)
                val topPath = Path().apply {
                    moveTo(18f, 32f)
                    lineTo(18f, 26f)
                    cubicTo(18f, 22f, 21f, 20f, 25f, 20f)
                    lineTo(52f, 20f)
                }
                canvas.drawPath(topPath, paint)
                canvas.drawLine(45f, 14f, 53f, 20f, paint)
                canvas.drawLine(45f, 26f, 53f, 20f, paint)

                // Bottom loop arrow (right to left)
                val bottomPath = Path().apply {
                    moveTo(54f, 40f)
                    lineTo(54f, 46f)
                    cubicTo(54f, 50f, 51f, 52f, 47f, 52f)
                    lineTo(20f, 52f)
                }
                canvas.drawPath(bottomPath, paint)
                canvas.drawLine(27f, 46f, 19f, 52f, paint)
                canvas.drawLine(27f, 58f, 19f, 52f, paint)

                if (kind == IconKind.REPEAT_ONE) {
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 21f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("1", 36f, 43f, textPaint)
                }
            }
        }
        return bmp
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val controller = AudioPlayerController.getInstance(applicationContext)
        if (!controller.playerState.value.isPlaying && controller.playerState.value.currentTrack == null) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
        } catch (_: Exception) {
        }
        mediaSession = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
