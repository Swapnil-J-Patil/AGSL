package com.swapnil.agsl

import android.graphics.BitmapFactory
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.swapnil.agsl.presentation.image_animation.infinite_waves.IMG_SHADER_SRC
import com.swapnil.agsl.presentation.navigation_animation.RevealShaderEffect
import com.swapnil.agsl.presentation.screens.Screen1
import com.swapnil.agsl.presentation.screens.Screen2
import com.swapnil.agsl.presentation.ui.theme.AGSLTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shader = RuntimeShader(IMG_SHADER_SRC)
        val photo = BitmapFactory.decodeResource(resources, R.drawable.tanjiro)
        enableEdgeToEdge()
        setContent {
            AGSLTheme {
                //ImageShaderAnimation(shader=shader, photo = photo)
                RevealShaderEffect(
                    firstContent = {
                        Screen1 {  }
                    },
                    secondContent = {
                        Screen2 {  }
                    }
                )
            }
        }
    }
}

