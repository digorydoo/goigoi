package io.github.digorydoo.goigoi.providers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Keep this private; colours need to be accessed through theme
private object Palette {
    val green700 = Color(0xFF349A35)
    val green750 = Color(0xFF318B34)
    val green800 = Color(0xFF2E7D32)
    // val green900 = Color(0xFF1B5E20)

    // val orange700 = Color(0xFFFF8800)
    val orange800 = Color(0xFFCC6600)

    val white = Color(0xFFFFFFFF)
    val grey50 = Color(0xFFFAFAFA)
    val grey200 = Color(0xFFEEEEEE)
    val grey250 = Color(0xFFE7E7E7)
    val grey300 = Color(0xFFE0E0E0)
    val grey400 = Color(0xFFBDBDBD)
    val grey500 = Color(0xFF9E9E9E)
    val grey550 = Color(0xFF898989)

    // val grey600 = Color(0xFF757575)
    val grey700 = Color(0xFF616161)
    val grey800 = Color(0xFF424242)
    val grey850 = Color(0xFF323232)

    // val grey875 = Color(0xFF292929)
    val grey900 = Color(0xFF212121)
    val grey925 = Color(0xFF191919)
    val grey950 = Color(0xFF111111)
    val black = Color(0xFF000000)

    val green800Opacity7F = green800.copy(alpha = 0.5f)

    val opacityBlack08 = Color(0x08000000)
    val opacityBlack1F = Color(0x1F000000)
    val opacityBlack3D = Color(0x3D000000)
    val opacityBlack64 = Color(0x64000000)
    val opacityBlack8A = Color(0x8A000000)
    val opacityBlackDD = Color(0xDD000000)

    val opacityWhite1A = Color(0x1AFFFFFF)
    val opacityWhite1F = Color(0x1FFFFFFF)
    val opacityWhite22 = Color(0x22FFFFFF)
    val opacityWhite64 = Color(0x64FFFFFF)
    val opacityWhite77 = Color(0x77FFFFFF)
}

@Immutable
data class GoigoiColours(
    // These colours will affect standard Material components
    val primary: Color, // our brand colour
    val onPrimary: Color, // text drawn over primary
    val background: Color, // general activity background
    val onBackground: Color,
    val surface: Color, // background of cards, sheets, menus
    val onSurface: Color,
    val surfaceContainerHigh: Color, // background of alerts
    val outline: Color, // e.g. outline of Switch when state is off; stronger than outlineVariant
    val outlineVariant: Color, // e.g. dividers; also used by our KeyboardHandler background

    // These colours are custom
    val appBarContainer: Color,
    val onAppBarContainer: Color,
    val emphasizedText: Color,
    val secondaryOnBackground: Color,
    val faintOnBackground: Color, // AnswerField
    val statusBarWhenEmptyAppBar: Color,
    val statusBarAboveBottomSheet: Color,
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
    val bottomSheetContainer: Color, // WordInfoBottomSheet
    val onBottomSheetContainer: Color, // WordInfoBottomSheet
    val dragHandle: Color, // DragHandle
    val fabBackground: Color, // GoigoiFab
    val pressedFabBackground: Color, // GoigoiFab
    val fabShimColour: Color, // GoigoiFab
    val fabGlowColour: Color, // GoigoiFab
    val onFabBackground: Color, // GoigoiFab
    val keyboardBackground: Color, // KeyboardHandler
    val keyboardOutline: Color, // KeyboardHandler
    val keyLensBackground: Color, // KeyLens
    val warningBackground: Color, // ScoresArea
)

private val darkGoigoiScheme = GoigoiColours(
    primary = Palette.green800,
    onPrimary = Palette.white,
    background = Palette.grey900,
    onBackground = Palette.white,
    surface = Palette.grey850,
    onSurface = Palette.white,
    surfaceContainerHigh = Palette.grey850,
    outline = Palette.opacityWhite64,
    outlineVariant = Palette.opacityWhite1F,
    appBarContainer = Palette.grey925,
    onAppBarContainer = Palette.grey500,
    emphasizedText = Palette.green750,
    secondaryOnBackground = Palette.opacityWhite77,
    faintOnBackground = Palette.opacityWhite1F,
    statusBarWhenEmptyAppBar = Palette.black,
    statusBarAboveBottomSheet = Palette.opacityBlack3D,
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
    bottomSheetContainer = Palette.grey850,
    onBottomSheetContainer = Palette.white,
    dragHandle = Palette.opacityWhite22,
    fabBackground = Palette.green800,
    pressedFabBackground = Palette.green700,
    fabShimColour = Palette.grey900,
    fabGlowColour = Palette.grey400,
    onFabBackground = Palette.white,
    keyboardBackground = Palette.grey950,
    keyboardOutline = Palette.opacityBlack1F,
    keyLensBackground = Palette.grey800,
    warningBackground = Palette.orange800,
)

private val lightGoigoiScheme = GoigoiColours(
    primary = Palette.green800,
    onPrimary = Palette.white,
    background = Palette.grey50,
    onBackground = Palette.opacityBlackDD,
    surface = Palette.white,
    onSurface = Palette.opacityBlackDD,
    surfaceContainerHigh = Palette.white,
    outline = Palette.opacityBlack64,
    outlineVariant = Palette.opacityBlack1F,
    appBarContainer = Palette.green800,
    onAppBarContainer = Palette.white,
    emphasizedText = Palette.green800,
    secondaryOnBackground = Palette.opacityBlack8A,
    faintOnBackground = Palette.opacityBlack1F,
    statusBarWhenEmptyAppBar = Palette.grey500,
    statusBarAboveBottomSheet = Palette.green800Opacity7F,
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
    bottomSheetContainer = Palette.white,
    onBottomSheetContainer = Palette.opacityBlackDD,
    dragHandle = Palette.opacityBlack3D,
    fabBackground = Palette.green800,
    pressedFabBackground = Palette.green700,
    fabShimColour = Palette.grey50,
    fabGlowColour = Palette.grey900,
    onFabBackground = Palette.white,
    keyboardBackground = Palette.grey200,
    keyboardOutline = Palette.opacityBlack08,
    keyLensBackground = Palette.grey300,
    warningBackground = Palette.orange800,
)

private val darkMaterialScheme = darkColorScheme(
    primary = darkGoigoiScheme.primary,
    onPrimary = darkGoigoiScheme.onPrimary,
    background = darkGoigoiScheme.background,
    onBackground = darkGoigoiScheme.onBackground,
    surface = darkGoigoiScheme.surface,
    onSurface = darkGoigoiScheme.onSurface,
    surfaceContainerHigh = darkGoigoiScheme.surfaceContainerHigh,
    outline = darkGoigoiScheme.outline,
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
    outline = lightGoigoiScheme.outline,
    outlineVariant = lightGoigoiScheme.outlineVariant,
)

private val LocalGoigoiColours = staticCompositionLocalOf {
    GoigoiColours(
        primary = Color.Unspecified,
        onPrimary = Color.Unspecified,
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        surfaceContainerHigh = Color.Unspecified,
        outline = Color.Unspecified,
        outlineVariant = Color.Unspecified,
        appBarContainer = Color.Unspecified,
        onAppBarContainer = Color.Unspecified,
        emphasizedText = Color.Unspecified,
        secondaryOnBackground = Color.Unspecified,
        faintOnBackground = Color.Unspecified,
        statusBarWhenEmptyAppBar = Color.Unspecified,
        statusBarAboveBottomSheet = Color.Unspecified,
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
        bottomSheetContainer = Color.Unspecified,
        onBottomSheetContainer = Color.Unspecified,
        dragHandle = Color.Unspecified,
        fabBackground = Color.Unspecified,
        pressedFabBackground = Color.Unspecified,
        fabShimColour = Color.Unspecified,
        fabGlowColour = Color.Unspecified,
        onFabBackground = Color.Unspecified,
        keyboardBackground = Color.Unspecified,
        keyboardOutline = Color.Unspecified,
        keyLensBackground = Color.Unspecified,
        warningBackground = Color.Unspecified,
    )
}

@Immutable
data class GoigoiTypography(
    // These styles will affect standard Material components
    val labelLarge: TextStyle,

    // These styles are custom
    val appBarTitle: TextStyle,
    val appBarTitleSmall: TextStyle,
    val bigContentTitle: TextStyle,
    val listItemPrimaryText: TextStyle,
    val listItemSecondaryText: TextStyle,
    val hint: TextStyle,
    val iconFromChar: TextStyle,
    val badge: TextStyle,
    val keyLens: TextStyle,
)

private val customTypography = GoigoiTypography(
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
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
    bigContentTitle = TextStyle(fontSize = 26.sp),
    listItemPrimaryText = TextStyle(fontSize = 17.sp),
    listItemSecondaryText = TextStyle(fontSize = 14.sp),
    hint = TextStyle(
        fontSize = 17.sp,
        lineHeight = 20.sp,
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Serif,
    ),
    iconFromChar = TextStyle(
        fontWeight = FontWeight.Medium,
        // fontSize needs to be computed, see ListItem
    ),
    badge = TextStyle(fontSize = 12.sp),
    keyLens = TextStyle(fontSize = 18.sp),
)

private val materialTypography = Typography(
    labelLarge = customTypography.labelLarge,
)

private val LocalGoigoiTypography = staticCompositionLocalOf {
    GoigoiTypography(
        labelLarge = TextStyle.Default,
        appBarTitle = TextStyle.Default,
        appBarTitleSmall = TextStyle.Default,
        bigContentTitle = TextStyle.Default,
        listItemPrimaryText = TextStyle.Default,
        listItemSecondaryText = TextStyle.Default,
        hint = TextStyle.Default,
        iconFromChar = TextStyle.Default,
        badge = TextStyle.Default,
        keyLens = TextStyle.Default,
    )
}

object GoigoiTheme {
    val colours: GoigoiColours
        @Composable
        @ReadOnlyComposable
        get() = LocalGoigoiColours.current

    val typography: GoigoiTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalGoigoiTypography.current
}

@Composable
fun GoigoiTheme(useFixedDarkModeAndAvoidAccessingSingletons: Boolean = false, content: @Composable () -> Unit) {
    val darkTheme = if (useFixedDarkModeAndAvoidAccessingSingletons) true else Singletons.prefs.darkMode
    val customScheme = if (darkTheme) darkGoigoiScheme else lightGoigoiScheme
    val materialScheme = if (darkTheme) darkMaterialScheme else lightMaterialScheme

    CompositionLocalProvider(
        LocalGoigoiColours provides customScheme,
        LocalGoigoiTypography provides customTypography
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = materialTypography,
            content = content
        )
    }
}
