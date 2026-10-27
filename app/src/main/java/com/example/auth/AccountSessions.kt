package com.example.auth

import org.json.JSONArray
import org.json.JSONObject

data class AccountSession(
    val key: String,
    val name: String,
    val handle: String?,
    val avatarUrl: String?,
    val cookie: String,
    val pageId: String?,
    val dataSyncId: String?,
    val authUser: String?,
    val visitorData: String?
)

internal object AccountSessions {

    fun decode(raw: String?): List<AccountSession> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val key = obj.optString("key").takeIf { it.isNotBlank() } ?: continue
                    val cookie = obj.optString("cookie").takeIf { it.isNotBlank() } ?: continue
                    add(
                        AccountSession(
                            key = key,
                            name = obj.optString("name").ifBlank { "YouTube Account" },
                            handle = obj.optString("handle").takeIf { it.isNotBlank() },
                            avatarUrl = obj.optString("avatarUrl").takeIf { it.isNotBlank() },
                            cookie = cookie,
                            pageId = obj.optString("pageId").takeIf { it.isNotBlank() },
                            dataSyncId = obj.optString("dataSyncId").takeIf { it.isNotBlank() },
                            authUser = obj.optString("authUser").takeIf { it.isNotBlank() },
                            visitorData = obj.optString("visitorData").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun encode(list: List<AccountSession>): String {
        val arr = JSONArray()
        for (s in list) {
            val obj = JSONObject()
            obj.put("key", s.key)
            obj.put("name", s.name)
            s.handle?.let { obj.put("handle", it) }
            s.avatarUrl?.let { obj.put("avatarUrl", it) }
            obj.put("cookie", s.cookie)
            s.pageId?.let { obj.put("pageId", it) }
            s.dataSyncId?.let { obj.put("dataSyncId", it) }
            s.authUser?.let { obj.put("authUser", it) }
            s.visitorData?.let { obj.put("visitorData", it) }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun upsert(existing: List<AccountSession>, incoming: AccountSession): List<AccountSession> {
        val filtered = existing.filterNot {
            it.key == incoming.key ||
                (!incoming.handle.isNullOrBlank() && it.handle.equals(incoming.handle, ignoreCase = true))
        }
        return listOf(incoming) + filtered
    }

    fun remove(existing: List<AccountSession>, key: String): List<AccountSession> =
        existing.filterNot { it.key == key }

    fun keyFor(pageId: String?, dataSyncId: String?, handle: String?, cookie: String): String {
        pageId?.takeIf { it.isNotBlank() }?.let { return "pid:$it" }
        dataSyncId?.takeIf { it.isNotBlank() }?.let { return "ds:$it" }
        handle?.takeIf { it.isNotBlank() }?.let { return "h:${it.lowercase()}" }
        return "ck:${cookie.hashCode()}"
    }
}
