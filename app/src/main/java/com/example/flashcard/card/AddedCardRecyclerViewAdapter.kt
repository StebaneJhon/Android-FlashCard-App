package com.example.flashcard.card

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardDefinition
import com.google.android.material.card.MaterialCardView

class AddedCardRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<ImmutableCard?>,
    private val deck: ImmutableDeck,
    private val cardRootClickListener: (ImmutableCard?) -> Unit,
    private val deleteCardClickListener: (ImmutableCard?) -> Unit,
    private val editCardClickListener: (ImmutableCard?) -> Unit,
) : RecyclerView.Adapter<AddedCardRecyclerViewAdapter.ViewHolder>() {

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
            deck,
            cardRootClickListener,
            deleteCardClickListener,
            editCardClickListener
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cardRoot: MaterialCardView = view.findViewById(R.id.cv_on_add_new_card_item_root)
        private val tvAddedCardContent: TextView = view.findViewById(R.id.tv_added_card_content)
        private val tvAddedCardDescription1: TextView = view.findViewById(R.id.tv_added_card_description1)
        private val tvAddedCardDescription2: TextView = view.findViewById(R.id.tv_added_card_description2)
        private val tvAddedCardDescription3: TextView = view.findViewById(R.id.tv_added_card_description3)
        private val tvAddedCardDescription4: TextView = view.findViewById(R.id.tv_added_card_description4)
        private val tvAddedCardDescriptionError: TextView = view.findViewById(R.id.tv_added_card_description_error)

        fun bind(
            context: Context,
            card: ImmutableCard?,
            deck: ImmutableDeck,
            cardRootClickListener: (ImmutableCard?) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
            editCardClickListener: (ImmutableCard?) -> Unit
        ) {

            tvAddedCardContent.text = card?.cardContent?.content

            val definitionTexts = cardDefinitionsToStrings(card?.cardDefinition)

            when (definitionTexts.size) {
                1 -> {
                    tvAddedCardDescriptionError.visibility = View.GONE
                    displayCardDefinition(context, tvAddedCardDescription1, card?.cardDefinition?.get(0))
                    tvAddedCardDescription2.visibility = View.GONE
                    tvAddedCardDescription3.visibility = View.GONE
                    tvAddedCardDescription4.visibility = View.GONE
                }
                2 -> {
                    tvAddedCardDescriptionError.visibility = View.GONE
                    displayCardDefinition(context, tvAddedCardDescription1, card?.cardDefinition?.get(0))
                    displayCardDefinition(context, tvAddedCardDescription2, card?.cardDefinition?.get(1))
                    tvAddedCardDescription3.visibility = View.GONE
                    tvAddedCardDescription4.visibility = View.GONE
                }
                3 -> {
                    tvAddedCardDescriptionError.visibility = View.GONE
                    displayCardDefinition(context, tvAddedCardDescription1, card?.cardDefinition?.get(0))
                    displayCardDefinition(context, tvAddedCardDescription2, card?.cardDefinition?.get(1))
                    displayCardDefinition(context, tvAddedCardDescription3, card?.cardDefinition?.get(2))
                    tvAddedCardDescription4.visibility = View.GONE
                }
                4 -> {
                    tvAddedCardDescriptionError.visibility = View.GONE
                    displayCardDefinition(context, tvAddedCardDescription1, card?.cardDefinition?.get(0))
                    displayCardDefinition(context, tvAddedCardDescription2, card?.cardDefinition?.get(1))
                    displayCardDefinition(context, tvAddedCardDescription3, card?.cardDefinition?.get(2))
                    displayCardDefinition(context, tvAddedCardDescription4, card?.cardDefinition?.get(3))
                }
                else -> {
                    tvAddedCardDescriptionError.visibility = View.VISIBLE
                    tvAddedCardDescription1.visibility = View.GONE
                    tvAddedCardDescription2.visibility = View.GONE
                    tvAddedCardDescription3.visibility = View.GONE
                    tvAddedCardDescription4.visibility = View.GONE
                }
            }

        }

        @SuppressLint("ResourceType")
        fun displayCardDefinition(context: Context, view: TextView, definition: CardDefinition?) {
            definition?.let {
                view.visibility = View.VISIBLE
                if (it.isCorrectDefinition == true) {
                    val textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.green100)
                    view.backgroundTintList = textBackgroundColorStateList
                    view.text = it.definition
                } else {
//                    val textBackgroundColorStateList = ContextCompat.getColorStateList(context, com.google.android.material.R.attr.colorSurfaceContainerLow)
//                    view.backgroundTintList = textBackgroundColorStateList
                    view.text = it.definition
                }
            }

        }

        private fun cardDefinitionsToStrings(cardDefinitions: List<CardDefinition>?): List<String> {
            val definitionStrings = mutableListOf<String>()
            cardDefinitions?.let { defins ->
                defins.forEach {
                    definitionStrings.add(it.definition!!)
                }
            }
            return definitionStrings
        }

        private fun getCorrectDefinition(definitions: List<CardDefinition>?): List<CardDefinition>? {
            definitions?.let { defins ->
                return defins.filter { it.isCorrectDefinition!! }
            }
            return null
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_item_card_on_add_new_card, parent, false)
                return ViewHolder(view)
            }
        }

    }

}