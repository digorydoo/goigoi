package io.github.digorydoo.goigoi.providers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Keep this private; colours need to be accessed through theme
private object Palette {
    val green700 = Color(0xFF349A35)
    val green750 = Color(0xFF318B34)
    val green800 = Color(0xFF2E7D32)
    val green900 = Color(0xFF1B5E20)

    val white = Color(0xFFFFFFFF)
    val grey50 = Color(0xFFFAFAFA)
    val grey200 = Color(0xFFEEEEEE)
    val grey250 = Color(0xFFE7E7E7)
    val grey300 = Color(0xFFE0E0E0)
    val grey400 = Color(0xFFBDBDBD)
    val grey500 = Color(0xFF9E9E9E)
    val grey550 = Color(0xFF898989)
    val grey600 = Color(0xFF757575)
    val grey700 = Color(0xFF616161)
    val grey800 = Color(0xFF424242)
    val grey875 = Color(0xFF292929)
    val grey900 = Color(0xFF212121)
    val grey925 = Color(0xFF191919)
    val grey950 = Color(0xFF111111)
    val black = Color(0xFF000000)

    val opacityBlack1F = Color(0x1F000000)
    val opacityBlack3D = Color(0x3D000000)
    val opacityBlack42 = Color(0x42000000)
    val opacityBlack8A = Color(0x8A000000)
    val opacityBlackDD = Color(0xDD000000)

    val opacityWhite1A = Color(0x1AFFFFFF)
    val opacityWhite22 = Color(0x22FFFFFF)
    val opacityWhite77 = Color(0x77FFFFFF)
}

@Immutable
data class GoigoiColors(
    // These colours will be copied into the material theme
    val primary: Color, // our brand colour
    val onPrimary: Color, // text drawn over primary
    val background: Color, // general activity background
    val onBackground: Color,
    val surface: Color, // background of cards, sheets, menus
    val onSurface: Color,
    val surfaceContainerHigh: Color, // background of alerts
    val outlineVariant: Color, // e.g. dividers

    // These colours are custom
    val appBarContainer: Color,
    val onAppBarContainer: Color,
    val emphasizedText: Color,
    val onBackgroundSecondary: Color,
    val statusBar: Color,
    val onStatusBar: Color,
    val decorativeIconTint: Color, // an icon that does not represent an action
    val dimmedDecorativeIconBackground: Color, // e.g. ZzzIconDrawable's background
    val onDimmedDecorativeIconBackground: Color, // e.g. ZzzIconDrawable's foreground
    val ring: Color, // e.g. big ring's "trail"
    val faintRing: Color, // e.g. big ring's "track"
    val popupOutline: Color, // e.g. HintBalloon's outline
    val popupShadow: Color, // e.g. HintBalloon's shadow
    val poorRating: Color, // BubbleIcon
    val bubbleBackground: Color, // BubbleIcon
    val bubbleOutline: Color, // BubbleIcon
    val highlight: Color, // Highlightable
)

private val darkGoigoiScheme = GoigoiColors(
    primary = Palette.green800,
    onPrimary = Palette.white,
    background = Palette.grey900,
    onBackground = Palette.white,
    surface = Palette.grey875,
    onSurface = Palette.white,
    surfaceContainerHigh = Palette.grey875,
    outlineVariant = Palette.opacityWhite22,
    appBarContainer = Palette.grey925,
    onAppBarContainer = Palette.grey500,
    emphasizedText = Palette.green750,
    onBackgroundSecondary = Palette.opacityWhite77,
    statusBar = Palette.black,
    onStatusBar = Palette.white,
    decorativeIconTint = Palette.white,
    ring = Palette.grey700,
    faintRing = Palette.grey950,
    popupOutline = Palette.opacityBlack1F,
    popupShadow = Palette.opacityBlack3D,
    dimmedDecorativeIconBackground = Palette.grey800,
    onDimmedDecorativeIconBackground = Palette.grey500,
    poorRating = Palette.grey700,
    bubbleBackground = Palette.grey950,
    bubbleOutline = Palette.black,
    highlight = Palette.opacityWhite1A,
)

private val lightGoigoiScheme = GoigoiColors(
    primary = Palette.green800,
    onPrimary = Palette.white,
    background = Palette.grey50,
    onBackground = Palette.opacityBlackDD,
    surface = Palette.white,
    onSurface = Palette.opacityBlackDD,
    surfaceContainerHigh = Palette.white,
    outlineVariant = Palette.opacityBlack1F,
    appBarContainer = Palette.green800,
    onAppBarContainer = Palette.white,
    emphasizedText = Palette.green800,
    onBackgroundSecondary = Palette.opacityBlack8A,
    statusBar = Palette.grey500,
    onStatusBar = Palette.opacityBlackDD,
    decorativeIconTint = Palette.opacityBlack8A,
    ring = Palette.grey400,
    faintRing = Palette.grey200,
    popupOutline = Palette.opacityBlackDD,
    popupShadow = Palette.opacityBlack3D,
    dimmedDecorativeIconBackground = Palette.grey300,
    onDimmedDecorativeIconBackground = Palette.grey500,
    poorRating = Palette.grey550,
    bubbleBackground = Palette.grey250,
    bubbleOutline = Palette.grey300,
    highlight = Palette.opacityBlack1F,
)

private val darkMaterialScheme = darkColorScheme(
    primary = darkGoigoiScheme.primary,
    onPrimary = darkGoigoiScheme.onPrimary,
    background = darkGoigoiScheme.background,
    onBackground = darkGoigoiScheme.onBackground,
    surface = darkGoigoiScheme.surface,
    onSurface = darkGoigoiScheme.onSurface,
    surfaceContainerHigh = darkGoigoiScheme.surfaceContainerHigh,
    outlineVariant = darkGoigoiScheme.outlineVariant,
)

private val lightMaterialScheme = lightColorScheme(
    primary = lightGoigoiScheme.primary,
    onPrimary = lightGoigoiScheme.onPrimary,
    background = lightGoigoiScheme.background,
    onBackground = lightGoigoiScheme.onBackground,
    surface = lightGoigoiScheme.surface,
    onSurface = lightGoigoiScheme.onSurface,
    surfaceContainerHigh = lightGoigoiScheme.surfaceContainerHigh,
    outlineVariant = lightGoigoiScheme.outlineVariant,
)

private val LocalGoigoiCustomScheme = staticCompositionLocalOf {
    GoigoiColors(
        primary = Color.Unspecified,
        onPrimary = Color.Unspecified,
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        surfaceContainerHigh = Color.Unspecified,
        outlineVariant = Color.Unspecified,
        appBarContainer = Color.Unspecified,
        onAppBarContainer = Color.Unspecified,
        emphasizedText = Color.Unspecified,
        onBackgroundSecondary = Color.Unspecified,
        statusBar = Color.Unspecified,
        onStatusBar = Color.Unspecified,
        decorativeIconTint = Color.Unspecified,
        ring = Color.Unspecified,
        faintRing = Color.Unspecified,
        popupOutline = Color.Unspecified,
        popupShadow = Color.Unspecified,
        dimmedDecorativeIconBackground = Color.Unspecified,
        onDimmedDecorativeIconBackground = Color.Unspecified,
        poorRating = Color.Unspecified,
        bubbleBackground = Color.Unspecified,
        bubbleOutline = Color.Unspecified,
        highlight = Color.Unspecified,
    )
}

@Immutable
data class GoigoiCustomTypography(
    val appBarTitle: TextStyle,
    val appBarTitleSmall: TextStyle,
    val bigContentTitle: TextStyle,
    val listItemPrimaryText: TextStyle,
    val listItemSecondaryText: TextStyle,
)

private val customTypography = GoigoiCustomTypography(
    appBarTitle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.em,
    ),
    appBarTitleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.em,
    ),
    bigContentTitle = TextStyle(
        fontSize = 26.sp,
    ),
    listItemPrimaryText = TextStyle(
        fontSize = 17.sp,
    ),
    listItemSecondaryText = TextStyle(
        fontSize = 14.sp,
    )
)

private val LocalGoigoiCustomTypography = staticCompositionLocalOf {
    GoigoiCustomTypography(
        appBarTitle = TextStyle.Default,
        appBarTitleSmall = TextStyle.Default,
        bigContentTitle = TextStyle.Default,
        listItemPrimaryText = TextStyle.Default,
        listItemSecondaryText = TextStyle.Default,
    )
}

object GoigoiTheme {
    val colours: GoigoiColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGoigoiCustomScheme.current

    val typography: GoigoiCustomTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalGoigoiCustomTypography.current
}

@Composable
fun GoigoiTheme(useFixedDarkModeAndAvoidAccessingSingletons: Boolean = false, content: @Composable () -> Unit) {
    val darkTheme = if (useFixedDarkModeAndAvoidAccessingSingletons) true else Singletons.prefs.darkMode
    val customScheme = if (darkTheme) darkGoigoiScheme else lightGoigoiScheme
    val standardScheme = if (darkTheme) darkMaterialScheme else lightMaterialScheme

    CompositionLocalProvider(
        LocalGoigoiCustomScheme provides customScheme,
        LocalGoigoiCustomTypography provides customTypography
    ) {
        MaterialTheme(
            colorScheme = standardScheme,
            content = content
        )
    }
}
