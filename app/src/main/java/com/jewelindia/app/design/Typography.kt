// Port of: wholesaler ios/JewelIndia/Sources/Design/Typography.swift
package com.jewelindia.app.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R

enum class JewelWeight {
    UltraLight,
    Thin,
    Light,
    Regular,
    Medium,
    Semibold,
    Bold,
    Heavy,
    Black
}

object JewelFonts {
    val cirkaLight = Font(R.font.cirka_light, FontWeight.Light)
    val cirkaRegular = Font(R.font.cirka_regular, FontWeight.Normal)
    val cirkaBold = Font(R.font.cirka_bold, FontWeight.Bold)
    val Cirka = FontFamily(cirkaLight, cirkaRegular, cirkaBold)

    val manropeExtraLight = Font(R.font.manrope_extralight, FontWeight.ExtraLight)
    val manropeLight = Font(R.font.manrope_light, FontWeight.Light)
    val manropeRegular = Font(R.font.manrope_regular, FontWeight.Normal)
    val manropeMedium = Font(R.font.manrope_medium, FontWeight.Medium)
    val manropeSemiBold = Font(R.font.manrope_semibold, FontWeight.SemiBold)
    val manropeBold = Font(R.font.manrope_bold, FontWeight.Bold)
    val manropeExtraBold = Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
    val Manrope = FontFamily(
        manropeExtraLight, manropeLight, manropeRegular,
        manropeMedium, manropeSemiBold, manropeBold, manropeExtraBold
    )

    val gilroyRegular = Font(R.font.gilroy_regular, FontWeight.Normal)
    val gilroyMedium = Font(R.font.gilroy_medium, FontWeight.Medium)
    val gilroySemiBold = Font(R.font.gilroy_semibold, FontWeight.SemiBold)
    val gilroyBold = Font(R.font.gilroy_bold, FontWeight.Bold)
    val Gilroy = FontFamily(gilroyRegular, gilroyMedium, gilroySemiBold, gilroyBold)

    val satoshiLight = Font(R.font.satoshi_light, FontWeight.Light)
    val satoshiRegular = Font(R.font.satoshi_regular, FontWeight.Normal)
    val satoshiMedium = Font(R.font.satoshi_medium, FontWeight.Medium)
    val satoshiBold = Font(R.font.satoshi_bold, FontWeight.Bold)
    val satoshiBlack = Font(R.font.satoshi_black, FontWeight.Black)
    val Satoshi = FontFamily(satoshiLight, satoshiRegular, satoshiMedium, satoshiBold, satoshiBlack)

    val gildaDisplay = Font(R.font.gilda_display_regular, FontWeight.Normal)
    val GildaDisplay = FontFamily(gildaDisplay)

    val employeeMatterRegular = Font(R.font.employee_matter_regular, FontWeight.Normal)
    val employeeMatterMedium = Font(R.font.employee_matter_medium, FontWeight.Medium)
    val employeeMatterBold = Font(R.font.employee_matter_bold, FontWeight.Bold)
    val EmployeeMatter = FontFamily(employeeMatterRegular, employeeMatterMedium, employeeMatterBold)

    val employeeSeasonMix = Font(R.font.employee_season_mix_regular, FontWeight.Normal)
    val EmployeeSeasonMix = FontFamily(employeeSeasonMix)
}

enum class RoleTheme {
    Wholesaler,
    Retailer,
    Employee;

    val headingFamily: FontFamily
        get() = when (this) {
            Wholesaler -> JewelFonts.Cirka
            Retailer -> JewelFonts.Satoshi
            Employee -> JewelFonts.GildaDisplay
        }

    val bodyFamily: FontFamily
        get() = when (this) {
            Wholesaler -> JewelFonts.Manrope
            Retailer -> JewelFonts.Satoshi
            Employee -> JewelFonts.Manrope
        }
}

val LocalRoleTheme = staticCompositionLocalOf { RoleTheme.Wholesaler }

@Suppress("DEPRECATION")
private val DefaultPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

fun cirka(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle {
    val (font, fw) = when (weight) {
        JewelWeight.Light, JewelWeight.UltraLight, JewelWeight.Thin ->
            JewelFonts.cirkaLight to FontWeight.Light
        JewelWeight.Bold, JewelWeight.Heavy, JewelWeight.Black, JewelWeight.Semibold ->
            JewelFonts.cirkaBold to FontWeight.Bold
        else ->
            JewelFonts.cirkaRegular to FontWeight.Normal
    }
    return TextStyle(
        fontFamily = FontFamily(font),
        fontWeight = fw,
        fontSize = size.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = DefaultPlatformTextStyle
    )
}

fun manrope(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle {
    val (font, fw) = when (weight) {
        JewelWeight.UltraLight, JewelWeight.Thin ->
            JewelFonts.manropeExtraLight to FontWeight.ExtraLight
        JewelWeight.Light ->
            JewelFonts.manropeLight to FontWeight.Light
        JewelWeight.Medium ->
            JewelFonts.manropeMedium to FontWeight.Medium
        JewelWeight.Semibold ->
            JewelFonts.manropeSemiBold to FontWeight.SemiBold
        JewelWeight.Bold ->
            JewelFonts.manropeBold to FontWeight.Bold
        JewelWeight.Heavy, JewelWeight.Black ->
            JewelFonts.manropeExtraBold to FontWeight.ExtraBold
        else ->
            JewelFonts.manropeRegular to FontWeight.Normal
    }
    return TextStyle(
        fontFamily = FontFamily(font),
        fontWeight = fw,
        fontSize = size.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = DefaultPlatformTextStyle
    )
}

fun gilroy(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle {
    val (font, fw) = when (weight) {
        JewelWeight.Medium ->
            JewelFonts.gilroyMedium to FontWeight.Medium
        JewelWeight.Semibold ->
            JewelFonts.gilroySemiBold to FontWeight.SemiBold
        JewelWeight.Bold, JewelWeight.Heavy, JewelWeight.Black ->
            JewelFonts.gilroyBold to FontWeight.Bold
        else ->
            JewelFonts.gilroyRegular to FontWeight.Normal
    }
    return TextStyle(
        fontFamily = FontFamily(font),
        fontWeight = fw,
        fontSize = size.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = DefaultPlatformTextStyle
    )
}

fun satoshi(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle {
    val (font, fw) = when (weight) {
        JewelWeight.UltraLight, JewelWeight.Thin, JewelWeight.Light ->
            JewelFonts.satoshiLight to FontWeight.Light
        JewelWeight.Medium, JewelWeight.Semibold ->
            JewelFonts.satoshiMedium to FontWeight.Medium
        JewelWeight.Bold ->
            JewelFonts.satoshiBold to FontWeight.Bold
        JewelWeight.Heavy, JewelWeight.Black ->
            JewelFonts.satoshiBlack to FontWeight.Black
        else ->
            JewelFonts.satoshiRegular to FontWeight.Normal
    }
    return TextStyle(
        fontFamily = FontFamily(font),
        fontWeight = fw,
        fontSize = size.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = DefaultPlatformTextStyle
    )
}

fun gilda(size: Float): TextStyle {
    return TextStyle(
        fontFamily = JewelFonts.GildaDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = size.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = DefaultPlatformTextStyle
    )
}

fun sfPro(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle {
    val fw = when (weight) {
        JewelWeight.UltraLight, JewelWeight.Thin -> FontWeight.Thin
        JewelWeight.Light -> FontWeight.Light
        JewelWeight.Regular -> FontWeight.Normal
        JewelWeight.Medium -> FontWeight.Medium
        JewelWeight.Semibold -> FontWeight.SemiBold
        JewelWeight.Bold -> FontWeight.Bold
        JewelWeight.Heavy -> FontWeight.ExtraBold
        JewelWeight.Black -> FontWeight.Black
    }
    return TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fw,
        fontSize = size.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = DefaultPlatformTextStyle
    )
}

object Typography {
    val Cirka: FontFamily get() = JewelFonts.Cirka
    val Manrope: FontFamily get() = JewelFonts.Manrope
    val Satoshi: FontFamily get() = JewelFonts.Satoshi
    val Gilroy: FontFamily get() = JewelFonts.Gilroy
    val GildaDisplay: FontFamily get() = JewelFonts.GildaDisplay
    val EmployeeMatter: FontFamily get() = JewelFonts.EmployeeMatter

    fun cirka(size: TextUnit, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.cirka(size.value, weight)

    fun cirka(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.cirka(size, weight)

    fun manrope(size: TextUnit, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.manrope(size.value, weight)

    fun manrope(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.manrope(size, weight)

    fun satoshi(size: TextUnit, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.satoshi(size.value, weight)

    fun satoshi(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.satoshi(size, weight)

    fun santoshi(size: TextUnit, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.satoshi(size.value, weight)

    fun santoshi(size: Float, weight: JewelWeight = JewelWeight.Regular): TextStyle =
        com.jewelindia.app.design.satoshi(size, weight)
}

