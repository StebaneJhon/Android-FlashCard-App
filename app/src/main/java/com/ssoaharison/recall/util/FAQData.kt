package com.ssoaharison.recall.util

import android.content.Context
import com.ssoaharison.recall.R

class FAQData(context: Context) {
    private val faqData = listOf(
        FAQDataModel(context.getString(R.string.question_what_is_recall), context.getString(R.string.answer_what_is_recall)),
        FAQDataModel(context.getString(R.string.question_how_to_use_recall), context.getString(R.string.answer_how_to_use_recall)),
        FAQDataModel(context.getString(R.string.question_in_a_few_sentence_how_does_memory_work), context.getString(R.string.answer_in_a_few_sentence_how_does_memory_work)),
        FAQDataModel(context.getString(R.string.question_how_does_recall_help_in_memorization), context.getString(R.string.answer_how_does_recall_help_in_memorization)),
        FAQDataModel(context.getString(R.string.question_what_is_spaced_repetition), context.getString(R.string.answer_what_is_spaced_repetition)),
        FAQDataModel(context.getString(R.string.question_what_are_mnemonics), context.getString(R.string.answer_what_are_mnemonics)),
    )
    fun getFAQData() = faqData
}