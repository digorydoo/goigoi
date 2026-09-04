package io.github.digorydoo.goigoi.activity.splash

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.utils.SingletonHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private const val ANIM_DURATION = 2000 // milliseconds
private const val PROCEED_AFTER = 2100L // milliseconds
private const val POLL_INTERVAL_MILLIS = 100

@Composable
fun SplashScreen(init: suspend CoroutineScope.() -> Unit, onInitComplete: () -> Unit) {
    BackHandler { } // disable Android back button

    val shouldAnimate = !SingletonHolder.singletonsExist
    var coroutineEnded = false
    val animValue = remember { Animatable(if (shouldAnimate) 0.0f else 1.0f) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO, init)
        coroutineEnded = true
    }

    LaunchedEffect(Unit) {
        val startMillis = System.currentTimeMillis()

        if (shouldAnimate) {
            animValue.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = ANIM_DURATION,
                    easing = LinearEasing
                )
            )

            val millisPassed = System.currentTimeMillis() - startMillis
            val millisLeft = PROCEED_AFTER - millisPassed

            if (millisLeft > 0) {
                delay(millisLeft.milliseconds)
            }
        }

        while (!coroutineEnded) {
            delay(POLL_INTERVAL_MILLIS.milliseconds)
        }

        onInitComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val ctx = LocalContext.current
        val drawable = remember { AnimatedLogo(ctx) }

        Canvas(
            modifier = Modifier.size(128.dp)
        ) {
            drawable.animValue = animValue.value
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawIntoCanvas {
                drawable.draw(it.nativeCanvas)
            }
        }
    }
}
