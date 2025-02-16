package com.ssoaharison.recall.deck

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R

class DeckColorPickerAdapter(
    private val context: Context,
    private val listOfColors: List<ColorModel>,
    private val onColorClicked: (ColorModel) -> Unit
): RecyclerView.Adapter<DeckColorPickerAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN = 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeckColorPickerAdapter.ViewHolder {

        val pickerItemWidth = parent.width / 6 - ( 2 * MARGIN)
        val view = LayoutInflater.from(context).inflate(R.layout.ly_item_color_picker, parent, false)
        val layoutParams = view.findViewById<MaterialButton>(R.id.cv_item_color_picker).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = pickerItemWidth
        layoutParams.width = pickerItemWidth
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckColorPickerAdapter.ViewHolder, position: Int) {
        return holder.bind(
            context,
            listOfColors[position],
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
            onColorClicked: (ColorModel) -> Unit
        ) {

            item.apply {
                background.setTint(context.getColor(color.color))
                setOnClickListener { onColorClicked(color) }
                icon = if (color.isSelected) context.getDrawable(R.drawable.icon_check) else null
            }
        }
    }

}