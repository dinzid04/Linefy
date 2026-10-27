package com.example.ui.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

interface MiniPlayerPull {
    fun drag(delta: Float)
    fun release(velocity: Float)
}

private const val SLIDE_STIFFNESS = 195f
private const val SLIDE_SETTLED_PX = 0.5f

@Stable
class PlayerSheetMotion(
    private val scope: CoroutineScope,
    private val dock: PlayerDock,
    private val shown: () -> Boolean,
    private val setShown: (Boolean) -> Unit,
) : MiniPlayerPull {
    var held by mutableFloatStateOf(Float.NaN)
        private set

    val holding: Boolean get() = !held.isNaN()

    var containerHeightPx by mutableFloatStateOf(0f)

    private var settle: Job? = null
    private var closing = false

    private fun travel(): Float =
        dock.sheetTravel.takeIf { it > 0f } ?: containerHeightPx.coerceAtLeast(1000f)

    fun position(): Float = if (holding) held else 0f

    fun open() {
        if (shown()) return
        settle?.cancel()
        closing = false
        val travel = travel()
        held = travel
        setShown(true)
        settle = scope.launch {
            slide(from = travel, to = 0f)
            held = Float.NaN
        }
    }

    fun close() {
        if (!shown() || closing) return
        closing = true
        settle?.cancel()
        val from = if (holding) held else 0f
        held = from
        settle = scope.launch {
            slide(from = from, to = travel())
            setShown(false)
            closing = false
            held = Float.NaN
        }
    }

    override fun drag(delta: Float) {
        val travel = travel()
        if (!holding) {
            if (delta >= 0f || shown()) return
            settle?.cancel()
            closing = false
            held = travel
            setShown(true)
        }
        settle?.cancel()
        held = (held + delta).coerceIn(0f, travel)
    }

    override fun release(velocity: Float) {
        if (!holding) return
        val travel = travel()
        val open = if (abs(velocity) > 400f) velocity < 0f else held < travel * 0.5f
        if (!open) closing = true
        val from = held
        settle = scope.launch {
            slide(from = from, to = if (open) 0f else travel, velocity = velocity)
            if (open) {
                held = Float.NaN
            } else {
                setShown(false)
                closing = false
                held = Float.NaN
            }
        }
    }

    private suspend fun slide(from: Float, to: Float, velocity: Float = 0f) {
        val travel = travel()
        animate(
            initialValue = from,
            targetValue = to,
            initialVelocity = velocity,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = SLIDE_STIFFNESS,
                visibilityThreshold = SLIDE_SETTLED_PX,
            ),
        ) { value, _ ->
            held = value.coerceIn(0f, travel)
        }
    }
}
