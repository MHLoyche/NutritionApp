package com.example.nutritionapptwo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    // Make all text / content colors black
    onPrimary = TextBlack,
    onSecondary = TextBlack,
    onTertiary = TextBlack,
    onBackground = TextBlack,
    onSurface = TextBlack,
    onError = TextBlack
)

@Composable
fun NutritionAppTwoTheme(
    // Force light theme & fixed colors
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}