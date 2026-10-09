package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import io.github.digorydoo.goigoi.R

@Composable
fun PrefsBtn(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_gear_24dp),
            contentDescription = stringResource(R.string.preferences),
        )
    }
}
