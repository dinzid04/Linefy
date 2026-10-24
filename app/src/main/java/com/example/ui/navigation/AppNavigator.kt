package com.example.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import com.example.data.model.HomeShelf

/**
 * A page pushed over the tabs: Settings and every page under it, and the
 * handful of pages a tab opens that are not a release.
 */
sealed interface AppRoute {
    data object Settings : AppRoute
    data object AccountScrobbling : AppRoute
    data object Sources : AppRoute
    data object ListenTogether : AppRoute
    data object WearOs : AppRoute
    data object Equalizer : AppRoute
    data object Replay : AppRoute
    data object History : AppRoute
    data object Discord : AppRoute
    data object Spotify : AppRoute

    /** A Library shelf's "Show all" — its own cards laid out again as a grid. */
    data class LibraryShowAll(val shelf: HomeShelf) : AppRoute
}

/**
 * One route on the stack.
 */
data class RouteEntry(val id: Long, val route: AppRoute, val detailDepth: Int)

/**
 * The one back stack the whole app navigates by.
 */
@Stable
class AppNavigator(
    private val detailDepth: () -> Int,
    private val popDetail: () -> Boolean,
    private val clearDetails: () -> Unit,
) {
    private val stack = mutableStateListOf<RouteEntry>()
    private var nextId = System.currentTimeMillis()

    val entries: List<RouteEntry> get() = stack

    fun visibleEntry(depth: Int = detailDepth()): RouteEntry? =
        stack.lastOrNull()?.takeIf { depth <= it.detailDepth }

    fun visibleRoute(depth: Int = detailDepth()): AppRoute? = visibleEntry(depth)?.route

    operator fun contains(route: AppRoute): Boolean = stack.any { it.route == route }

    fun push(route: AppRoute) {
        val depth = detailDepth()
        if (visibleRoute(depth) == route) return
        stack += RouteEntry(nextId++, route, depth)
    }

    fun pushAll(vararg routes: AppRoute) {
        if (routes.isEmpty() || visibleRoute() == routes.last()) return
        routes.forEach(::push)
    }

    fun pop(): Boolean {
        val top = stack.lastOrNull()
        if (detailDepth() > (top?.detailDepth ?: 0)) return popDetail()
        if (top == null) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    fun clear() {
        stack.clear()
        clearDetails()
    }

    fun reset(vararg routes: AppRoute) {
        clear()
        routes.forEach(::push)
    }

    fun onDetailStackShrunk(depth: Int) {
        for (i in stack.indices) {
            val entry = stack[i]
            if (entry.detailDepth > depth) stack[i] = entry.copy(detailDepth = depth)
        }
    }
}

/** What fills the page area: the key the page switcher animates between. */
sealed interface AppDestination {
    val key: String

    data class Tab(val index: Int) : AppDestination {
        override val key: String get() = "tab:$index"
    }

    data class Route(val entry: RouteEntry) : AppDestination {
        override val key: String get() = "route:${entry.id}"
    }

    data class Release(val browseId: String) : AppDestination {
        override val key: String get() = browseId
    }
}
