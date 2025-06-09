package com.swapnil.agsl.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


const val IMG_SHADER_SRC = """
    uniform float2 size;
    uniform float time;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        float scale = 1 / size.x;
        float2 scaledCoord = fragCoord * scale;
        float2 center = size * 0.5 * scale;
        float dist = distance(scaledCoord, center);
        float2 dir = scaledCoord - center;
        float sin = sin(dist * 70 - time * 6.28);
        float2 offset = dir * sin;
        float2 textCoord = scaledCoord + offset / 30;
        return composable.eval(textCoord / scale);
    }
"""


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ImageShaderAnimation(
    modifier: Modifier = Modifier,
    shader: RuntimeShader,
    photo: Bitmap
) {
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0f) }
    val shouldRenderEffect = remember { mutableStateOf(true) }

    // Start timer and stop after 2 seconds
    LaunchedEffect(Unit) {
        scope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime <= 1000) {
                timeMs.value = (System.currentTimeMillis() % 100_000L) / 1_000f
                delay(20)
            }
            shouldRenderEffect.value = false // disable shader effect after 2s
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Image(
            bitmap = photo.asImageBitmap(),
            modifier = Modifier
                .onSizeChanged { size ->
                    shader.setFloatUniform("size", size.width.toFloat(), size.height.toFloat())
                }
                .graphicsLayer {
                    clip = true
                    if (shouldRenderEffect.value) {
                        shader.setFloatUniform("time", timeMs.value)
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    } else {
                        renderEffect = null
                    }
                },
            contentScale = ContentScale.FillHeight,
            contentDescription = null,
        )
    }
}
