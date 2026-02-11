package com.ssoaharison.recall.helper

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckCategoryColorConst
import kotlin.collections.getOrDefault

class DeckColorCategorySelector {


    private val deckColorSurfaceLow = mapOf(
//        WHITE to R.color.neutral100,
        DeckCategoryColorConst.GREY to R.color.neutral100,
//        BLACK to R.color.neutral950,
        DeckCategoryColorConst.RED to R.color.red100,
        DeckCategoryColorConst.ORANGE to R.color.orange100,
        DeckCategoryColorConst.YELLOW to R.color.yellow100,
        DeckCategoryColorConst.LIME to R.color.lime100,
        DeckCategoryColorConst.GREEN to R.color.green100,
        DeckCategoryColorConst.EMERALD to R.color.emerald100,
        DeckCategoryColorConst.TEAL to R.color.teal100,
        DeckCategoryColorConst.CYAN to R.color.cyan100,
        DeckCategoryColorConst.SKY to R.color.sky100,
        DeckCategoryColorConst.BLUE to R.color.blue100,
        DeckCategoryColorConst.INDIGO to R.color.indigo100,
        DeckCategoryColorConst.VIOLET to R.color.violet100,
        DeckCategoryColorConst.PURPLE to R.color.purple100,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia100,
        DeckCategoryColorConst.PINK to R.color.pink100,
        DeckCategoryColorConst.ROSE to R.color.rose100,
    )

    private val deckColorSurfaceContainer = mapOf(
//        WHITE to R.color.neutral100,
        DeckCategoryColorConst.GREY to R.color.neutral200,
//        BLACK to R.color.neutral950,
        DeckCategoryColorConst.RED to R.color.red200,
        DeckCategoryColorConst.ORANGE to R.color.orange200,
        DeckCategoryColorConst.YELLOW to R.color.yellow200,
        DeckCategoryColorConst.LIME to R.color.lime200,
        DeckCategoryColorConst.GREEN to R.color.green200,
        DeckCategoryColorConst.EMERALD to R.color.emerald200,
        DeckCategoryColorConst.TEAL to R.color.teal200,
        DeckCategoryColorConst.CYAN to R.color.cyan200,
        DeckCategoryColorConst.SKY to R.color.sky200,
        DeckCategoryColorConst.BLUE to R.color.blue200,
        DeckCategoryColorConst.INDIGO to R.color.indigo200,
        DeckCategoryColorConst.VIOLET to R.color.violet200,
        DeckCategoryColorConst.PURPLE to R.color.purple200,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia200,
        DeckCategoryColorConst.PINK to R.color.pink200,
        DeckCategoryColorConst.ROSE to R.color.rose200,
    )

    private val deckDarkColorSurfaceLow = mapOf(
//        WHITE to R.color.neutral900,
        DeckCategoryColorConst.GREY to R.color.neutral900,
//        BLACK to R.color.neutral700,
        DeckCategoryColorConst.RED to R.color.red900,
        DeckCategoryColorConst.ORANGE to R.color.orange900,
        DeckCategoryColorConst.YELLOW to R.color.yellow900,
        DeckCategoryColorConst.LIME to R.color.lime900,
        DeckCategoryColorConst.GREEN to R.color.green900,
        DeckCategoryColorConst.EMERALD to R.color.emerald900,
        DeckCategoryColorConst.TEAL to R.color.teal900,
        DeckCategoryColorConst.CYAN to R.color.cyan900,
        DeckCategoryColorConst.SKY to R.color.sky900,
        DeckCategoryColorConst.BLUE to R.color.blue900,
        DeckCategoryColorConst.INDIGO to R.color.indigo900,
        DeckCategoryColorConst.VIOLET to R.color.violet900,
        DeckCategoryColorConst.PURPLE to R.color.purple900,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia900,
        DeckCategoryColorConst.PINK to R.color.pink900,
        DeckCategoryColorConst.ROSE to R.color.rose900,
    )

    private val deckDarkColorSurfaceContainer = mapOf(
//        WHITE to R.color.neutral100,
        DeckCategoryColorConst.GREY to R.color.neutral900,
//        BLACK to R.color.neutral950,
        DeckCategoryColorConst.RED to R.color.red900,
        DeckCategoryColorConst.ORANGE to R.color.orange900,
        DeckCategoryColorConst.YELLOW to R.color.yellow900,
        DeckCategoryColorConst.LIME to R.color.lime900,
        DeckCategoryColorConst.GREEN to R.color.green900,
        DeckCategoryColorConst.EMERALD to R.color.emerald900,
        DeckCategoryColorConst.TEAL to R.color.teal900,
        DeckCategoryColorConst.CYAN to R.color.cyan900,
        DeckCategoryColorConst.SKY to R.color.sky900,
        DeckCategoryColorConst.BLUE to R.color.blue900,
        DeckCategoryColorConst.INDIGO to R.color.indigo900,
        DeckCategoryColorConst.VIOLET to R.color.violet900,
        DeckCategoryColorConst.PURPLE to R.color.purple900,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia900,
        DeckCategoryColorConst.PINK to R.color.pink900,
        DeckCategoryColorConst.ROSE to R.color.rose900,
    )

    private val deckColorOnSurface = mapOf(
//        WHITE to R.color.neutral50,
        DeckCategoryColorConst.GREY to R.color.neutral950,
//        BLACK to R.color.neutral950,
        DeckCategoryColorConst.RED to R.color.red950,
        DeckCategoryColorConst.ORANGE to R.color.orange950,
        DeckCategoryColorConst.YELLOW to R.color.yellow950,
        DeckCategoryColorConst.LIME to R.color.lime950,
        DeckCategoryColorConst.GREEN to R.color.green950,
        DeckCategoryColorConst.EMERALD to R.color.emerald950,
        DeckCategoryColorConst.TEAL to R.color.teal950,
        DeckCategoryColorConst.CYAN to R.color.cyan950,
        DeckCategoryColorConst.SKY to R.color.sky950,
        DeckCategoryColorConst.BLUE to R.color.blue950,
        DeckCategoryColorConst.INDIGO to R.color.indigo950,
        DeckCategoryColorConst.VIOLET to R.color.violet950,
        DeckCategoryColorConst.PURPLE to R.color.purple950,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia950,
        DeckCategoryColorConst.PINK to R.color.pink950,
        DeckCategoryColorConst.ROSE to R.color.rose950,
    )

    private val deckDarkColorOnSurface = mapOf(
//        WHITE to R.color.neutral950,
        DeckCategoryColorConst.GREY to R.color.neutral50,
//        BLACK to R.color.neutral50,
        DeckCategoryColorConst.RED to R.color.red50,
        DeckCategoryColorConst.ORANGE to R.color.orange50,
        DeckCategoryColorConst.YELLOW to R.color.yellow50,
        DeckCategoryColorConst.LIME to R.color.lime50,
        DeckCategoryColorConst.GREEN to R.color.green50,
        DeckCategoryColorConst.EMERALD to R.color.emerald50,
        DeckCategoryColorConst.TEAL to R.color.teal50,
        DeckCategoryColorConst.CYAN to R.color.cyan50,
        DeckCategoryColorConst.SKY to R.color.sky50,
        DeckCategoryColorConst.BLUE to R.color.blue50,
        DeckCategoryColorConst.INDIGO to R.color.indigo50,
        DeckCategoryColorConst.VIOLET to R.color.violet50,
        DeckCategoryColorConst.PURPLE to R.color.purple50,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia50,
        DeckCategoryColorConst.PINK to R.color.pink50,
        DeckCategoryColorConst.ROSE to R.color.rose50,
    )

    private val deckColorSurfaceLowEst = mapOf(
//        WHITE to R.color.neutral50,
        DeckCategoryColorConst.GREY to R.color.neutral50,
//        BLACK to R.color.neutral950,
        DeckCategoryColorConst.RED to R.color.red50,
        DeckCategoryColorConst.ORANGE to R.color.orange50,
        DeckCategoryColorConst.YELLOW to R.color.yellow50,
        DeckCategoryColorConst.LIME to R.color.lime50,
        DeckCategoryColorConst.GREEN to R.color.green50,
        DeckCategoryColorConst.EMERALD to R.color.emerald50,
        DeckCategoryColorConst.TEAL to R.color.teal50,
        DeckCategoryColorConst.CYAN to R.color.cyan50,
        DeckCategoryColorConst.SKY to R.color.sky50,
        DeckCategoryColorConst.BLUE to R.color.blue50,
        DeckCategoryColorConst.INDIGO to R.color.indigo50,
        DeckCategoryColorConst.VIOLET to R.color.violet50,
        DeckCategoryColorConst.PURPLE to R.color.purple50,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia50,
        DeckCategoryColorConst.PINK to R.color.pink50,
        DeckCategoryColorConst.ROSE to R.color.rose50,
    )

    private val deckDarkColorSurfaceLowEst = mapOf(
//        WHITE to R.color.neutral950,
        DeckCategoryColorConst.GREY to R.color.neutral950,
//        BLACK to R.color.neutral950,
        DeckCategoryColorConst.RED to R.color.red950,
        DeckCategoryColorConst.ORANGE to R.color.orange950,
        DeckCategoryColorConst.YELLOW to R.color.yellow950,
        DeckCategoryColorConst.LIME to R.color.lime950,
        DeckCategoryColorConst.GREEN to R.color.green950,
        DeckCategoryColorConst.EMERALD to R.color.emerald950,
        DeckCategoryColorConst.TEAL to R.color.teal950,
        DeckCategoryColorConst.CYAN to R.color.cyan950,
        DeckCategoryColorConst.SKY to R.color.sky950,
        DeckCategoryColorConst.BLUE to R.color.blue950,
        DeckCategoryColorConst.INDIGO to R.color.indigo950,
        DeckCategoryColorConst.VIOLET to R.color.violet950,
        DeckCategoryColorConst.PURPLE to R.color.purple950,
        DeckCategoryColorConst.FUCHSIA to R.color.fuchsia950,
        DeckCategoryColorConst.PINK to R.color.pink950,
        DeckCategoryColorConst.ROSE to R.color.rose950,
    )

    fun getColors() = this.deckColorSurfaceLow

    fun getDarkColors() = this.deckDarkColorSurfaceLow

    fun getRandomColor() = this.deckColorSurfaceLow.keys.random()

    fun selectDeckColorSurfaceContainerLow(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckColorSurfaceLow.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLow))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorSurfaceContainer(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceContainer.keys) {
            ContextCompat.getColor(context, this.deckColorSurfaceContainer.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainer))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckDarkColorSurfaceContainerLow(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckDarkColorSurfaceLow.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLow))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckDarkColorSurfaceContainer(context: Context, color: String?): Int {
        return if (color in this.deckDarkColorSurfaceContainer.keys) {
            ContextCompat.getColor(context, this.deckDarkColorSurfaceContainer.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainer))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorStateListSurfaceContainerLow(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckColorSurfaceLow.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLow))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckDarkColorStateListSurfaceContainerLow(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckDarkColorSurfaceLow.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLow))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorSurfaceContainerLowEst(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckColorSurfaceLowEst.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLowest))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLowest, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckDarkColorSurfaceContainerLowEst(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLowEst.keys) {
            ContextCompat.getColor(context, this.deckDarkColorSurfaceLowEst.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLowest))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLowest, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorStateListSurfaceContainerLowEst(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckColorSurfaceLowEst.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLowest))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLowest, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckDarkColorStateListSurfaceContainerLowEst(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckDarkColorSurfaceLowEst.getOrDefault(color, com.google.android.material.R.attr.colorSurfaceContainerLowest))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLowest, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckOnSurfaceColor(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckColorOnSurface.getOrDefault(color, com.google.android.material.R.attr.colorOnSurface))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckOnSurfaceColorDark(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckDarkColorOnSurface.getOrDefault(color, com.google.android.material.R.attr.colorOnSurface))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckOnSurfaceColorStateList(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckColorOnSurface.getOrDefault(color, com.google.android.material.R.attr.colorOnSurface))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckOnSurfaceColorDarkStateList(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckDarkColorOnSurface.getOrDefault(color, com.google.android.material.R.attr.colorOnSurface))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

}