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
import com.swapnil.agsl.presentation.navigation_animation.data.WaveEffectParams
import kotlinx.coroutines.android.awaitFrame

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
internal fun ExpandingWaveEffect(
    origin: Offset,
    trigger: Int,
    params: WaveEffectParams,
    content: @Composable () -> Unit
) {
    var elapsedTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
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

    val rippleShaderCode = """
        uniform shader inputShader;
        uniform float2 uResolution;
        uniform float2 uOrigin;
        uniform float uTime;
        uniform float uAmplitude;
        uniform float uFrequency;
        uniform float uDecay;
        uniform float uSpeed;
        
        half4 main(float2 fragCoord) {
            float2 pos = fragCoord;
            float distance = length(pos - uOrigin);
            float delay = distance / uSpeed;
            float time = max(0.0, uTime - delay);
            float rippleAmount = uAmplitude * sin(uFrequency * time) * exp(-uDecay * time);
            float2 n = normalize(pos - uOrigin);
            float2 newPos = pos + rippleAmount * n;
            return inputShader.eval(newPos);
        }
    """.trimIndent()

    val runtimeShader = remember { RuntimeShader(rippleShaderCode) }

    if (origin.x.isFinite() && origin.y.isFinite()) {
        runtimeShader.setFloatUniform("uOrigin", floatArrayOf(origin.x, origin.y))
    }
    if (screenWidth > 0 && screenHeight > 0) {
        runtimeShader.setFloatUniform("uResolution", floatArrayOf(screenWidth, screenHeight))
    }
    if (elapsedTime.isFinite()) {
        runtimeShader.setFloatUniform("uTime", elapsedTime)
    }

    runtimeShader.setFloatUniform("uAmplitude", params.amplitude)
    runtimeShader.setFloatUniform("uFrequency", params.frequency)
    runtimeShader.setFloatUniform("uDecay", params.decay)
    runtimeShader.setFloatUniform("uSpeed", params.speed)

    val androidRenderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputShader")
    val composeRenderEffect = androidRenderEffect.asComposeRenderEffect()

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { renderEffect = composeRenderEffect }
    ) {
        content()
    }
}