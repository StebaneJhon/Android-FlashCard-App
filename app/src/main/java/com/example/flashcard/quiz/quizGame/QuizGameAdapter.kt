package com.example.flashcard.quiz.quizGame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.DeckColorCategorySelector
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class QuizGameAdapter(
    val context: Context,
    val cardList: List<ModelCard?>,
    val deckColor: String,
    val deck: ImmutableDeck,
    private val cardOnClick: (UserResponseModel) -> Unit,
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
            modelCard: ModelCard?,
            cardNumber: Int,
            cardPosition: Int,
            cardSum: Int,
            deckColorCode: String,
            deck: ImmutableDeck,
            cardOnClick: (UserResponseModel) -> Unit,
            onSpeak: (QuizSpeakModel) -> Unit,
        ) {

            if (modelCard!!.isFlipped) {
                cvCardContainerBack.alpha = 1f
                cvCardContainerBack.rotationY = 0f
                cvCardContainer.alpha = 0f
            } else {
                cvCardContainer.alpha = 1f
                cvCardContainer.rotationY = 0f
                cvCardContainerBack.alpha = 1f
            }

            val card = modelCard.cardDetails
            tvFrontProgression.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvContent.text = card?.cardContent?.content
            tvCardType.text = card?.cardType
            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardContainer.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)



            when(card?.cardType) {
                SINGLE_ANSWER_CARD -> {
                    onFlashCard(modelCard, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
                }
                TRUE_OR_FALSE_CARD -> {
                    onTrueOrFalseCard(modelCard,cardPosition , cardOnClick)
                }
                MULTIPLE_ANSWER_CARD -> {
                    onOneOrMultiAnswer(modelCard, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
                }
                else -> {
                    onFlashCard(modelCard, deckColorCode, cardNumber, cardPosition, cardSum, cardOnClick)
                }
            }

        }

        private fun onFlashCard(
            modelCard: ModelCard,
            deckColorCode: String,
            cardNumber: Int,
            cardPosition: Int,
            cardSum: Int,
            cardOnClick: (UserResponseModel) -> Unit
        ) {
            val card = modelCard.cardDetails
            val cardModel = FlashCardModel(modelCard, cardList)
            btAlternatives.forEach {
                it.visibility = View.GONE
            }
//            btAlternative1.isVisible = false
//            btAlternative2.isVisible = false
//            btAlternative3.isVisible = false
//            btAlternative4.isVisible = false
            flCardRoot.isClickable = cardModel.isFlippable()

            val deckColor = DeckColorCategorySelector().selectColor(deckColorCode) ?: R.color.black
            cvCardContainerBack.backgroundTintList = ContextCompat.getColorStateList(context, deckColor)
            tvBackProgression.text = context.getString(R.string.tx_flash_card_game_progression, "$cardNumber", "$cardSum")
            tvDefinition.text = card?.cardDefinition?.get(0)?.definition
            tvCardTypeBack.text = card?.cardType

            flCardRoot.setOnClickListener {
                cardOnClick(
                    UserResponseModel(
                        null,
                        modelCard,
                        cardPosition,
                        it,
                        cvCardContainer,
                        cvCardContainerBack
                    )
                )
                flipCard(modelCard.isFlipped)
            }

            btSpeak.setOnClickListener {
                val views = listOf(tvContent)
                val texts = listOf(card?.cardContent?.content!!)
                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                        deck.deckFirstLanguage!!
                    )
                )
            }
            btSpeakBack.setOnClickListener {
                val views = listOf(tvDefinition)
                val texts = listOf(card?.cardDefinition?.get(0)?.definition!!)
                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                        deck.deckSecondLanguage!!
                    )
                )
            }

        }

        private fun onTrueOrFalseCard(
            modelCard: ModelCard,
            cardPosition: Int,
            cardOnClick: (UserResponseModel) -> Unit
        ) {
            val card = modelCard.cardDetails
            val cardModel = TrueOrFalseCardModel(modelCard, cardList)
            val answers = cardModel.getCardAnswers()
            flCardRoot.isClickable = cardModel.isFlippable()
            btAlternative1.apply {
                isVisible = true
                text = answers[0].definition
                onAlternativeClicked(answers[0], cardOnClick, modelCard, cardPosition)
            }
            btAlternative2.apply {
                isVisible = true
                text = answers[1].definition
                onAlternativeClicked(answers[1], cardOnClick, modelCard, cardPosition)
            }
            btAlternative3.isVisible = false
            btAlternative4.isVisible = false

            btSpeak.setOnClickListener {
                val views = listOf(tvContent)
                val texts = listOf(card?.cardContent?.content!!)
                onSpeak(
                    QuizSpeakModel(
                        text = texts,
                        views = views,
                        deck.deckFirstLanguage!!
                    )
                )
            }

        }

        private fun MaterialButton.onAlternativeClicked(
            answer: CardDefinition,
            cardOnClick: (UserResponseModel) -> Unit,
            modelCard: ModelCard,
            cardPosition: Int
        ) {
            setOnClickListener {
                cardOnClick(
                    UserResponseModel(
                        answer,
                        modelCard,
                        cardPosition,
                        this,
                        cvCardContainer,
                        cvCardContainerBack
                    )
                )
            }
        }

        fun onOneOrMultiAnswer(
            modelCard: ModelCard,
            deckColorCode: String,
            cardNumber: Int,
            cardPosition: Int,
            cardSum: Int,
            cardOnClick: (UserResponseModel) -> Unit
        ) {
            val card = modelCard.cardDetails
            val cardModel = OneOrMultipleAnswerCardModel(modelCard, cardList)
            val answers = cardModel.getCardAnswers()
            flCardRoot.isClickable = cardModel.isFlippable()

            val texts = arrayListOf(card?.cardContent?.content!!)
            val views = arrayListOf(tvContent)
            btAlternatives.forEachIndexed { index, materialButton ->
                if (index < answers.size) {
                    materialButton.apply {
                        visibility = View.VISIBLE
                        text = answers[index].definition
                        onAlternativeClicked(answers[index], cardOnClick, modelCard, cardPosition)
                    }
                    texts.add(answers[index].definition)
                    views.add(materialButton)
                } else {
                    materialButton.visibility = View.GONE
                }
            }
            frontSpeak(views, texts, deck.deckFirstLanguage!!)
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