package com.ssoaharison.recall.util

import android.content.Context
import com.ssoaharison.recall.R

class FAQData(context: Context) {
    private val faqData = listOf(
        FAQDataModel(context.getString(R.string.question_what_is_mneme), context.getString(R.string.answer_what_is_mneme)),
        FAQDataModel(context.getString(R.string.question_how_to_use_mneme), context.getString(R.string.answer_how_to_use_mneme)),
        FAQDataModel(context.getString(R.string.question_how_to_optimize_memorization), context.getString(R.string.answer_how_to_optimize_memorization)),
        FAQDataModel(context.getString(R.string.question_how_does_mneme_help_in_memorization), context.getString(R.string.answer_how_does_mneme_help_in_memorization)),
    )
    fun getFAQData() = faqData
}