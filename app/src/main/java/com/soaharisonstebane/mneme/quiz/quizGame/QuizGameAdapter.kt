package com.soaharisonstebane.mneme.quiz.quizGame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.util.CardType.SINGLE_ANSWER_CARD
import com.soaharisonstebane.mneme.util.CardType.MULTIPLE_ANSWER_CARD
import com.google.android.material.color.MaterialColors
import com.soaharisonstebane.mneme.backend.models.ExternalDeck
import com.soaharisonstebane.mneme.databinding.LyAudioPlayerBinding
import com.soaharisonstebane.mneme.databinding.LyCardTestBinding
import com.soaharisonstebane.mneme.databinding.LyQuizAlternativeBinding
import com.soaharisonstebane.mneme.helper.AppThemeHelper
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.util.TextType.CONTENT
import com.soaharisonstebane.mneme.util.TextType.DEFINITION
import com.soaharisonstebane.mneme.util.TextWithLanguageModel
import java.io.File

class QuizGameAdapter(
    val context: Context,
    val cardList: List<QuizGameCardModel>,
    val deck: ExternalDeck,
    private val cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
    private val onSpeak: (QuizSpeakModel) -> Unit,
    private val onPlayAudio: (AudioModel, LyAudioPlayerBinding) -> Unit,
) : RecyclerView.Adapter<QuizGameAdapter.TestQuizGameAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TestQuizGameAdapterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LyCardTestBinding.inflate(layoutInflater, parent, false)
        return TestQuizGameAdapterViewHolder(binding)
    }

    override fun getItemCount() = cardList.size

    override fun onBindViewHolder(holder: TestQuizGameAdapterViewHolder, position: Int) {
        return holder.bind(
            context,
            cardList[position],
            cardOnClick,
            onPlayAudio,
        )
    }

    inner class TestQuizGameAdapterViewHolder(
        private val binding: LyCardTestBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var frontAnim: AnimatorSet
        private lateinit var backAnim: AnimatorSet

        private val alternatives = listOf(
            QuizAlternativeModel(container = binding.btAlternative1, view = binding.inAlternative1),
            QuizAlternativeModel(container = binding.btAlternative2, view = binding.inAlternative2),
            QuizAlternativeModel(container = binding.btAlternative3, view = binding.inAlternative3),
            QuizAlternativeModel(container = binding.btAlternative4, view = binding.inAlternative4),
            QuizAlternativeModel(container = binding.btAlternative5, view = binding.inAlternative5),
            QuizAlternativeModel(container = binding.btAlternative6, view = binding.inAlternative6),
            QuizAlternativeModel(container = binding.btAlternative7, view = binding.inAlternative7),
            QuizAlternativeModel(container = binding.btAlternative8, view = binding.inAlternative8),
            QuizAlternativeModel(container = binding.btAlternative9, view = binding.inAlternative9),
            QuizAlternativeModel(container = binding.btAlternative10, view = binding.inAlternative10),
        )


        fun bind(
            context: Context,
            card: QuizGameCardModel,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
            onPlayAudio: (AudioModel, LyAudioPlayerBinding) -> Unit,
        ) {

            if (card.cardContent.contentText != null) {
                val spannableString = Html.fromHtml(card.cardContent.contentText, FROM_HTML_MODE_LEGACY).trim()
                binding.inContent.tvText.apply {
                    visibility = View.VISIBLE
                    text = spannableString
                }
            } else {
                binding.inContent.tvText.visibility = View.GONE
            }

            if (card.cardContent.contentImage != null) {
                val photoFile = File(context.filesDir, card.cardContent.contentImage.name)
                val photoBytes = photoFile.readBytes()
                val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                binding.inContent.imgPhoto.apply {
                    visibility = View.VISIBLE
                    setImageBitmap(photoBtm)
                }
            } else {
                binding.inContent.imgPhoto.visibility = View.GONE
            }

            if (card.cardContent.contentAudio != null) {
                binding.inContent.llAudioContainer.visibility = View.VISIBLE
                binding.inContent.inAudioPlayer.btPlay.setOnClickListener {
                    onPlayAudio(card.cardContent.contentAudio, binding.inContent.inAudioPlayer)
                }
            } else {
                binding.inContent.llAudioContainer.visibility = View.GONE
            }

            binding.tvCardType.text = card.cardType
            bindHintOnLightTheme(card, context)

            if (card.cardType == SINGLE_ANSWER_CARD) {
                if (card.flipCount == 0) {
                    if (!card.isFlipped) {
                        binding.cvCardContainer.alpha = 1f
                        binding.cvCardContainer.rotationY = 0f
                        binding.cvCardContainerBack.alpha = 0f
                    } else {
                        binding.cvCardContainer.alpha = 0f
                        binding.cvCardContainer.rotationY = 0f
                        binding.cvCardContainerBack.rotationY = 0f
                        binding.cvCardContainerBack.alpha = 1f
                    }
                } else {
                    flipCard(card.isFlipped)
                }
            } else {
                binding.cvCardContainer.alpha = 1f
                binding.cvCardContainer.rotationY = 0f
                binding.cvCardContainerBack.alpha = 1f
                alternatives.forEachIndexed { index, alternative ->
                    if (index < card.cardDefinition.size) {
                        alternative.container.visibility = View.VISIBLE
                        if (card.cardDefinition[index].isSelected) {
                            val answerStatus = card.cardDefinition[index].isCorrect != 0
                            onAlternativeClicked(
                                alternative.view,
                                alternative.container,
                                card.cardType!!,
                                context,
                                answerStatus,
                            )
                        } else {
                            onButtonUnClicked(alternative.view, alternative.container, card.cardType!!, context)
                        }
                    } else {
                        alternative.container.visibility = View.GONE
                    }
                }
            }
            bindAnswerAlternatives(card, cardOnClick)
            when (AppThemeHelper.getSavedTheme(context)) {
                1 -> {
                    bindHintOnLightTheme(card, context)
                }
                2 -> {
                    bindHintOnDarkTheme(card, context)
                }
                else -> {
                    if (AppThemeHelper.isSystemDarkTheme(context)) {
                        bindHintOnDarkTheme(card, context)
                    } else {
                        bindHintOnLightTheme(card, context)
                    }
                }
            }
        }

        private fun bindHintOnLightTheme(
            card: QuizGameCardModel,
            context: Context
        ) {
            when {
                card.cardType == SINGLE_ANSWER_CARD -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    binding.tvHint.text =
                        ContextCompat.getString(context, R.string.text_tap_to_flip)
                    binding.tvHint.setTextColor(
                        MaterialColors.getColor(
                            itemView,
                            com.google.android.material.R.attr.colorOnSurfaceVariant
                        )
                    )
                }

                card.attemptTime == 0 -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text = ContextCompat.getString(context, R.string.text_not_answered)
                    binding.tvHint.setTextColor(
                        MaterialColors.getColor(
                            itemView,
                            com.google.android.material.R.attr.colorOnSurfaceVariant
                        )
                    )
                }

                card.attemptTime > 0 && card.isCorrectlyAnswered -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text = ContextCompat.getString(context, R.string.text_correct)
                    binding.tvHint.setTextColor(ContextCompat.getColor(context, R.color.green500))
                }

                card.attemptTime > 0 && hasCardCorrectAnswer(card) && !card.isCorrectlyAnswered -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text = ContextCompat.getString(context, R.string.text_correct_answer_more)
                    binding.tvHint.setTextColor(ContextCompat.getColor(context, R.color.yellow500))
                }

                card.attemptTime > 0 && !card.isCorrectlyAnswered -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text =
                        ContextCompat.getString(context, R.string.text_wrong_answer)
                    binding.tvHint.setTextColor(ContextCompat.getColor(context, R.color.red500))
                }
            }
        }

        private fun bindHintOnDarkTheme(
            card: QuizGameCardModel,
            context: Context
        ) {
            when {
                card.cardType == SINGLE_ANSWER_CARD -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    binding.tvHint.text =
                        ContextCompat.getString(context, R.string.text_tap_to_flip)
                    binding.tvHint.setTextColor(
                        MaterialColors.getColor(
                            itemView,
                            com.google.android.material.R.attr.colorOnSurfaceVariant
                        )
                    )
                }

                card.attemptTime == 0 -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text =
                        ContextCompat.getString(context, R.string.text_not_answered)
                    binding.tvHint.setTextColor(
                        MaterialColors.getColor(
                            itemView,
                            com.google.android.material.R.attr.colorOnSurfaceVariant
                        )
                    )
                }

                card.attemptTime > 0 && card.isCorrectlyAnswered -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text = ContextCompat.getString(context, R.string.text_correct)
                    binding.tvHint.setTextColor(ContextCompat.getColor(context, R.color.green200))
                }

                card.attemptTime > 0 && hasCardCorrectAnswer(card) && !card.isCorrectlyAnswered -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text = ContextCompat.getString(context, R.string.text_correct_answer_more)
                    binding.tvHint.setTextColor(ContextCompat.getColor(context, R.color.yellow200))
                }

                card.attemptTime > 0 && !card.isCorrectlyAnswered -> {
                    binding.tvHint.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    binding.tvHint.text =
                        ContextCompat.getString(context, R.string.text_wrong_answer)
                    binding.tvHint.setTextColor(ContextCompat.getColor(context, R.color.red200))
                }
            }
        }

        private fun hasCardCorrectAnswer(card: QuizGameCardModel): Boolean {
            card.cardDefinition.forEach { d ->
                if (d.isSelected && d.isCorrect == 1) {
                    return true
                }
            }
            return false
        }

        private fun FrameLayout.onAlternativeClicked(
            card: QuizGameCardModel,
            answer: QuizGameCardDefinitionModel,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit,
        ) {
            setOnClickListener {
                if (!card.isCorrectlyAnswered) {
                    answer.isSelected = !answer.isSelected
                    cardOnClick(answer)
                } else {
                    Toast.makeText(context, context.getString(R.string.message_on_card_already_answered), Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun onAlternativeClicked(
            alternative: LyQuizAlternativeBinding,
            container: View,
            cardType: String,
            context: Context,
            isCorrectlyAnswered: Boolean,
        ) {

            when (AppThemeHelper.getSavedTheme(context)) {
                1 -> {
                    onAlternativeClickedOnLightTheme(isCorrectlyAnswered, cardType, alternative, container, context)
                }
                2 -> {
                    onAlternativeClickedOnDarkTheme(isCorrectlyAnswered, cardType, alternative, container, context)
                }
                else -> {
                    if (AppThemeHelper.isSystemDarkTheme(context)) {
                        onAlternativeClickedOnDarkTheme(isCorrectlyAnswered, cardType, alternative, container, context)
                    } else {
                        onAlternativeClickedOnLightTheme(isCorrectlyAnswered, cardType, alternative, container, context)
                    }
                }
            }
        }

        private fun onAlternativeClickedOnLightTheme(
            isCorrectlyAnswered: Boolean,
            cardType: String,
            alternative: LyQuizAlternativeBinding,
            container: View,
            context: Context
        ) {
            if (isCorrectlyAnswered) {
                if (cardType == MULTIPLE_ANSWER_CARD) {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_check_box)
                } else {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_radio_button_checked)
                }
                container.background.setTint(ContextCompat.getColor(context, R.color.green500))
                alternative.imgLeadingIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.green50),
                    PorterDuff.Mode.SRC_IN
                )
                alternative.tvText.setTextColor(ContextCompat.getColor(context, R.color.green50))
            } else {
                if (cardType == MULTIPLE_ANSWER_CARD) {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_check_box_wrong)
                } else {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_cancel)
                }
                container.background.setTint(ContextCompat.getColor(context, R.color.red500))
                alternative.imgLeadingIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.red50),
                    PorterDuff.Mode.SRC_IN
                )
                alternative.tvText.setTextColor(ContextCompat.getColor(context, R.color.red50))
            }
        }

        private fun onAlternativeClickedOnDarkTheme(
            isCorrectlyAnswered: Boolean,
            cardType: String,
            alternative: LyQuizAlternativeBinding,
            container: View,
            context: Context
        ) {
            if (isCorrectlyAnswered) {
                if (cardType == MULTIPLE_ANSWER_CARD) {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_check_box)
                } else {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_radio_button_checked)
                }

                container.background.setTint(ContextCompat.getColor(context, R.color.green700))
                alternative.imgLeadingIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.green50),
                    PorterDuff.Mode.SRC_IN
                )
                alternative.tvText.setTextColor(ContextCompat.getColor(context, R.color.red50))
            } else {
                if (cardType == MULTIPLE_ANSWER_CARD) {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_check_box_wrong)
                } else {
                    alternative.imgLeadingIcon.setImageResource(R.drawable.icon_cancel)
                }
                container.background.setTint(ContextCompat.getColor(context, R.color.red700))
                alternative.imgLeadingIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.red50),
                    PorterDuff.Mode.SRC_IN
                )
                alternative.tvText.setTextColor(ContextCompat.getColor(context, R.color.red50))
            }
        }

        private fun onButtonUnClicked(
            view: LyQuizAlternativeBinding,
            container: View,
            cardType: String,
            context: Context
        ) {
            if (cardType == MULTIPLE_ANSWER_CARD) {
                view.imgLeadingIcon.setImageResource(R.drawable.icon_check_box_outline_blank)
            } else {
                view.imgLeadingIcon.setImageResource(R.drawable.icon_radio_button_unchecked)
            }
            container.background.setTint(MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerLowest,
                Color.GRAY
            ))

            view.imgLeadingIcon.setColorFilter(MaterialColors.getColor(
                view.imgLeadingIcon,
                com.google.android.material.R.attr.colorOnSurface
            ))
            view.tvText.setTextColor(MaterialColors.getColor(view.tvText, com.google.android.material.R.attr.colorOnSurface))
        }

        private fun bindAnswerAlternatives(
            card: QuizGameCardModel,
            cardOnClick: (QuizGameCardDefinitionModel) -> Unit
        ) {
            if (card.cardType == SINGLE_ANSWER_CARD) {

                binding.flCardRoot.isClickable = true
                alternatives.forEach { alternative ->
                    alternative.container.visibility = View.GONE
                }

                if (card.cardDefinition.first().definition != null) {
                    val spannableString = Html.fromHtml(card.cardDefinition.first().definition, FROM_HTML_MODE_LEGACY).trim()
                    binding.inDefinition.tvText.apply {
                        visibility = View.VISIBLE
                        text = spannableString
                    }
                } else {
                    binding.inDefinition.tvText.visibility = View.GONE
                }

                if (card.cardDefinition.first().definitionImage != null) {
                    val photoFile = File(context.filesDir, card.cardDefinition.first().definitionImage?.name!!)
                    val photoBytes = photoFile.readBytes()
                    val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                    binding.inDefinition.imgPhoto.apply {
                        visibility = View.VISIBLE
                        setImageBitmap(photoBtm)
                    }
                } else {
                    binding.inDefinition.imgPhoto.visibility = View.GONE
                }

                if (card.cardDefinition.first().definitionAudio != null) {
                    binding.inDefinition.llAudioContainer.visibility = View.VISIBLE
                    binding.inDefinition.inAudioPlayer.btPlay.setOnClickListener {
                        onPlayAudio(card.cardDefinition.first().definitionAudio!!,  binding.inDefinition.inAudioPlayer)
                    }
                } else {
                    binding.inDefinition.llAudioContainer.visibility = View.GONE
                }

                binding.tvCardTypeBack.text = card.cardType
                binding.fronCardInnerContainer.setOnClickListener {
                    card.cardDefinition.first().isSelected = true
                    cardOnClick(
                        card.cardDefinition.first()
                    )
                }
                binding.flCardRoot.setOnClickListener {
                    card.cardDefinition.first().isSelected = true
                    cardOnClick(
                        card.cardDefinition.first()
                    )
                }
                binding.btSpeakBack.setOnClickListener {
                    onSpeak(
                        QuizSpeakModel(
                            text = listOf(
                                TextWithLanguageModel(
                                    card.cardId,
                                    Html.fromHtml(card.cardDefinition.first().definition, FROM_HTML_MODE_LEGACY).toString(),
                                    DEFINITION,
                                    card.cardDefinitionLanguage
                                )
                            ),
                            views = listOf(binding.inDefinition.tvText),
                        )
                    )
                }
                binding.btSpeak.setOnClickListener {
                    frontSpeak(
                        listOf(binding.inContent.tvText),
                        listOf(
                            TextWithLanguageModel(
                                card.cardId,
                                binding.inContent.tvText.text.toString(),
                                CONTENT,
                                card.cardContentLanguage
                            )
                        ),
                    )
                }
            } else {
                binding.flCardRoot.isClickable = false
                val texts = arrayListOf(
                    TextWithLanguageModel(
                        card.cardId,
                        Html.fromHtml(card.cardContent.contentText, FROM_HTML_MODE_LEGACY).toString(),
                        CONTENT,
                        card.cardContentLanguage ?: deck.cardContentDefaultLanguage
                    )
                )
                val views = arrayListOf(binding.inContent.tvText)
                alternatives.forEachIndexed { index, alternative ->
                    if (index < card.cardDefinition.size) {
                        alternative.container.visibility = View.VISIBLE

                        if (card.cardDefinition[index].definition != null) {
                            val spannableString = Html.fromHtml(card.cardDefinition[index].definition, FROM_HTML_MODE_LEGACY).trim()
                            alternative.view.tvText.apply {
                                visibility = View.VISIBLE
                                text = spannableString
                            }
                        } else {
                            alternative.view.tvText.visibility = View.GONE
                        }

                        if (card.cardDefinition[index].definitionImage != null) {
                            val photoFile = File(context.filesDir, card.cardDefinition[index].definitionImage?.name!!)
                            val photoBytes = photoFile.readBytes()
                            val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                            alternative.view.imgPhoto.apply {
                                visibility = View.VISIBLE
                                setImageBitmap(photoBtm)
                            }
                        } else {
                            alternative.view.imgPhoto.visibility = View.GONE
                        }

                        if (card.cardDefinition[index].definitionAudio != null) {
                            alternative.view.llContainerAudioPlayer.visibility = View.VISIBLE
                            alternative.view.inAudioPlayer.btPlay.setOnClickListener {
                                onPlayAudio(card.cardDefinition[index].definitionAudio!!,  alternative.view.inAudioPlayer)
                            }
                        } else {
                            alternative.view.llContainerAudioPlayer.visibility = View.GONE
                        }

                        (alternative.container as FrameLayout).onAlternativeClicked(
                            card = card,
                            answer = card.cardDefinition[index],
                            cardOnClick = cardOnClick
                        )
                        texts.add(
                            TextWithLanguageModel(
                                card.cardDefinition[index].cardId,
                                Html.fromHtml(card.cardDefinition[index].definition, FROM_HTML_MODE_LEGACY).toString(),
                                DEFINITION,
                                card.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
                            )
                        )
                        views.add(alternative.view.tvText)
                    } else {
                        alternative.container.visibility = View.GONE
                    }
                }
                frontSpeak(views, texts)
            }
        }

        private fun frontSpeak(
            views: List<TextView>,
            texts: List<TextWithLanguageModel>,
        ) {
            binding.btSpeak.setOnClickListener {
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
            binding.cvCardContainer.cameraDistance = 8000 * scale
            binding.cvCardContainerBack.cameraDistance = 8000 * scale
            if (isFlipped) {
                frontAnim.setTarget(binding.cvCardContainer)
                backAnim.setTarget(binding.cvCardContainerBack)
                frontAnim.start()
                backAnim.start()
            } else {
                frontAnim.setTarget(binding.cvCardContainerBack)
                backAnim.setTarget(binding.cvCardContainer)
                frontAnim.start()
                backAnim.start()
            }
        }

    }
}