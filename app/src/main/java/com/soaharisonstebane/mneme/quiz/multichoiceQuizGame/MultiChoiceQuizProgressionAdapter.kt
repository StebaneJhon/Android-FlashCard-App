package com.soaharisonstebane.mneme.quiz.multichoiceQuizGame

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.soaharisonstebane.mneme.R

class MultiChoiceQuizProgressionAdapter(
    val cardList: List<MultiChoiceGameCardModel>,
    val appTheme: String,
    val context: Context,
    val recyclerView: RecyclerView
) : RecyclerView.Adapter<MultiChoiceQuizProgressionAdapter.MultiChoiceQuizProgressionAdapterViewHolder>() {

//    companion object {
//        private const val MARGIN = 6
//    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultiChoiceQuizProgressionAdapterViewHolder {

//        val pickerItemWidth = parent.width / cardList.size - (2 * MARGIN)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_item_progression_single_bar, parent, false)
//        val layoutParams =
//            view.findViewById<MaterialCardView>(R.id.cv_item_progression_single_bar).layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.width = pickerItemWidth
//        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)

        return MultiChoiceQuizProgressionAdapterViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(
        holder: MultiChoiceQuizProgressionAdapterViewHolder,
        position: Int
    ) {
        val spacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, recyclerView.context.resources.displayMetrics
        ).toInt()
        val itemCount = maxOf(1, cardList.size)
        val recyclerViewWidth = recyclerView.width
        val totalSpacing = spacingPx * (itemCount - 1)


        val itemWidth = (recyclerViewWidth - totalSpacing) / itemCount

        val layoutParams = RecyclerView.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT)

        if (position < itemCount - 1) {
            layoutParams.rightMargin = spacingPx
        }
        holder.itemView.layoutParams = layoutParams

//        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
//        holder.itemView.layoutParams = RecyclerView.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT)

        return holder.bind(
            cardList[position],
//            appTheme,
            context,
//            recyclerView
        )
    }

    inner class MultiChoiceQuizProgressionAdapterViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val cardItem = view.findViewById<MaterialCardView>(R.id.cv_item_progression_single_bar)

        fun bind(
            card: MultiChoiceGameCardModel,
//            appTheme: String,
            context: Context,
//            recyclerView: RecyclerView
        ) {

//            val displayMetrics = Resources.getSystem().displayMetrics

            val cardItemColor = if (!card.isActualOrPassed) {
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceContainer,
                    Color.GRAY
                )
            } else {
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceContainerHighest,
                    Color.GRAY
                )
            }

            cardItem.setCardBackgroundColor(cardItemColor)
        }

    }

}