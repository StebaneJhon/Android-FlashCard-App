package com.example.flashcard.settings

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.deckCategoryColorConst.WHITE
import com.example.flashcard.util.themeConst.DARK_THEME
import com.example.flashcard.util.themeConst.WHITE_THEME
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class ThemePickerAdapter(
    private val context: Context,
    private val listOfThemes: List<ThemeModel>,
    private val onThemeSelected: (ThemeModel) -> Unit
): RecyclerView.Adapter<ThemePickerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThemePickerAdapter.ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ThemePickerAdapter.ViewHolder, position: Int) {
        return holder.bind(
            context,
            listOfThemes[position],
            onThemeSelected
        )
    }

    override fun getItemCount(): Int {
        return listOfThemes.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val item: MaterialCardView = view.findViewById(R.id.cv_item_theme_picker)
        private val icon: ImageView = view.findViewById(R.id.im_on_theme_checked)

        fun bind(
            context: Context,
            theme: ThemeModel,
            onThemeSelected: (ThemeModel) -> Unit
        ) {
            item.apply {
                background.setTint(context.getColor(ThemePicker().getThemeBaseColor(theme.themeId) ?: R.color.white))
                setOnClickListener { onThemeSelected(theme) }
            }
            icon.isVisible = theme.isSelected
            if (icon.isVisible && theme.themeId == WHITE_THEME || theme.themeId == DARK_THEME) {
                icon.imageTintList = MaterialColors.getColorStateList(context, com.google.android.material.R.attr.colorOnSurface, context.getColorStateList(R.color.black))
            } else {
                icon.imageTintList = MaterialColors.getColorStateList(context, com.google.android.material.R.attr.colorSurfaceContainerLowest, context.getColorStateList(R.color.white))
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_item_theme_picker, parent, false)
                return ViewHolder(view)
            }
        }
    }
}