package com.swapnil.agsl.presentation.image_animation.wave_reveal

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.swapnil.agsl.presentation.navigation_animation.data.RevealTransitionParams
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
internal fun RevealContentTransition(
    origin: Offset,
    trigger: Int,
    params: RevealTransitionParams,
    firstContent: @Composable () -> Unit,
    secondContent: @Composable () -> Unit
) {
    var elapsedTime by remember { mutableFloatStateOf(0f) }
    var isReversed by remember { mutableStateOf(true) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            delay(params.transitionDelay)
            isReversed = !isReversed
            elapsedTime = 0f
            val startTime = withFrameNanos { it }
            do {
                val now = withFrameNanos { it }
                elapsedTime = (now - startTime) / 1_000_000_000f
                if (elapsedTime >= params.duration) break
                awaitFrame()
            } while (true)
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    val rippleMaskShaderCode = """
    uniform shader inputShader;
    uniform float2 uResolution;
    uniform float2 uOrigin;
    uniform float uTime;
    uniform float uSpeed;
    uniform float uFrequency;
    uniform float uAmplitude;
    uniform float uEdgeWidth;
    uniform float uWiggleStrength;
    
    half4 main(float2 fragCoord) {
        float distance = length(fragCoord - uOrigin);
        float radius = uTime * uSpeed;
        
        if (distance < radius - uEdgeWidth) {
            return half4(0.0, 0.0, 0.0, 0.0);
        }
        
        if (distance > radius + uEdgeWidth) {
            return inputShader.eval(fragCoord);
        }
        
        float wiggle = sin(distance * uFrequency + uTime * 6.283) * uWiggleStrength;
        float normDistance = (distance - (radius - uEdgeWidth)) / (2.0 * uEdgeWidth);
        float baseMask = smoothstep(0.0, 1.0, normDistance + wiggle);
        
        half4 color = inputShader.eval(fragCoord);
        color.a *= baseMask;
        
        return color;
    }
    """.trimIndent()

    val runtimeShader = remember { RuntimeShader(rippleMaskShaderCode) }

    if (origin.x.isFinite() && origin.y.isFinite()) {
        runtimeShader.setFloatUniform("uOrigin", floatArrayOf(origin.x, origin.y))
    }
    if (screenWidth > 0 && screenHeight > 0) {
        runtimeShader.setFloatUniform("uResolution", floatArrayOf(screenWidth, screenHeight))
    }
    if (elapsedTime.isFinite()) {
        runtimeShader.setFloatUniform("uTime", elapsedTime)
    }

    runtimeShader.setFloatUniform("uSpeed", params.speed)
    runtimeShader.setFloatUniform("uFrequency", params.frequency)
    runtimeShader.setFloatUniform("uAmplitude", params.amplitude)
    runtimeShader.setFloatUniform("uEdgeWidth", params.edgeWidth)
    runtimeShader.setFloatUniform("uWiggleStrength", params.wiggleStrength)

    val androidRenderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputShader")
    val composeRenderEffect = androidRenderEffect.asComposeRenderEffect()

    Box(modifier = Modifier.fillMaxSize()) {
        // Bottom layer: new content (toggles based on isReversed)
        Box(modifier = Modifier.fillMaxSize()) {
            if (isReversed) secondContent() else firstContent()
        }

        // Top layer: old content (toggles based on isReversed) with shader
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    renderEffect = composeRenderEffect
                    alpha = if (elapsedTime >= params.duration) 0f else 1f
                }
        ) {
            if (isReversed) firstContent() else secondContent()
        }
    }
}