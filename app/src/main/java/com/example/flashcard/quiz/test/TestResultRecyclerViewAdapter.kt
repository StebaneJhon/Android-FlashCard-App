package com.example.flashcard.quiz.test

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.quiz.quizGame.QuizSpeakModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class TestResultRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<TestCardModel>,
): RecyclerView.Adapter<TestResultRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position]
        )
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

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
            card: TestCardModel,
        ) {

            tvAddedCardContent.text = card.cardContent.content
            btDelete.visibility = View.GONE
            when(card.cardDefinition.size) {
                2 -> {
                    displayCardDefinition(context, card.cardDefinition[0], tvAddedCardDescription1)
                    displayCardDefinition(context, card.cardDefinition[1], tvAddedCardDescription2)
                    tvAddedCardDescription3.visibility = View.GONE
                    tvAddedCardDescription4.visibility = View.GONE
                    tvAddedCardDescriptionError.visibility = View.GONE

                }
                3 -> {
                    displayCardDefinition(context, card.cardDefinition[0], tvAddedCardDescription1)
                    displayCardDefinition(context, card.cardDefinition[1], tvAddedCardDescription2)
                    displayCardDefinition(context, card.cardDefinition[2], tvAddedCardDescription3)
                    tvAddedCardDescription4.visibility = View.GONE
                    tvAddedCardDescriptionError.visibility = View.GONE
                }
                4 -> {
                    displayCardDefinition(context, card.cardDefinition[0], tvAddedCardDescription1)
                    displayCardDefinition(context, card.cardDefinition[1], tvAddedCardDescription2)
                    displayCardDefinition(context, card.cardDefinition[2], tvAddedCardDescription3)
                    displayCardDefinition(context, card.cardDefinition[3], tvAddedCardDescription4)
                    tvAddedCardDescriptionError.visibility = View.GONE
                }
                else -> {
                    tvAddedCardDescriptionError.visibility = View.VISIBLE
                }
            }
        }

        private fun displayCardDefinition(
            context: Context,
            definition: TestCardDefinitionModel,
            v: TextView
        ) {
            v.visibility = View.VISIBLE
            v.text = definition.definition
            if (definition.isSelected) {
                if (definition.isCorrect == 1 && definition.attachedCardId == definition.cardId) {
                    onCorrectAnswer(context, v)
                } else {
                    onWrongAnswer(context, v)
                }
            } else {
                onNeutralAnswer(context, v)
            }
        }

        private fun onWrongAnswer(context: Context, v: TextView) {
            val textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.red100)
            v.backgroundTintList = textBackgroundColorStateList
            v.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_cancel,
                0,
                0,
                0
            )
            v.setCompoundDrawablePadding(8)
        }

        private fun onCorrectAnswer(context: Context, v: TextView) {
            val textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.green100)
            v.backgroundTintList = textBackgroundColorStateList
            v.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_check_circle,
                0,
                0,
                0
            )
            v.setCompoundDrawablePadding(8)
        }

        private fun onNeutralAnswer(context: Context, v: TextView) {
            v.background.setTint(context.getColorFromAttr(com.google.android.material.R.attr.colorSurfaceContainerLow))
            v.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
            v.setCompoundDrawablePadding(0)
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
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_item_card_on_add_new_card, parent, false)
                return ViewHolder(view)
            }
        }
    }

}