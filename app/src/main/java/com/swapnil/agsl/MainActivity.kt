package com.swapnil.agsl

import android.graphics.BitmapFactory
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.swapnil.agsl.presentation.IMG_SHADER_SRC
import com.swapnil.agsl.presentation.ImageShaderAnimation
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
                ImageShaderAnimation(shader=shader, photo = photo)
            }
        }
    }
}

