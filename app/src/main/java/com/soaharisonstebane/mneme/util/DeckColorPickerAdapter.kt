package com.soaharisonstebane.mneme.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.soaharisonstebane.mneme.R

class DeckColorPickerAdapter(
    private val context: Context,
    private val listOfColors: List<ColorModel>,
    private val isItemsClickable: Boolean,
    private val onColorClicked: (ColorModel) -> Unit
): RecyclerView.Adapter<DeckColorPickerAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN = 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val pickerItemWidth = parent.width / 6 - ( 2 * MARGIN)
        val view = LayoutInflater.from(context).inflate(R.layout.ly_item_color_picker, parent, false)
        val layoutParams = view.findViewById<MaterialButton>(R.id.cv_item_color_picker).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = pickerItemWidth
        layoutParams.width = pickerItemWidth
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            context,
            listOfColors[position],
            isItemsClickable,
            onColorClicked
        )
    }

    override fun getItemCount(): Int {
        return listOfColors.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val item: MaterialButton = view.findViewById(R.id.cv_item_color_picker)

        fun bind(
            context: Context,
            color: ColorModel,
            isItemsClickable: Boolean,
            onColorClicked: (ColorModel) -> Unit
        ) {

            item.apply {
                background.setTint(context.getColor(color.color))
                setOnClickListener {
                    if (isItemsClickable) {
                        onColorClicked(color)
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_message_cannot_edit_main_deck_background), Toast.LENGTH_SHORT).show()
                    }

                }
                icon = if (color.isSelected) context.getDrawable(R.drawable.icon_check) else null
            }
        }
    }

}