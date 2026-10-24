package com.example.ui.components.backdrop.effects

import androidx.compose.ui.graphics.RenderEffect
import com.example.ui.components.backdrop.BackdropEffectScope
import com.example.ui.components.backdrop.RuntimeShader
import com.example.ui.components.backdrop.internal.RuntimeShaderEffect
import com.example.ui.components.backdrop.internal.chain
import com.example.ui.components.backdrop.isRenderEffectSupported
import com.example.ui.components.backdrop.isRuntimeShaderSupported
import org.intellij.lang.annotations.Language

fun BackdropEffectScope.effect(effect: RenderEffect) {
    if (!isRenderEffectSupported()) return

    renderEffect = renderEffect.chain(effect)
}

fun BackdropEffectScope.runtimeShaderEffect(
    key: String,
    @Language("AGSL") shaderString: String,
    uniformShaderName: String,
    block: RuntimeShader.() -> Unit
) {
    if (!isRuntimeShaderSupported()) return

    val effect =
        RuntimeShaderEffect(
            runtimeShader = obtainRuntimeShader(key, shaderString).apply(block),
            uniformShaderName = uniformShaderName
        )
    renderEffect = renderEffect.chain(effect)
}
