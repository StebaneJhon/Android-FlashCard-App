package com.example.flashcard.card

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableSpaceRepetitionBox
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.google.android.material.card.MaterialCardView


class CardsRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<ImmutableCard?>,
    private val deck: ImmutableDeck,
    private val boxLevels: List<ImmutableSpaceRepetitionBox>,
    private val fullScreenClickListener: (ImmutableCard?) -> Unit,
    private val editCardClickListener: (ImmutableCard?) -> Unit,
    private val deleteCardClickListener: (ImmutableCard?) -> Unit,
    private val onCardContentClicked: (TextClickedModel) -> Unit,
    private val onCardDefinitionClicked: (TextClickedModel) -> Unit,
) : RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder>() {

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
            boxLevels,
            fullScreenClickListener,
            editCardClickListener,
            deleteCardClickListener,
            onCardContentClicked,
            onCardDefinitionClicked,
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var isCardRevealed = false

        private val onCardText: TextView = view.findViewById(R.id.tv_card_content)
        private val cardDescription1: TextView = view.findViewById(R.id.tv_card_description1)
        private val cardDescription2: TextView = view.findViewById(R.id.tv_card_description2)
        private val cardDescription3: TextView = view.findViewById(R.id.tv_card_description3)
        private val cardStatus: TextView = view.findViewById(R.id.tv_card_status)
        private val popUpBT: Button = view.findViewById(R.id.pupUpBT)
        private val cardRoot: MaterialCardView = view.findViewById(R.id.card_root)
        private val cvContainerCard: ConstraintLayout = view.findViewById(R.id.cl_container_card)
        private val cardDescriptionError: TextView = view.findViewById(R.id.tv_card_description_error)

        private val ICON_MARGIN = 5

        fun bind(
            context: Context,
            card: ImmutableCard?,
            deck: ImmutableDeck,
            boxLevels: List<ImmutableSpaceRepetitionBox>,
            fullScreenClickListener: (ImmutableCard?) -> Unit,
            editCardClickListener: (ImmutableCard?) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
            onCardContentClicked: (TextClickedModel) -> Unit,
            onCardDefinitionClicked: (TextClickedModel) -> Unit,
        ) {
            //val statusColor = SpaceRepetitionAlgorithmHelper().box[card.cardStatus]?.color
            val actualBoxLevel = SpaceRepetitionAlgorithmHelper().getBoxLevelByStatus(boxLevels, card?.cardStatus!!)
            val statusColor = SpaceRepetitionAlgorithmHelper().selectBoxLevelColor(actualBoxLevel?.levelColor!!)
            val cardBackgroundStatusColor = SpaceRepetitionAlgorithmHelper().selectBackgroundLevelColor(actualBoxLevel.levelColor)
            val colorStateList = ContextCompat.getColorStateList(context, statusColor!!)
            val cardBackgroundStateList = ContextCompat.getColorStateList(context, cardBackgroundStatusColor!!)
            cvContainerCard.backgroundTintList = cardBackgroundStateList
            cardStatus.apply {
                text = card.cardStatus
                backgroundTintList = colorStateList
            }
            onCardText.text = card.cardContent?.content

            val correctDefinition = getCorrectDefinition(card.cardDefinition)
            val definitionTexts = cardDefinitionsToStrings(correctDefinition)

            when (definitionTexts.size) {
                0 -> {
                    cardDescription1.visibility = View.GONE
                    cardDescriptionError.text = context.getString(R.string.error_no_card_definition)
                    cardDescriptionError.visibility = View.VISIBLE
                    cardDescription2.visibility = View.GONE
                    cardDescription3.visibility = View.GONE
                }
                1 -> {
                    cardDescriptionError.visibility = View.GONE
                    cardDescription1.visibility = View.VISIBLE
                    cardDescription1.text = definitionTexts[0]
                    cardDescription2.visibility = View.GONE
                    cardDescription3.visibility = View.GONE
                }
                2 -> {
                    cardDescriptionError.visibility = View.GONE
                    cardDescription1.visibility = View.VISIBLE
                    cardDescription1.text = definitionTexts[0]
                    cardDescription2.visibility = View.VISIBLE
                    cardDescription2.text = definitionTexts[1]
                    cardDescription3.visibility = View.GONE
                }
                3 -> {
                    cardDescriptionError.visibility = View.GONE
                    cardDescription1.visibility = View.VISIBLE
                    cardDescription1.text = definitionTexts[0]
                    cardDescription2.visibility = View.VISIBLE
                    cardDescription2.text = definitionTexts[1]
                    cardDescription3.visibility = View.VISIBLE
                    cardDescription3.text = definitionTexts[2]
                }
                else -> {
                     cardDescriptionError.visibility = View.GONE
                     cardDescription1.visibility = View.VISIBLE
                     cardDescription1.text = definitionTexts[0]
                     cardDescription2.visibility = View.VISIBLE
                     cardDescription2.text = definitionTexts[1]
                     cardDescription3.visibility = View.VISIBLE
                     cardDescription3.text = definitionTexts[2]
                 }
            }
            //cardDescription.text = card?.cardDefinition?.first()?.definition

            popUpBT.setOnClickListener { v: View ->
                showMenu(
                    context,
                    v,
                    R.menu.card_popup_menu,
                    fullScreenClickListener,
                    editCardClickListener,
                    deleteCardClickListener,
                    card
                )
            }
            cardRoot.setOnClickListener {
                //TODO: read card content & definition
            }

            onCardText.setOnClickListener {it as TextView
                onCardContentClicked(TextClickedModel(it.text.toString().lowercase(), it))
            }

            cardDescription1.setOnClickListener { it as TextView
                onCardDefinitionClicked(TextClickedModel(it.text.toString().lowercase(), it))
            }
            cardDescription2.setOnClickListener { it as TextView
                onCardDefinitionClicked(TextClickedModel(it.text.toString().lowercase(), it))
            }
            cardDescription3.setOnClickListener { it as TextView
                onCardDefinitionClicked(TextClickedModel(it.text.toString().lowercase(), it))
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

        @SuppressLint("RestrictedApi")
        private fun showMenu(
            context: Context,
            v: View,
            @MenuRes menuRes: Int,
            fullScreenClickListener: (ImmutableCard?) -> Unit,
            editCardClickListener: (ImmutableCard?) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
            card: ImmutableCard?
            ) {

            val popup = PopupMenu(context, v)
            popup.menuInflater.inflate(menuRes, popup.menu)

            if (popup.menu is MenuBuilder) {
                val menuBuilder = popup.menu as MenuBuilder
                menuBuilder.setOptionalIconsVisible(true)
                for (item in menuBuilder.visibleItems) {
                    val iconMarginPx =
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            ICON_MARGIN.toFloat(),
                            context.resources.displayMetrics
                        )
                            .toInt()
                    if (item.icon != null) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                            item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                        } else {
                            item.icon =
                                object :
                                    InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                    override fun getIntrinsicWidth(): Int {
                                        return intrinsicHeight + iconMarginPx + iconMarginPx
                                    }
                                }
                        }
                    }
                }
            }

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.edit_card_DM -> {
                        editCardClickListener(card)
                        true
                    }
                    R.id.delete_card_DM -> {
                        deleteCardClickListener(card)
                        true
                    }
                    R.id.fullscrean_card_DM -> {
                        fullScreenClickListener(card)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            popup.setOnDismissListener {
                // Respond to popup being dismissed.
            }
            // Show the popup menu.
            popup.show()

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