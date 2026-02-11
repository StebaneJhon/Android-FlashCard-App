package com.soaharisonstebane.mneme.quiz.quizGame

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

class QuizGameProgressBarAdapter(
    val cardList: List<QuizGameCardModel>,
    val context: Context,
    private val recyclerView: RecyclerView
): RecyclerView.Adapter<QuizGameProgressBarAdapter.QuizGameProgressBarAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuizGameProgressBarAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_item_progression_single_bar, parent, false)
        return QuizGameProgressBarAdapterViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: QuizGameProgressBarAdapterViewHolder, position: Int) {
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
        return holder.bind(
            cardList[position],
            context
        )
    }

    inner class QuizGameProgressBarAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val cardItem = view.findViewById<MaterialCardView>(R.id.cv_item_progression_single_bar)

        fun bind(
            card: QuizGameCardModel,
            context: Context
        ) {
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