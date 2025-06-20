package com.ssoaharison.recall.help

import android.content.Context
import androidx.lifecycle.ViewModel
import com.ssoaharison.recall.util.FAQData

class HelpFragmentViewModel: ViewModel() {

    fun getFaqData(context: Context) = FAQData(context).getFAQData()

}