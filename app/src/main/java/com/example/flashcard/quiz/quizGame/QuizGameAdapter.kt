package com.example.flashcard.quiz.quizGame

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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class QuizGameAdapter(
    val context: Context,
    val cardList: List<QuizGameCardModel>,
    val deckColor: String,
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
            deck,
            cardOnClick,
            onSpeak,
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
        var hasFlipped = false

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
            deck: ImmutableDeck,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit,
        ) {

//            if (modelCard!!.isFlipped) {
//                cvCardContainerBack.alpha = 1f
//                cvCardContainerBack.rotationY = 0f
//                cvCardContainer.alpha = 0f
//            } else {
//                cvCardContainer.alpha = 1f
//                cvCardContainer.rotationY = 0f
//                cvCardContainerBack.alpha = 1f
//            }

//            val card = modelCard.cardDetails
            tvFrontProgression.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvContent.text = card.cardContent?.content
            tvCardType.text = card.cardType
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardContainer.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)

            if (card.cardType == SINGLE_ANSWER_CARD) {
//                if (card.isFlipped) {
//                    cvCardContainerBack.alpha = 1f
//                    cvCardContainerBack.rotationY = 0f
//                    cvCardContainer.alpha = 0f
//                } else {
//                    cvCardContainer.alpha = 1f
//                    cvCardContainer.rotationY = 0f
//                    cvCardContainerBack.alpha = 1f
//                }
            } else {
                cvCardContainer.alpha = 1f
                cvCardContainer.rotationY = 0f
                cvCardContainerBack.alpha = 1f
                btAlternatives.forEachIndexed { index, materialButton ->
                    if (index < card.cardDefinition.size) {
                        materialButton.visibility = View.VISIBLE
                        if (card.cardDefinition[index].isSelected) {
                            val answerStatus = card.cardDefinition[index].isCorrect != 0
                            onButtonClicked(materialButton, card.cardType!!, context, answerStatus)
                        } else {
                            onButtonUnClicked(materialButton, card.cardType!!, context)
                        }
                    } else {
                        materialButton.visibility = View.GONE
                    }
                }
            }

            bindAnswerAlternatives(card, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)

//            when(card?.cardType) {
//                SINGLE_ANSWER_CARD -> {
//                    onFlashCard(card, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
//                }
//                TRUE_OR_FALSE_CARD -> {
//                    onTrueOrFalseCard(card,cardPosition , cardOnClick)
//                }
//                MULTIPLE_ANSWER_CARD -> {
//                    bindAnswerAlternatives(card, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
//                }
//                else -> {
//                    onFlashCard(card, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
//                }
//            }

        }

//        private fun onFlashCard(
//            modelCard: ModelCard,
//            deckColorCode: String,
//            cardNumber: Int,
//            cardPosition: Int,
//            cardSum: Int,
//            cardOnClick: (UserResponseModel) -> Unit
//        ) {
//            val card = modelCard.cardDetails
//            val cardModel = FlashCardModel(modelCard, cardList)
//            btAlternatives.forEach {
//                it.visibility = View.GONE
//            }
////            btAlternative1.isVisible = false
////            btAlternative2.isVisible = false
////            btAlternative3.isVisible = false
////            btAlternative4.isVisible = false
//            flCardRoot.isClickable = cardModel.isFlippable()
//
//            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
//            cvCardContainerBack.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)
//            tvBackProgression.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
//            tvDefinition.text = card?.cardDefinition?.get(0)?.definition
//            tvCardTypeBack.text = card?.cardType
//
//            flCardRoot.setOnClickListener {
//                cardOnClick(
//                    UserResponseModel(
//                        null,
//                        modelCard,
//                        cardPosition,
//                        it,
//                        cvCardContainer,
//                        cvCardContainerBack
//                    )
//                )
//                flipCard(modelCard.isFlipped)
//            }
//
//            btSpeak.setOnClickListener {
//                val views = listOf(tvContent)
//                val texts = listOf(card?.cardContent?.content!!)
//                onSpeak(
//                    QuizSpeakModel(
//                        text = texts,
//                        views = views,
//                        deck.deckFirstLanguage!!
//                    )
//                )
//            }
//            btSpeakBack.setOnClickListener {
//                val views = listOf(tvDefinition)
//                val texts = listOf(card?.cardDefinition?.get(0)?.definition!!)
//                onSpeak(
//                    QuizSpeakModel(
//                        text = texts,
//                        views = views,
//                        deck.deckSecondLanguage!!
//                    )
//                )
//            }
//
//        }

//        private fun onTrueOrFalseCard(
//            modelCard: ModelCard,
//            cardPosition: Int,
//            cardOnClick: (UserResponseModel) -> Unit
//        ) {
//            val card = modelCard.cardDetails
//            val cardModel = TrueOrFalseCardModel(modelCard, cardList)
//            val answers = cardModel.getCardAnswers()
//            flCardRoot.isClickable = cardModel.isFlippable()
//            btAlternative1.apply {
//                isVisible = true
//                text = answers[0].definition
//                onAlternativeClicked(answers[0], cardOnClick, modelCard, cardPosition)
//            }
//            btAlternative2.apply {
//                isVisible = true
//                text = answers[1].definition
//                onAlternativeClicked(answers[1], cardOnClick, modelCard, cardPosition)
//            }
//            btAlternative3.isVisible = false
//            btAlternative4.isVisible = false
//
//            btSpeak.setOnClickListener {
//                val views = listOf(tvContent)
//                val texts = listOf(card?.cardContent?.content!!)
//                onSpeak(
//                    QuizSpeakModel(
//                        text = texts,
//                        views = views,
//                        deck.deckFirstLanguage!!
//                    )
//                )
//            }
//
//        }

        private fun MaterialButton.onAlternativeClicked(
            answer: QuizGameCardDefinitionModel,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
//            modelCard: ModelCard,
            cardPosition: Int
        ) {
            setOnClickListener {
//                cardOnClick(
//                    UserResponseModel(
//                        answer,
//                        modelCard,
//                        cardPosition,
//                        this,
//                        cvCardContainer,
//                        cvCardContainerBack
//                    )
//                )
                answer.isSelected = !answer.isSelected
                cardOnClick(answer)
            }
        }

        private fun onButtonClicked(
            button: MaterialButton,
            cardType: String,
            context: Context,
            isCorrectlyAnswered: Boolean
        ) {
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
//            val card = modelCard.cardDetails
//            val cardModel = OneOrMultipleAnswerCardModel(modelCard, cardList)
//            val answers = cardModel.getCardAnswers()

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
                    flipCard(card.isFlipped)
                }
                btSpeakBack.setOnClickListener {
                    onSpeak(
                        QuizSpeakModel(
                            text = listOf(card.cardDefinition.first().definition),
                            views = listOf(tvDefinition),
                            deck.deckSecondLanguage!!
                        )
                    )
                }
                btSpeak.setOnClickListener {
                    frontSpeak(
                        listOf(tvContent),
                        listOf(card.cardContent?.content!!),
                        deck.deckFirstLanguage!!
                    )
                }
            } else {
                flCardRoot.isClickable = false
                val texts = arrayListOf(card.cardContent?.content!!)
                val views = arrayListOf(tvContent)
                btAlternatives.forEachIndexed { index, materialButton ->
                    if (index < card.cardDefinition.size) {
                        materialButton.apply {
                            visibility = View.VISIBLE
                            text = card.cardDefinition[index].definition
                            onAlternativeClicked(card.cardDefinition[index], cardOnClick, cardPosition)
                        }
                        texts.add(card.cardDefinition[index].definition)
                        views.add(materialButton)
                    } else {
                        materialButton.visibility = View.GONE
                    }
                }
                frontSpeak(views, texts, deck.deckFirstLanguage!!)
            }


//            when (answers.size ) {
//                2 -> {
//                    btAlternative1.apply {
//                        isVisible = true
//                        text = answers[0].definition
//                        onAlternativeClicked(answers[0], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative2.apply {
//                        isVisible = true
//                        text = answers[1].definition
//                        onAlternativeClicked(answers[1], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative3.isVisible = false
//                    btAlternative4.isVisible = false
//
//                    val texts = listOf(
//                        card?.cardContent?.content!!,
//                        answers[0].definition,
//                        answers[1].definition
//                    )
//                    val views = listOf(
//                        tvContent,
//                        btAlternative1,
//                        btAlternative2
//                    )
//
//                    frontSpeak(
//                        views,
//                        texts,
//                        deck.deckFirstLanguage!!
//                    )
//
//                }
//                3 -> {
//                    btAlternative1.apply {
//                        isVisible = true
//                        text = answers[0].definition
//                        onAlternativeClicked(answers[0], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative2.apply {
//                        isVisible = true
//                        text = answers[1].definition
//                        onAlternativeClicked(answers[1], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative3.apply {
//                        isVisible = true
//                        text = answers[2].definition
//                        onAlternativeClicked(answers[2], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative4.isVisible = false
//                    val texts = listOf(
//                        card?.cardContent?.content!!,
//                        answers[0].definition,
//                        answers[1].definition,
//                        answers[2].definition,
//                    )
//                    val views = listOf(
//                        tvContent,
//                        btAlternative1,
//                        btAlternative2,
//                        btAlternative3,
//                    )
//
//                    frontSpeak(
//                        views,
//                        texts,
//                        deck.deckFirstLanguage!!
//                    )
//                }
//                4 -> {
//                    btAlternative1.apply {
//                        isVisible = true
//                        text = answers[0].definition
//                        onAlternativeClicked(answers[0], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative2.apply {
//                        isVisible = true
//                        text = answers[1].definition
//                        onAlternativeClicked(answers[1], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative3.apply {
//                        isVisible = true
//                        text = answers[2].definition
//                        onAlternativeClicked(answers[2], cardOnClick, modelCard, cardPosition)
//                    }
//                    btAlternative4.apply {
//                        isVisible = true
//                        text = answers[3].definition
//                        onAlternativeClicked(answers[3], cardOnClick, modelCard, cardPosition)
//                    }
//                    val texts = listOf(
//                        card?.cardContent?.content!!,
//                        answers[0].definition,
//                        answers[1].definition,
//                        answers[2].definition,
//                        answers[3].definition,
//                    )
//                    val views = listOf(
//                        tvContent,
//                        btAlternative1,
//                        btAlternative2,
//                        btAlternative3,
//                        btAlternative4,
//                    )
//
//                    frontSpeak(
//                        views,
//                        texts,
//                        deck.deckFirstLanguage!!
//                    )
//
//                }
//                else -> {
//                    onFlashCard(modelCard, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
//                }
//            }


        }

        private fun frontSpeak(
            views: List<View>,
            texts: List<String>,
            language: String,
        ) {
            btSpeak.setOnClickListener {
                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                        language
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