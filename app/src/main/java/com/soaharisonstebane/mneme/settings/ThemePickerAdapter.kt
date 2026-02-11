package com.soaharisonstebane.mneme.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.util.ThemePicker

class ThemePickerAdapter(
    private val context: Context,
    private val listOfThemes: List<ThemeModel>,
    private val onThemeSelected: (ThemeModel) -> Unit
): RecyclerView.Adapter<ThemePickerAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN = 4
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThemePickerAdapter.ViewHolder {

        val pickerItemWidth = parent.width / 6 - ( 2 * MARGIN)
        val view = LayoutInflater.from(context).inflate(R.layout.ly_item_theme_picker, parent, false)
        val layoutParams = view.findViewById<MaterialButton>(R.id.cv_item_theme_picker).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = pickerItemWidth
        layoutParams.width = pickerItemWidth
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)

        return ViewHolder(view)
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

        private val item: MaterialButton = view.findViewById(R.id.cv_item_theme_picker)

        fun bind(
            context: Context,
            theme: ThemeModel,
            onThemeSelected: (ThemeModel) -> Unit
        ) {
            item.apply {
                background.setTint(context.getColor(ThemePicker().getThemeBaseColor(theme.themeId) ?: R.color.white))
                setOnClickListener { onThemeSelected(theme) }
                icon = if (theme.isSelected) context.getDrawable(R.drawable.icon_check) else null
            }
        }
    }
}