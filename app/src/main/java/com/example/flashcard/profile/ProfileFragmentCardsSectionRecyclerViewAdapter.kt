package com.example.flashcard.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableSpaceRepetitionBox
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.google.android.material.card.MaterialCardView


class ProfileFragmentCardsSectionRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<ImmutableCard?>,
    private val boxLevels: List<ImmutableSpaceRepetitionBox>,
) : RecyclerView.Adapter<ProfileFragmentCardsSectionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            boxLevels,
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var isCardRevealed = false

        private val onCardText: TextView = view.findViewById(R.id.tv_card_content)
        private val cardDescription: TextView = view.findViewById(R.id.tv_card_description1)
        private val cardStatus: TextView = view.findViewById(R.id.tv_card_status)
        private val popUpBT: Button = view.findViewById(R.id.pupUpBT)
        private val cardRoot: MaterialCardView = view.findViewById(R.id.card_root)

        private val ICON_MARGIN = 5

        fun bind(
            context: Context,
            card: ImmutableCard?,
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            ) {
            popUpBT.visibility = View.GONE
            //val statusColor = SpaceRepetitionAlgorithmHelper().box[card.cardStatus]?.color
            val actualBoxLevel = SpaceRepetitionAlgorithmHelper().getBoxLevelByStatus(boxLevels, card?.cardStatus!!)
            val statusColor = SpaceRepetitionAlgorithmHelper().selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val colorStateList = ContextCompat.getColorStateList(context, statusColor!!)
            cardStatus.apply {
                text = card.cardStatus
                backgroundTintList = colorStateList
            }
            onCardText.text = card.cardContent?.content
            cardDescription.text = card.cardDefinition?.first()?.definition
            cardRoot.setOnClickListener {
                //flipCard(card, deck)
            }

        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_card_fragment_item, parent, false)
                return ViewHolder(view)
            }
        }
    }

}