package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.backdrop.backdrops.LayerBackdrop
import com.example.ui.components.backdrop.backdrops.layerBackdrop
import com.example.ui.components.backdrop.backdrops.rememberLayerBackdrop

@Stable
class OptimizedHazeState(
    internal val layerBackdrop: LayerBackdrop
)

@Composable
fun rememberOptimizedHazeState(): OptimizedHazeState {
    val backdrop = rememberLayerBackdrop()
    return remember(backdrop) { OptimizedHazeState(backdrop) }
}

fun Modifier.optimizedHazeSource(state: OptimizedHazeState): Modifier =
    this.layerBackdrop(state.layerBackdrop)

fun Modifier.optimizedHazeEffect(
    state: OptimizedHazeState?,
    tintColor: Color = Color(0xFF1C1C1E).copy(alpha = 0.88f)
): Modifier = this.background(tintColor)
