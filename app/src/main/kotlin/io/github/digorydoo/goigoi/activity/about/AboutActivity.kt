package io.github.digorydoo.goigoi.activity.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.digorydoo.goigoi.composables.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.providers.SingletonsProvider
import io.github.digorydoo.goigoi.utils.ResUtils

class AboutActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        setContent {
            SingletonsProvider(this) {
                DevicePropsProvider(this) {
                    GoigoiTheme {
                        AboutScreen(onBack = { finish() })
                    }
                }
            }
        }
    }
}
