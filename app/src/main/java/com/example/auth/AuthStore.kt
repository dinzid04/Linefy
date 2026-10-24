package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthStore {
    private const val PREFS_FILE = "bitchord_auth_enc"
    private const val LEGACY_PREFS_FILE = "bitchord_auth"
    private const val KEY_COOKIE = "yt_cookie"
    private const val KEY_VISITOR = "yt_visitor_data"
    private const val KEY_PAGE_ID = "yt_page_id"
    private const val KEY_DATA_SYNC_ID = "yt_data_sync_id"
    private const val KEY_AUTH_USER = "yt_auth_user"
    private const val KEY_CHANNEL_NAME = "yt_channel_name"
    private const val KEY_CHANNEL_HANDLE = "yt_channel_handle"
    private const val KEY_CHANNEL_AVATAR = "yt_channel_avatar"
    private const val KEY_SESSIONS = "yt_saved_sessions"
    private const val KEY_SPOTIFY_COOKIE = "sp_cookie"
    private const val KEY_DISCORD_TOKEN = "dc_token"
    private const val KEY_DISCORD_ENABLED = "dc_enabled"
    private const val KEY_LASTFM_API_KEY = "lfm_api_key"
    private const val KEY_LASTFM_SECRET = "lfm_secret"
    private const val KEY_LASTFM_SESSION = "lfm_session"
    private const val KEY_LASTFM_USER = "lfm_user"
    private const val KEY_LASTFM_ENABLED = "lfm_enabled"
    private const val KEY_LB_TOKEN = "lb_token"
    private const val KEY_LB_USER = "lb_user"
    private const val KEY_LB_ENABLED = "lb_enabled"

    private lateinit var prefs: SharedPreferences

    private val _cookie = MutableStateFlow<String?>(null)
    val cookie: StateFlow<String?> = _cookie.asStateFlow()

    private val _visitorData = MutableStateFlow<String?>(null)
    val visitorData: StateFlow<String?> = _visitorData.asStateFlow()

    private val _pageId = MutableStateFlow<String?>(null)
    val pageId: StateFlow<String?> = _pageId.asStateFlow()

    private val _dataSyncId = MutableStateFlow<String?>(null)
    val dataSyncId: StateFlow<String?> = _dataSyncId.asStateFlow()

    private val _authUser = MutableStateFlow<String?>(null)
    val authUser: StateFlow<String?> = _authUser.asStateFlow()

    private val _channelName = MutableStateFlow<String?>(null)
    val channelName: StateFlow<String?> = _channelName.asStateFlow()

    private val _channelHandle = MutableStateFlow<String?>(null)
    val channelHandle: StateFlow<String?> = _channelHandle.asStateFlow()

    private val _channelAvatar = MutableStateFlow<String?>(null)
    val channelAvatar: StateFlow<String?> = _channelAvatar.asStateFlow()

    private val _savedSessions = MutableStateFlow<List<AccountSession>>(emptyList())
    val savedSessions: StateFlow<List<AccountSession>> = _savedSessions.asStateFlow()

    private val _spotifyCookie = MutableStateFlow<String?>(null)
    val spotifyCookie: StateFlow<String?> = _spotifyCookie.asStateFlow()

    private val _discordToken = MutableStateFlow<String?>(null)
    val discordToken: StateFlow<String?> = _discordToken.asStateFlow()

    private val _discordEnabled = MutableStateFlow(true)
    val discordEnabled: StateFlow<Boolean> = _discordEnabled.asStateFlow()

    private val _lastFmApiKey = MutableStateFlow<String?>(null)
    val lastFmApiKey: StateFlow<String?> = _lastFmApiKey.asStateFlow()

    private val _lastFmSecret = MutableStateFlow<String?>(null)
    val lastFmSecret: StateFlow<String?> = _lastFmSecret.asStateFlow()

    private val _lastFmSession = MutableStateFlow<String?>(null)
    val lastFmSession: StateFlow<String?> = _lastFmSession.asStateFlow()

    private val _lastFmUsername = MutableStateFlow<String?>(null)
    val lastFmUsername: StateFlow<String?> = _lastFmUsername.asStateFlow()

    private val _lastFmEnabled = MutableStateFlow(true)
    val lastFmEnabled: StateFlow<Boolean> = _lastFmEnabled.asStateFlow()

    private val _listenBrainzToken = MutableStateFlow<String?>(null)
    val listenBrainzToken: StateFlow<String?> = _listenBrainzToken.asStateFlow()

    private val _listenBrainzUsername = MutableStateFlow<String?>(null)
    val listenBrainzUsername: StateFlow<String?> = _listenBrainzUsername.asStateFlow()

    private val _listenBrainzEnabled = MutableStateFlow(true)
    val listenBrainzEnabled: StateFlow<Boolean> = _listenBrainzEnabled.asStateFlow()

    val isInitialized: Boolean
        get() = ::prefs.isInitialized

    fun hasApiSid(cookie: String?): Boolean = SignedIn.hasValidCookie(cookie)

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = EncryptedPrefs.open(context, PREFS_FILE, LEGACY_PREFS_FILE)
        _cookie.value = prefs.getString(KEY_COOKIE, null)?.takeIf { it.isNotBlank() }
        _visitorData.value = prefs.getString(KEY_VISITOR, null)?.takeIf { it.isNotBlank() }
        _pageId.value = prefs.getString(KEY_PAGE_ID, null)?.takeIf { it.isNotBlank() }
        _dataSyncId.value = prefs.getString(KEY_DATA_SYNC_ID, null)?.takeIf { it.isNotBlank() }
        _authUser.value = prefs.getString(KEY_AUTH_USER, null)?.takeIf { it.isNotBlank() }
        _channelName.value = prefs.getString(KEY_CHANNEL_NAME, null)?.takeIf { it.isNotBlank() }
        _channelHandle.value = prefs.getString(KEY_CHANNEL_HANDLE, null)?.takeIf { it.isNotBlank() }
        _channelAvatar.value = prefs.getString(KEY_CHANNEL_AVATAR, null)?.takeIf { it.isNotBlank() }
        _savedSessions.value = AccountSessions.decode(prefs.getString(KEY_SESSIONS, null))
        _spotifyCookie.value = prefs.getString(KEY_SPOTIFY_COOKIE, null)?.takeIf { it.isNotBlank() }
        _discordToken.value = prefs.getString(KEY_DISCORD_TOKEN, null)?.takeIf { it.isNotBlank() }
        _discordEnabled.value = prefs.getBoolean(KEY_DISCORD_ENABLED, true)
        _lastFmApiKey.value = prefs.getString(KEY_LASTFM_API_KEY, null)?.takeIf { it.isNotBlank() }
        _lastFmSecret.value = prefs.getString(KEY_LASTFM_SECRET, null)?.takeIf { it.isNotBlank() }
        _lastFmSession.value = prefs.getString(KEY_LASTFM_SESSION, null)?.takeIf { it.isNotBlank() }
        _lastFmUsername.value = prefs.getString(KEY_LASTFM_USER, null)?.takeIf { it.isNotBlank() }
        _lastFmEnabled.value = prefs.getBoolean(KEY_LASTFM_ENABLED, true)
        _listenBrainzToken.value = prefs.getString(KEY_LB_TOKEN, null)?.takeIf { it.isNotBlank() }
        _listenBrainzUsername.value = prefs.getString(KEY_LB_USER, null)?.takeIf { it.isNotBlank() }
        _listenBrainzEnabled.value = prefs.getBoolean(KEY_LB_ENABLED, true)
    }

    fun setCookie(cookie: String?, visitorData: String? = null) {
        val clean = cookie?.trim()?.takeIf { it.isNotBlank() }
        _cookie.value = clean
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            if (clean == null) remove(KEY_COOKIE) else putString(KEY_COOKIE, clean)
            if (visitorData != null) {
                _visitorData.value = visitorData.takeIf { it.isNotBlank() }
                if (visitorData.isBlank()) remove(KEY_VISITOR) else putString(KEY_VISITOR, visitorData)
            }
        }.apply()
    }

    fun setActiveChannel(
        pageId: String?,
        name: String? = null,
        handle: String? = null,
        avatarUrl: String? = null,
        rawDataSyncId: String? = null,
        authUser: String? = null
    ) {
        val parsed = DataSyncId.parse(rawDataSyncId)
        val cleanId = pageId?.trim()?.takeIf { it.isNotBlank() } ?: parsed.pageId
        val cleanDataSync = parsed.dataSyncId
        val cleanAuthUser = authUser?.trim()?.takeIf { it.isNotBlank() }
        val cleanName = name?.trim()?.takeIf { it.isNotBlank() }
        val cleanHandle = handle?.trim()?.takeIf { it.isNotBlank() }
        val cleanAvatar = avatarUrl?.trim()?.takeIf { it.isNotBlank() }
        _pageId.value = cleanId
        _dataSyncId.value = cleanDataSync
        if (authUser != null) _authUser.value = cleanAuthUser
        _channelName.value = cleanName
        _channelHandle.value = cleanHandle
        _channelAvatar.value = cleanAvatar
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            if (cleanId == null) remove(KEY_PAGE_ID) else putString(KEY_PAGE_ID, cleanId)
            if (cleanDataSync == null) remove(KEY_DATA_SYNC_ID) else putString(KEY_DATA_SYNC_ID, cleanDataSync)
            if (authUser != null) {
                if (cleanAuthUser == null) remove(KEY_AUTH_USER) else putString(KEY_AUTH_USER, cleanAuthUser)
            }
            if (cleanName == null) remove(KEY_CHANNEL_NAME) else putString(KEY_CHANNEL_NAME, cleanName)
            if (cleanHandle == null) remove(KEY_CHANNEL_HANDLE) else putString(KEY_CHANNEL_HANDLE, cleanHandle)
            if (cleanAvatar == null) remove(KEY_CHANNEL_AVATAR) else putString(KEY_CHANNEL_AVATAR, cleanAvatar)
        }.apply()
    }

    fun snapshotCurrentSession(
        fallbackName: String? = null,
        fallbackHandle: String? = null,
        fallbackAvatar: String? = null
    ) {
        val ck = _cookie.value?.takeIf { it.isNotBlank() } ?: return
        val name = _channelName.value ?: fallbackName ?: "YouTube Account"
        val handle = _channelHandle.value ?: fallbackHandle
        val avatar = _channelAvatar.value ?: fallbackAvatar
        val session = AccountSession(
            key = AccountSessions.keyFor(_pageId.value, _dataSyncId.value, handle, ck),
            name = name,
            handle = handle,
            avatarUrl = avatar,
            cookie = ck,
            pageId = _pageId.value,
            dataSyncId = _dataSyncId.value,
            authUser = _authUser.value,
            visitorData = _visitorData.value
        )
        val updated = AccountSessions.upsert(_savedSessions.value, session)
        _savedSessions.value = updated
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_SESSIONS, AccountSessions.encode(updated)).apply()
        }
    }

    fun activateSavedSession(session: AccountSession) {
        setCookie(session.cookie, session.visitorData)
        setActiveChannel(
            pageId = session.pageId,
            name = session.name,
            handle = session.handle,
            avatarUrl = session.avatarUrl,
            rawDataSyncId = session.dataSyncId,
            authUser = session.authUser
        )
    }

    fun removeSavedSession(key: String) {
        val updated = AccountSessions.remove(_savedSessions.value, key)
        _savedSessions.value = updated
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_SESSIONS, AccountSessions.encode(updated)).apply()
        }
    }

    fun signOut() {
        val currentCookie = _cookie.value
        if (!currentCookie.isNullOrBlank()) {
            val activeKey = AccountSessions.keyFor(
                _pageId.value, _dataSyncId.value, _channelHandle.value, currentCookie
            )
            removeSavedSession(activeKey)
        }
        _cookie.value = null
        _visitorData.value = null
        _pageId.value = null
        _dataSyncId.value = null
        _authUser.value = null
        _channelName.value = null
        _channelHandle.value = null
        _channelAvatar.value = null
        if (!::prefs.isInitialized) return
        prefs.edit()
            .remove(KEY_COOKIE)
            .remove(KEY_VISITOR)
            .remove(KEY_PAGE_ID)
            .remove(KEY_DATA_SYNC_ID)
            .remove(KEY_AUTH_USER)
            .remove(KEY_CHANNEL_NAME)
            .remove(KEY_CHANNEL_HANDLE)
            .remove(KEY_CHANNEL_AVATAR)
            .apply()
    }

    fun setSpotifyCookie(cookie: String?) {
        val clean = cookie?.trim()?.takeIf { it.isNotBlank() }
        _spotifyCookie.value = clean
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            if (clean == null) remove(KEY_SPOTIFY_COOKIE) else putString(KEY_SPOTIFY_COOKIE, clean)
        }.apply()
    }

    fun signOutSpotify() = setSpotifyCookie(null)

    fun setDiscordToken(token: String?) {
        val clean = token?.trim()?.takeIf { it.isNotBlank() }
        _discordToken.value = clean
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            if (clean == null) remove(KEY_DISCORD_TOKEN) else putString(KEY_DISCORD_TOKEN, clean)
        }.apply()
    }

    fun setDiscordEnabled(enabled: Boolean) {
        _discordEnabled.value = enabled
        if (!::prefs.isInitialized) return
        prefs.edit().putBoolean(KEY_DISCORD_ENABLED, enabled).apply()
    }

    fun signOutDiscord() = setDiscordToken(null)

    fun setLastFmCredentials(apiKey: String, secret: String, sessionKey: String, username: String) {
        val cleanKey = apiKey.trim()
        val cleanSecret = secret.trim()
        val cleanSession = sessionKey.trim()
        val cleanUser = username.trim()
        _lastFmApiKey.value = cleanKey.takeIf { it.isNotEmpty() }
        _lastFmSecret.value = cleanSecret.takeIf { it.isNotEmpty() }
        _lastFmSession.value = cleanSession.takeIf { it.isNotEmpty() }
        _lastFmUsername.value = cleanUser.takeIf { it.isNotEmpty() }
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putString(KEY_LASTFM_API_KEY, cleanKey)
            .putString(KEY_LASTFM_SECRET, cleanSecret)
            .putString(KEY_LASTFM_SESSION, cleanSession)
            .putString(KEY_LASTFM_USER, cleanUser)
            .apply()
    }

    fun setLastFmEnabled(enabled: Boolean) {
        _lastFmEnabled.value = enabled
        if (!::prefs.isInitialized) return
        prefs.edit().putBoolean(KEY_LASTFM_ENABLED, enabled).apply()
    }

    fun signOutLastFm() {
        _lastFmSession.value = null
        _lastFmUsername.value = null
        if (!::prefs.isInitialized) return
        prefs.edit()
            .remove(KEY_LASTFM_SESSION)
            .remove(KEY_LASTFM_USER)
            .apply()
    }

    fun setListenBrainzCredentials(token: String, username: String?) {
        val cleanToken = token.trim().takeIf { it.isNotEmpty() }
        val cleanUser = username?.trim()?.takeIf { it.isNotEmpty() }
        _listenBrainzToken.value = cleanToken
        _listenBrainzUsername.value = cleanUser
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            if (cleanToken == null) remove(KEY_LB_TOKEN) else putString(KEY_LB_TOKEN, cleanToken)
            if (cleanUser == null) remove(KEY_LB_USER) else putString(KEY_LB_USER, cleanUser)
        }.apply()
    }

    fun setListenBrainzEnabled(enabled: Boolean) {
        _listenBrainzEnabled.value = enabled
        if (!::prefs.isInitialized) return
        prefs.edit().putBoolean(KEY_LB_ENABLED, enabled).apply()
    }

    fun signOutListenBrainz() {
        _listenBrainzToken.value = null
        _listenBrainzUsername.value = null
        if (!::prefs.isInitialized) return
        prefs.edit()
            .remove(KEY_LB_TOKEN)
            .remove(KEY_LB_USER)
            .apply()
    }
}
