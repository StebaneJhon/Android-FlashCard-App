package com.soaharisonstebane.mneme.help

import android.content.Context
import androidx.lifecycle.ViewModel
import com.soaharisonstebane.mneme.util.FAQData

class HelpFragmentViewModel: ViewModel() {

    fun getFaqData(context: Context) = FAQData(context).getFAQData()

}