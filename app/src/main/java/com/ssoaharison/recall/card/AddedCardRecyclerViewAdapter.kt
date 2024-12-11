package com.ssoaharison.recall.card

import android.annotation.SuppressLint
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
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.google.android.material.card.MaterialCardView
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME

class AddedCardRecyclerViewAdapter(
    private val context: Context,
    private val cardList: List<ImmutableCard?>,
    private val deck: ImmutableDeck,
    private val appTheme: String,
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
            editCardClickListener,
            deleteCardClickListener,
            appTheme,
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
        private val tvAddedCardDescription5: TextView = view.findViewById(R.id.tv_added_card_description5)
        private val tvAddedCardDescription6: TextView = view.findViewById(R.id.tv_added_card_description6)
        private val tvAddedCardDescription7: TextView = view.findViewById(R.id.tv_added_card_description7)
        private val tvAddedCardDescription8: TextView = view.findViewById(R.id.tv_added_card_description8)
        private val tvAddedCardDescription9: TextView = view.findViewById(R.id.tv_added_card_description9)
        private val tvAddedCardDescription10: TextView = view.findViewById(R.id.tv_added_card_description10)
        private val tvAddedCardDescriptionError: TextView = view.findViewById(R.id.tv_added_card_description_error)

        private val tvDefinitions = listOf(
            tvAddedCardDescription1, tvAddedCardDescription2, tvAddedCardDescription3,
            tvAddedCardDescription4, tvAddedCardDescription5, tvAddedCardDescription6,
            tvAddedCardDescription7, tvAddedCardDescription8, tvAddedCardDescription9,
            tvAddedCardDescription10,
        )

        fun bind(
            context: Context,
            card: ImmutableCard?,
            cardPosition: Int,
            editCardClickListener: (ModelCardWithPositionOnLocalEdit) -> Unit,
            deleteCardClickListener: (ImmutableCard?) -> Unit,
            appTheme: String,
        ) {

            tvAddedCardContent.text = card?.cardContent?.content

            tvDefinitions.forEachIndexed { index, tv ->
                if (index < card!!.cardDefinition!!.size) {
                    tvAddedCardDescriptionError.visibility = View.GONE
                    tv.visibility = View.VISIBLE
                    displayCardDefinition(context, tv, card.cardDefinition?.get(index), appTheme)
                } else {
                    tv.visibility = View.GONE
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
        fun displayCardDefinition(
            context: Context,
            view: TextView,
            definition: CardDefinition?,
            appTheme: String,
        ) {
            definition?.let {
                view.visibility = View.VISIBLE
                val textBackgroundColorStateList: ColorStateList? = if (appTheme != DARK_THEME) {
                    ContextCompat.getColorStateList(context, R.color.green100)
                } else {
                    ContextCompat.getColorStateList(context, R.color.green800)
                }
                if (it.isCorrectDefinition == 1) {
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