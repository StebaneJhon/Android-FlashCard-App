package com.ssoaharison.recall.deck

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.ssoaharison.recall.util.DeckCategoryColorConst.WHITE
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class DeckColorPickerAdapter(
    private val context: Context,
    private val listOfColors: List<ColorModel>,
    private val onColorClicked: (ColorModel) -> Unit
): RecyclerView.Adapter<DeckColorPickerAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN = 5
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeckColorPickerAdapter.ViewHolder {

        val pickerItemWidth = parent.width / 6 - ( 2 * MARGIN)
        val view = LayoutInflater.from(context).inflate(R.layout.ly_item_color_picker, parent, false)
        val layoutParams = view.findViewById<MaterialCardView>(R.id.cv_item_color_picker).layoutParams as ViewGroup.MarginLayoutParams
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

        private val item: MaterialCardView = view.findViewById(R.id.cv_item_color_picker)
        private val icon: ImageView = view.findViewById(R.id.im_on_color_checked)
        private val colorPickerHelper = DeckColorCategorySelector()

        fun bind(
            context: Context,
            color: ColorModel,
            onColorClicked: (ColorModel) -> Unit
        ) {

            item.apply {
                background.setTint(context.getColor(color.color))
                setOnClickListener { onColorClicked(color) }
            }
            icon.isVisible = color.isSelected
            if (icon.isVisible && color.id == WHITE) {
                icon.imageTintList = MaterialColors.getColorStateList(context, com.google.android.material.R.attr.colorOnSurface, context.getColorStateList(R.color.black))
            } else {
                icon.imageTintList = MaterialColors.getColorStateList(context, com.google.android.material.R.attr.colorSurfaceContainerLowest, context.getColorStateList(R.color.white))
            }
        }
    }

}