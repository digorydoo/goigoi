package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel

// This is intentionally a global variable, because we need to track this across activity lifecycle
private var cachedTodaysStudyProgress = Float.NaN

private const val ANIM_OVERLAP_MILLIS = 1100
private const val DAY_ICONS_ANIM_MIN_PRE_DELAY_MILLIS = 300

@Composable
fun BigRingAndDayIcons(model: WelcomeActivityModel) {
    val todaysProgress = model.todaysProgress.collectAsState().value
    val dailyProgress = model.dailyProgress.collectAsState().value

    val shouldAnimateProgress = todaysProgress != cachedTodaysStudyProgress
    cachedTodaysStudyProgress = todaysProgress

    val bigRingAnimDurationMillis =
        if (!shouldAnimateProgress) null
        else (500.0f + 1200.0f * todaysProgress).toInt()

    val dayIconsPreDelay =
        if (bigRingAnimDurationMillis == null) null
        else maxOf(DAY_ICONS_ANIM_MIN_PRE_DELAY_MILLIS, bigRingAnimDurationMillis - ANIM_OVERLAP_MILLIS)

    BigRingArea(todaysProgress, bigRingAnimDurationMillis, model)
    DayIconsArea(dailyProgress, dayIconsPreDelay)
}
