package com.ssoaharison.recall.util

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckCategoryColorConst.BLACK
import com.ssoaharison.recall.util.DeckCategoryColorConst.BLUE
import com.ssoaharison.recall.util.DeckCategoryColorConst.CYAN
import com.ssoaharison.recall.util.DeckCategoryColorConst.EMERALD
import com.ssoaharison.recall.util.DeckCategoryColorConst.GREEN
import com.ssoaharison.recall.util.DeckCategoryColorConst.GREY
import com.ssoaharison.recall.util.DeckCategoryColorConst.INDIGO
import com.ssoaharison.recall.util.DeckCategoryColorConst.LIME
import com.ssoaharison.recall.util.DeckCategoryColorConst.PINK
import com.ssoaharison.recall.util.DeckCategoryColorConst.PURPLE
import com.ssoaharison.recall.util.DeckCategoryColorConst.RED
import com.ssoaharison.recall.util.DeckCategoryColorConst.ROSE
import com.ssoaharison.recall.util.DeckCategoryColorConst.SKY
import com.ssoaharison.recall.util.DeckCategoryColorConst.TEAL
import com.ssoaharison.recall.util.DeckCategoryColorConst.VIOLET
import com.ssoaharison.recall.util.DeckCategoryColorConst.WHITE
import com.ssoaharison.recall.util.DeckCategoryColorConst.YELLOW
import com.ssoaharison.recall.util.DeckCategoryColorConst.ORANGE
import com.ssoaharison.recall.util.DeckCategoryColorConst.FUCHSIA

class DeckColorCategorySelector {


    private val deckColorSurfaceLow = mapOf(
        WHITE to R.color.neutral100,
        GREY to R.color.neutral100,
        BLACK to R.color.neutral950,
        RED to R.color.red100,
        ORANGE to R.color.orange100,
        YELLOW to R.color.yellow100,
        LIME to R.color.lime100,
        GREEN to R.color.green100,
        EMERALD to R.color.emerald100,
        TEAL to R.color.teal100,
        CYAN to R.color.cyan100,
        SKY to R.color.sky100,
        BLUE to R.color.blue100,
        INDIGO to R.color.indigo100,
        VIOLET to R.color.violet100,
        PURPLE to R.color.purple100,
        FUCHSIA to R.color.fuchsia100,
        PINK to R.color.pink100,
        ROSE to R.color.rose100,
    )

    private val deckColorOnSurface = mapOf(
        WHITE to R.color.neutral50,
        GREY to R.color.neutral950,
        BLACK to R.color.neutral950,
        RED to R.color.red950,
        ORANGE to R.color.orange950,
        YELLOW to R.color.yellow950,
        LIME to R.color.lime950,
        GREEN to R.color.green950,
        EMERALD to R.color.emerald950,
        TEAL to R.color.teal950,
        CYAN to R.color.cyan950,
        SKY to R.color.sky950,
        BLUE to R.color.blue950,
        INDIGO to R.color.indigo950,
        VIOLET to R.color.violet950,
        PURPLE to R.color.purple950,
        FUCHSIA to R.color.fuchsia950,
        PINK to R.color.pink950,
        ROSE to R.color.rose950,
    )

    private val deckColorSurfaceLowEst = mapOf(
        WHITE to R.color.neutral50,
        GREY to R.color.neutral50,
        BLACK to R.color.neutral950,
        RED to R.color.red50,
        ORANGE to R.color.orange50,
        YELLOW to R.color.yellow50,
        LIME to R.color.lime50,
        GREEN to R.color.green50,
        EMERALD to R.color.emerald50,
        TEAL to R.color.teal50,
        CYAN to R.color.cyan50,
        SKY to R.color.sky50,
        BLUE to R.color.blue50,
        INDIGO to R.color.indigo50,
        VIOLET to R.color.violet50,
        PURPLE to R.color.purple50,
        FUCHSIA to R.color.fuchsia50,
        PINK to R.color.pink50,
        ROSE to R.color.rose50,
    )

    fun getColors() = this.deckColorSurfaceLow

    fun getRandomColor() = this.deckColorSurfaceLow.keys.random()

    fun selectDeckColorSurfaceContainerLow(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckColorSurfaceLow.getOrDefault(color, R.color.neutral100))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorStateListSurfaceContainerLow(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckColorSurfaceLow.getOrDefault(color, R.color.neutral100))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorSurfaceContainerLowEst(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckColorSurfaceLowEst.getOrDefault(color, R.color.neutral50))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLowest, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckColorStateListSurfaceContainerLowEst(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckColorSurfaceLowEst.getOrDefault(color, R.color.neutral50))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLowest, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckOnSurfaceColor(context: Context, color: String?): Int {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColor(context, this.deckColorOnSurface.getOrDefault(color, R.color.neutral950))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
        }
    }

    fun selectDeckOnSurfaceColorStateList(context: Context, color: String?): ColorStateList? {
        return if (color in this.deckColorSurfaceLow.keys) {
            ContextCompat.getColorStateList(context, this.deckColorOnSurface.getOrDefault(color, R.color.neutral950))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            ResourcesCompat.getColorStateList(context.resources, typedValue.resourceId, context.theme)
        }
    }

}

