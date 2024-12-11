package com.ssoaharison.recall.quiz.quizGame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.ssoaharison.recall.util.TextWithLanguageModel
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME

class QuizGameAdapter(
    val context: Context,
    val cardList: List<QuizGameCardModel>,
    private val deckColor: String,
    val appTheme: String,
    val deck: ImmutableDeck,
    private val cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
    private val onSpeak: (QuizSpeakModel) -> Unit,
): RecyclerView.Adapter<QuizGameAdapter.TestQuizGameAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TestQuizGameAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_card_test, parent, false)
        return TestQuizGameAdapterViewHolder(view)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: TestQuizGameAdapterViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            position.plus(1),
            position,
            cardList.size,
            deckColor,
            appTheme,
            cardOnClick,
        )
    }

    inner class TestQuizGameAdapterViewHolder(
        view: View
    ): RecyclerView.ViewHolder(view) {

        private val flCardRoot: FrameLayout = view.findViewById(R.id.fl_card_root)
        private val cvCardContainer: MaterialCardView = view.findViewById(R.id.cv_card_container)
        private val cvCardContainerBack: MaterialCardView = view.findViewById(R.id.cv_card_container_back)
        private val tvFrontProgression: TextView = view.findViewById(R.id.tv_front_progression)
        private val tvBackProgression: TextView = view.findViewById(R.id.tv_back_progression)
        private val tvCardType: TextView = view.findViewById(R.id.tv_card_type)
        private val tvCardTypeBack: TextView = view.findViewById(R.id.tv_card_type_back)
        private val btSpeak: MaterialButton = view.findViewById(R.id.bt_speak)
        private val btSpeakBack: Button = view.findViewById(R.id.bt_speak_back)
        private val tvContent: TextView = view.findViewById(R.id.tv_content)
        private val tvDefinition: TextView = view.findViewById(R.id.tv_definition)
        private val tvHint: TextView = view.findViewById(R.id.tv_hint)
        private val btAlternative1: MaterialButton = view.findViewById(R.id.bt_alternative1)
        private val btAlternative2: MaterialButton = view.findViewById(R.id.bt_alternative2)
        private val btAlternative3: MaterialButton = view.findViewById(R.id.bt_alternative3)
        private val btAlternative4: MaterialButton = view.findViewById(R.id.bt_alternative4)
        private val btAlternative5: MaterialButton = view.findViewById(R.id.bt_alternative5)
        private val btAlternative6: MaterialButton = view.findViewById(R.id.bt_alternative6)
        private val btAlternative7: MaterialButton = view.findViewById(R.id.bt_alternative7)
        private val btAlternative8: MaterialButton = view.findViewById(R.id.bt_alternative8)
        private val btAlternative9: MaterialButton = view.findViewById(R.id.bt_alternative9)
        private val btAlternative10: MaterialButton = view.findViewById(R.id.bt_alternative10)
        private lateinit var frontAnim: AnimatorSet
        private lateinit var backAnim: AnimatorSet

        private val btAlternatives = listOf(
            btAlternative1, btAlternative2, btAlternative3, btAlternative4, btAlternative5,
            btAlternative6, btAlternative7, btAlternative8, btAlternative9, btAlternative10,
        )


        fun bind(
            context: Context,
            card: QuizGameCardModel,
            cardNumber: Int,
            cardPosition: Int,
            cardSum: Int,
            deckColorCode: String,
            appTheme: String,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
        ) {

            tvFrontProgression.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvContent.text = card.cardContent?.content
            tvCardType.text = card.cardType
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardContainer.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            when {
                card.cardType == SINGLE_ANSWER_CARD -> {
                    tvHint.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    tvHint.text = ContextCompat.getString(context, R.string.text_tap_to_flip)
                    tvHint.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface))
                }
                card.attemptTime == 0 -> {
                    tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    tvHint.text = ContextCompat.getString(context, R.string.text_not_answered)
                    tvHint.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface))
                }
                card.attemptTime > 0 && card.isCorrectlyAnswered -> {
                    tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    tvHint.text = ContextCompat.getString(context, R.string.text_correct)
                    tvHint.setTextColor(ContextCompat.getColor(context, R.color.green500))
                }
                card.attemptTime > 0 && hasCardCorrectAnswer(card) && !card.isCorrectlyAnswered  -> {
                    tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    tvHint.text = ContextCompat.getString(context, R.string.text_correct_answer_more)
                    tvHint.setTextColor(ContextCompat.getColor(context, R.color.green500))
                }
                card.attemptTime > 0 && !card.isCorrectlyAnswered -> {
                    tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    tvHint.text = ContextCompat.getString(context, R.string.text_wrong_answer)
                    tvHint.setTextColor(ContextCompat.getColor(context, R.color.red500))
                }
            }

            if (card.cardType == SINGLE_ANSWER_CARD) {
//                if(!card.isFlipped) {
//                    cvCardContainer.alpha = 1f
//                    cvCardContainer.rotationY = 0f
//                    cvCardContainerBack.alpha = 1f
//                } else {
//                    cvCardContainer.alpha = 0f
//                    cvCardContainer.rotationY = 0f
//                    cvCardContainerBack.alpha = 1f
//                }

                if (card.flipCount == 0) {
                    if (!card.isFlipped) {
                        cvCardContainer.alpha = 1f
                        cvCardContainer.rotationY = 0f
                        cvCardContainerBack.alpha = 0f
                    } else {
                        cvCardContainer.alpha = 0f
                        cvCardContainer.rotationY = 0f
                        cvCardContainerBack.rotationY = 0f
                        cvCardContainerBack.alpha = 1f
                    }
                } else {
                    flipCard(card.isFlipped)
                }
            } else {
                cvCardContainer.alpha = 1f
                cvCardContainer.rotationY = 0f
                cvCardContainerBack.alpha = 1f
                btAlternatives.forEachIndexed { index, materialButton ->
                    if (index < card.cardDefinition.size) {
                        materialButton.visibility = View.VISIBLE
                        if (card.cardDefinition[index].isSelected) {
                            val answerStatus = card.cardDefinition[index].isCorrect != 0
                            onButtonClicked(materialButton, card.cardType!!, context, answerStatus, appTheme)
                        } else {
                            onButtonUnClicked(materialButton, card.cardType!!, context)
                        }
                    } else {
                        materialButton.visibility = View.GONE
                    }
                }
            }
            bindAnswerAlternatives(card, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
        }

        private fun hasCardCorrectAnswer(card: QuizGameCardModel): Boolean {
            card.cardDefinition.forEach { d ->
                if (d.isSelected && d.isCorrect == 1) {
                    return true
                }
            }
            return false
        }

        private fun MaterialButton.onAlternativeClicked(
            answer: QuizGameCardDefinitionModel,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
        ) {
            setOnClickListener {
                answer.isSelected = !answer.isSelected
                cardOnClick(answer)
            }
        }

        private fun onButtonClicked(
            button: MaterialButton,
            cardType: String,
            context: Context,
            isCorrectlyAnswered: Boolean,
            appTheme: String
        ) {
            if (appTheme != DARK_THEME) {
                if (isCorrectlyAnswered) {
                    if (cardType == MULTIPLE_ANSWER_CARD) {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box)
                    } else {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_radio_button_checked)
                    }
                    button.background.setTint(ContextCompat.getColor(context, R.color.green50))
                    button.setStrokeColorResource(R.color.green500)
                    button.setIconTintResource(R.color.green500)
                } else {
                    if (cardType == MULTIPLE_ANSWER_CARD) {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box_wrong)
                    } else {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_cancel)
                    }
                    button.background.setTint(ContextCompat.getColor(context, R.color.red50))
                    button.setStrokeColorResource(R.color.red500)
                    button.setIconTintResource(R.color.red500)
                }
            } else {
                if (isCorrectlyAnswered) {
                    if (cardType == MULTIPLE_ANSWER_CARD) {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box)
                    } else {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_radio_button_checked)
                    }
                    button.background.setTint(ContextCompat.getColor(context, R.color.green800))
                    button.setStrokeColorResource(R.color.green50)
                    button.setIconTintResource(R.color.green50)
                    button.setTextColor(ContextCompat.getColor(context, R.color.green50))
                } else {
                    if (cardType == MULTIPLE_ANSWER_CARD) {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box_wrong)
                    } else {
                        button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_cancel)
                    }
                    button.background.setTint(ContextCompat.getColor(context, R.color.red800))
                    button.setStrokeColorResource(R.color.red50)
                    button.setIconTintResource(R.color.red50)
                    button.setTextColor(ContextCompat.getColor(context, R.color.red50))
                }
            }


        }

        private fun onButtonUnClicked(button: MaterialButton, cardType: String, context: Context) {
            if (cardType == MULTIPLE_ANSWER_CARD) {
                button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_check_box_outline_blank)
            } else {
                button.icon = AppCompatResources.getDrawable(context, R.drawable.icon_radio_button_unchecked)
            }
            button.background.setTint(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerLowest, Color.GRAY))
            button.iconTint = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorOnSurface,
                ContextCompat.getColorStateList(context, R.color.neutral950)!!
            )
            button.strokeColor = MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerHigh,
                ContextCompat.getColorStateList(context, R.color.neutral500)!!
            )
        }

        fun bindAnswerAlternatives(
            card: QuizGameCardModel,
            deckColorCode: String,
            cardNumber: Int,
            cardPosition: Int,
            cardSum: Int,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit
        ) {
            if (card.cardType == SINGLE_ANSWER_CARD) {

                flCardRoot.isClickable = true
                btAlternatives.forEach { materialButton ->
                    materialButton.visibility = View.GONE
                }
                val deckColor =
                    DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
                cvCardContainerBack.backgroundTintList =
                    ContextCompat.getColorStateList(context, deckColor)
                tvBackProgression.text = context.getString(
                    R.string.tx_flash_card_game_progression,
                    "$cardNumber",
                    "$cardSum"
                )
                tvDefinition.text = card.cardDefinition.first().definition
                tvCardTypeBack.text = card.cardType
                flCardRoot.setOnClickListener {
                    card.cardDefinition.first().isSelected = true
                    cardOnClick(
                        card.cardDefinition.first()
                    )
//                    flipCard(card.isFlipped)
                }
                btSpeakBack.setOnClickListener {
                    onSpeak(
                        QuizSpeakModel(
                            text = listOf(TextWithLanguageModel(card.cardDefinition.first().definition, card.cardDefinitionLanguage!!)),
                            views = listOf(tvDefinition),
                        )
                    )
                }
                btSpeak.setOnClickListener {
                    frontSpeak(
                        listOf(tvContent),
                        listOf(TextWithLanguageModel(card.cardContent?.content!!, card.cardContentLanguage!!)),
                    )
                }
            } else {
                flCardRoot.isClickable = false
                val texts = arrayListOf(TextWithLanguageModel(card.cardContent?.content!!, card.cardContentLanguage!!))
                val views = arrayListOf(tvContent)
                btAlternatives.forEachIndexed { index, materialButton ->
                    if (index < card.cardDefinition.size) {
                        materialButton.apply {
                            visibility = View.VISIBLE
                            text = card.cardDefinition[index].definition
                            onAlternativeClicked(card.cardDefinition[index], cardOnClick)
                        }
                        texts.add(TextWithLanguageModel(card.cardDefinition[index].definition, card.cardDefinitionLanguage!!))
                        views.add(materialButton)
                    } else {
                        materialButton.visibility = View.GONE
                    }
                }
                frontSpeak(views, texts)
            }
        }

        private fun frontSpeak(
            views: List<View>,
            texts: List<TextWithLanguageModel>,
        ) {
            btSpeak.setOnClickListener {
                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                    )
                )
            }
        }

        private fun flipCard(isFlipped: Boolean) {
            frontAnim = AnimatorInflater.loadAnimator(
                context,
                R.animator.front_animator
            ) as AnimatorSet
            backAnim = AnimatorInflater.loadAnimator(
                context,
                R.animator.back_animator
            ) as AnimatorSet

            val scale: Float = context.resources.displayMetrics.density
            cvCardContainer.cameraDistance = 8000 * scale
            cvCardContainerBack.cameraDistance = 8000 * scale
            if (isFlipped) {
                frontAnim.setTarget(cvCardContainer)
                backAnim.setTarget(cvCardContainerBack)
                frontAnim.start()
                backAnim.start()
            } else {
                frontAnim.setTarget(cvCardContainerBack)
                backAnim.setTarget(cvCardContainer)
                frontAnim.start()
                backAnim.start()
            }
        }

    }
}