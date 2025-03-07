package com.ssoaharison.recall.quiz.test

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.google.android.material.card.MaterialCardView
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME

class TestResultRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<TestCardModel>,
    val appTheme: String,
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
            cardList[position],
            appTheme
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
        private val tvAddedCardDescription5: TextView = view.findViewById(R.id.tv_added_card_description5)
        private val tvAddedCardDescription6: TextView = view.findViewById(R.id.tv_added_card_description6)
        private val tvAddedCardDescription7: TextView = view.findViewById(R.id.tv_added_card_description7)
        private val tvAddedCardDescription8: TextView = view.findViewById(R.id.tv_added_card_description8)
        private val tvAddedCardDescription9: TextView = view.findViewById(R.id.tv_added_card_description9)
        private val tvAddedCardDescription10: TextView = view.findViewById(R.id.tv_added_card_description10)
        private val tvAddedCardDescriptionError: TextView = view.findViewById(R.id.tv_added_card_description_error)
        private val tvCardDescriptions = listOf(
            tvAddedCardDescription1, tvAddedCardDescription2, tvAddedCardDescription3,
            tvAddedCardDescription4, tvAddedCardDescription5, tvAddedCardDescription6,
            tvAddedCardDescription7, tvAddedCardDescription8, tvAddedCardDescription9,
            tvAddedCardDescription10,
        )


        fun bind(
            context: Context,
            card: TestCardModel,
            appTheme: String
        ) {

            tvAddedCardContent.text = card.cardContent.content
            btDelete.visibility = View.GONE

            tvCardDescriptions.forEachIndexed { index, textView ->
                if (index < card.cardDefinition.size) {
                    textView.visibility = View.VISIBLE
                    displayCardDefinition(context, card.cardDefinition[index], textView, appTheme)
                } else {
                    textView.visibility = View.GONE
                }
            }
        }

        private fun displayCardDefinition(
            context: Context,
            definition: TestCardDefinitionModel,
            v: TextView,
            appTheme: String
        ) {
            v.visibility = View.VISIBLE
            v.text = definition.definition.text
            if (definition.isSelected) {
                if (definition.isCorrect == 1 && definition.attachedCardId == definition.cardId) {
                    onCorrectAnswer(context, v, appTheme)
                } else {
                    onWrongAnswer(context, v, appTheme)
                }
            } else {
                onNeutralAnswer(context, v)
            }
        }

        private fun onWrongAnswer(context: Context, v: TextView, appTheme: String) {
            val textBackgroundColorStateList: ColorStateList?
            val drawableBackgroundColorStateList: ColorStateList?
            if (appTheme != DARK_THEME) {
                textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.red100)
                drawableBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.red600)
            } else {
                textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.red800)
                drawableBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.red50)
            }
            v.backgroundTintList = textBackgroundColorStateList
            v.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_cancel,
                0,
                0,
                0
            )
            v.setCompoundDrawablePadding(8)
            v.setCompoundDrawableTintList(drawableBackgroundColorStateList)
        }

        private fun onCorrectAnswer(context: Context, v: TextView, appTheme: String) {
            val textBackgroundColorStateList: ColorStateList?
            val drawableBackgroundColorStateList: ColorStateList?
            if (appTheme != DARK_THEME) {
                textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.green100)
                drawableBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.green600)
            } else {
                textBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.green800)
                drawableBackgroundColorStateList = ContextCompat.getColorStateList(context, R.color.green50)
            }
            v.backgroundTintList = textBackgroundColorStateList
            v.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.icon_check_circle,
                0,
                0,
                0
            )
            v.setCompoundDrawablePadding(8)
            v.setCompoundDrawableTintList(drawableBackgroundColorStateList)
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