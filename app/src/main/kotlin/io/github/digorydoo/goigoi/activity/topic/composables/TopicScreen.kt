package io.github.digorydoo.goigoi.activity.topic.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel
import io.github.digorydoo.goigoi.composables.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.composables.menus.UnytCtxMenu
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(topic: Topic, model: TopicActivityModel, onUnytClicked: (Unyt) -> Unit, onBack: () -> Unit) {
    val unytOfMenu = remember { mutableStateOf(null as Unyt?) }

    Scaffold(
        topBar = {
            GoigoiAppBar(
                title = topic.name.withSystemLangExcept("ja"), // use en if systemLang is ja
                onBack = onBack,
            )
        }
    ) { innerPadding ->
        TopicContent(
            modifier = Modifier.padding(innerPadding),
            topic = topic,
            model = model,
            onUnytClicked = onUnytClicked,
            onUnytLongPressed = { unytOfMenu.value = it }
        )

        unytOfMenu.value?.let { unyt ->
            UnytCtxMenu(
                model,
                unyt,
                onDismissRequest = { unytOfMenu.value = null }
            )
        }
    }
}
