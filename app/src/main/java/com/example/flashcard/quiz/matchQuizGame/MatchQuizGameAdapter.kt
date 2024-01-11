package com.example.flashcard.quiz.matchQuizGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.MatchQuizGameItemModel
import com.example.flashcard.util.MatchQuizGameBorderSize
import com.google.android.material.card.MaterialCardView

class MatchQuizGameAdapter(
    private val context: Context,
    private val itemList: List<MatchQuizGameItemModel>,
    private val boardSize: MatchQuizGameBorderSize,
    private val flipCard: (MatchQuizGameItemModel) -> Unit
): RecyclerView.Adapter<MatchQuizGameAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN_SIZE = 5
        private const val TAG = "MatchQuizGameAdapter"
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = cardWidth.coerceAtMost(cardHeight)
        val view = LayoutInflater.from(context).inflate(R.layout.ly_matching_quiz_game_item, parent, false)
        val layoutParams = view.findViewById<MaterialCardView>(R.id.cv_item_root).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(context, itemList[position], flipCard)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val tvItem: TextView = view.findViewById(R.id.tv_item_text)
        val cvItemContainer: MaterialCardView = view.findViewById(R.id.cv_item_root)

        fun bind(
            context: Context,
            item: MatchQuizGameItemModel,
            flipCard: (MatchQuizGameItemModel) -> Unit,
        ) {

            if (item.isMatched) {
                cvItemContainer.visibility = View.GONE
            } else {
                tvItem.text = item.text
            }
            cvItemContainer.setOnClickListener { flipCard(item) }

        }
    }


}