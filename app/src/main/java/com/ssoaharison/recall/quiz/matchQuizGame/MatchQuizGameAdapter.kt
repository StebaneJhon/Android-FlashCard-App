package com.ssoaharison.recall.quiz.matchQuizGame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.Model.MatchQuizGameItemModel
import com.google.android.material.card.MaterialCardView

class MatchQuizGameAdapter(
    private val context: Context,
    private val itemList: List<MatchQuizGameItemModel>,
    private val boardSize: MatchQuizGameBorderSize,
    private val flipCard: (MatchingQuizGameSelectedItemInfo) -> Unit
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
        val view = LayoutInflater.from(context).inflate(R.layout.ly_matching_quiz_game_item, parent, false)
        val layoutParams = view.findViewById<MaterialCardView>(R.id.cv_item_root).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = cardHeight
        layoutParams.width = cardWidth
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(context, itemList[position], flipCard)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val tvItemActive: TextView = view.findViewById(R.id.tv_item_text_card_active)
        private val tvItemInactive: TextView = view.findViewById(R.id.tv_item_text_card_inactive)
        private val tvItemWrong: TextView = view.findViewById(R.id.tv_item_text_card_wrong)
        private val llItemContainerActive: LinearLayout = view.findViewById(R.id.ll_card_active)
        private val llItemContainerInactive: LinearLayout = view.findViewById(R.id.ll_card_inactive)
        private val llItemContainerWrong: LinearLayout = view.findViewById(R.id.ll_card_wrong)
        private val cvItemContainer: MaterialCardView = view.findViewById(R.id.cv_item_root)

        fun bind(
            context: Context,
            item: MatchQuizGameItemModel,
            flipCard: (MatchingQuizGameSelectedItemInfo) -> Unit,
        ) {
            tvItemActive.text = item.text
            tvItemInactive.text = item.text
            tvItemWrong.text = item.text

            cvItemContainer.setOnClickListener {
                val itemInfo = MatchingQuizGameSelectedItemInfo(
                    item,
                    cvItemContainer,
                    llItemContainerActive,
                    llItemContainerInactive,
                    llItemContainerWrong
                )
                flipCard(itemInfo)
            }

        }
    }
}