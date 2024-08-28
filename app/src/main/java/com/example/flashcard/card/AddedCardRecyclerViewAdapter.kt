package com.example.flashcard.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.getColorOrThrow
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardDefinition
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class AddedCardRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<ImmutableCard?>,
    private val deck: ImmutableDeck,
    private val editCardClickListener: (ModelCardWithPositionOnLocalEdit) -> Unit,
    private val deleteCardClickListener: (ImmutableCard?) -> Unit,
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
            position,
            deck,
            editCardClickListener,
            deleteCardClickListener,
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cardRoot: MaterialCardView = view.findViewById(R.id.cv_on_add_new_card_item_root)
        private val btDelete: Button = view.findViewById(R.id.bt_added_delete)
        private val tvAddedCardContent: TextView = view.findViewById(R.id.tv_added_card_content)
        private val tvAddedCardDescription1: TextView = view.findViewById(R.id.tv_added_card_description1)
        private val tvAddedCardDescription2: TextView = view.findViewById(R.id.tv_added_card_description2)
        private val tvAddedCardDescription3: TextView = view.findViewById(R.id.tv_added_card_description3)
        private val tvAddedCardDescription4: TextView = view.findViewById(R.id.tv_added_card_description4)
        private val tvAddedCardDescriptionError: TextView = view.findViewById(R.id.tv_added_card_description_error)

        fun bind(
            context: Context,
            card: ImmutableCard?,
            cardPosition: Int,
            deck: ImmutableDeck,
            editCardClickListener: (ModelCardWithPositionOnLocalEdit) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
        ) {

            tvAddedCardContent.text = card?.cardContent?.content

            when (card?.cardDefinition?.size) {
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

            cardRoot.setOnClickListener {
                editCardClickListener(
                    ModelCardWithPositionOnLocalEdit(
                        card!!, cardPosition
                    )
                )
            }
            btDelete.setOnClickListener { deleteCardClickListener(card) }

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
                    view.background.setTint(context.getColorFromAttr(com.google.android.material.R.attr.colorSurfaceContainerLow))
                    view.text = it.definition
                }
            }

        }

        @ColorInt
        fun Context.getColorFromAttr(
            @AttrRes attrColor: Int,
            typedValue: TypedValue = TypedValue(),
            resolveRefs: Boolean = true
        ): Int {
            theme.resolveAttribute(attrColor, typedValue, resolveRefs)
            return typedValue.data
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