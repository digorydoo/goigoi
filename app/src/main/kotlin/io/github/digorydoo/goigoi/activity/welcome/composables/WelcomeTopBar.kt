package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R

@Composable
fun WelcomeTopBar(horizPadding: Dp, onPrefsBtnClicked: () -> Unit) {
    // The WelcomeScreen's top bar is part of the content, so we don't call TopAppBar here.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = horizPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.padding(end = horizPadding - 16.dp),
            onClick = onPrefsBtnClicked,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_gear_24dp),
                contentDescription = null
            )
        }
    }
}
