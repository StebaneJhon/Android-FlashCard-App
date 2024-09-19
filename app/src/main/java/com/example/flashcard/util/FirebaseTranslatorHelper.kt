package com.example.flashcard.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

class FirebaseTranslatorHelper {
    private val languages = mapOf(
        "Afrikaans" to mapOf("langCodeTranslation" to TranslateLanguage.AFRIKAANS, "langCodeSpeechAndText" to "af_"),
        "Arabic" to mapOf("langCodeTranslation" to TranslateLanguage.ARABIC, "langCodeSpeechAndText" to "ar_"),
        "Belarusian" to mapOf("langCodeTranslation" to TranslateLanguage.BELARUSIAN, "langCodeSpeechAndText" to "be_"),
        "Bulgarian" to mapOf("langCodeTranslation" to TranslateLanguage.BULGARIAN, "langCodeSpeechAndText" to "bg_"),
        "Bengali" to mapOf("langCodeTranslation" to TranslateLanguage.BENGALI, "langCodeSpeechAndText" to "bn_"),
        "Catalan" to mapOf("langCodeTranslation" to TranslateLanguage.CATALAN, "langCodeSpeechAndText" to "ca_"),
        "Czech" to mapOf("langCodeTranslation" to TranslateLanguage.CZECH, "langCodeSpeechAndText" to "cs_"),
        "Welsh" to mapOf("langCodeTranslation" to TranslateLanguage.WELSH, "langCodeSpeechAndText" to "cy_"),
        "Danish" to mapOf("langCodeTranslation" to TranslateLanguage.DANISH, "langCodeSpeechAndText" to "da_"),
        "German" to mapOf("langCodeTranslation" to TranslateLanguage.GERMAN, "langCodeSpeechAndText" to "de_"),
        "Greek" to mapOf("langCodeTranslation" to TranslateLanguage.GREEK, "langCodeSpeechAndText" to "el_"),
        "English" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en_"),
        "Esperanto" to mapOf("langCodeTranslation" to TranslateLanguage.ESPERANTO, "langCodeSpeechAndText" to "eo_"),
        "Spanish" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es_"),
        "Estonian" to mapOf("langCodeTranslation" to TranslateLanguage.ESTONIAN, "langCodeSpeechAndText" to "et_"),
        "Persian" to mapOf("langCodeTranslation" to TranslateLanguage.PERSIAN, "langCodeSpeechAndText" to "fa_"),
        "Finnish" to mapOf("langCodeTranslation" to TranslateLanguage.FINNISH, "langCodeSpeechAndText" to "fi_"),
        "French" to mapOf("langCodeTranslation" to TranslateLanguage.FRENCH,  "langCodeSpeechAndText" to "fr_"),
        "Irish" to mapOf("langCodeTranslation" to TranslateLanguage.IRISH, "langCodeSpeechAndText" to "ga_"),
        "Galician" to mapOf("langCodeTranslation" to TranslateLanguage.GALICIAN, "langCodeSpeechAndText" to "gl_"),
        "Gujarati" to mapOf("langCodeTranslation" to TranslateLanguage.GUJARATI, "langCodeSpeechAndText" to "gu_"),
        "Hebrew" to mapOf("langCodeTranslation" to TranslateLanguage.HEBREW, "langCodeSpeechAndText" to "iw_"),
        "Hindi" to mapOf("langCodeTranslation" to TranslateLanguage.HINDI, "langCodeSpeechAndText" to "hi_"),
        "Croatian" to mapOf("langCodeTranslation" to TranslateLanguage.CROATIAN, "langCodeSpeechAndText" to "hr_"),
        "Haitian" to mapOf("langCodeTranslation" to TranslateLanguage.HAITIAN_CREOLE, "langCodeSpeechAndText" to "fr_HT"),
        "Hungarian" to mapOf("langCodeTranslation" to TranslateLanguage.HUNGARIAN, "langCodeSpeechAndText" to "hu_"),
        "Indonesian" to mapOf("langCodeTranslation" to TranslateLanguage.INDONESIAN, "langCodeSpeechAndText" to "in_"),
        "Icelandic" to mapOf("langCodeTranslation" to TranslateLanguage.ICELANDIC, "langCodeSpeechAndText" to "is_"),
        "Italian" to mapOf("langCodeTranslation" to TranslateLanguage.ITALIAN, "langCodeSpeechAndText" to "it_"),
        "Japanese" to mapOf("langCodeTranslation" to TranslateLanguage.JAPANESE, "langCodeSpeechAndText" to "ja_"),
        "Georgian" to mapOf("langCodeTranslation" to TranslateLanguage.GEORGIAN, "langCodeSpeechAndText" to "ka_"),
        "Kannada" to mapOf("langCodeTranslation" to TranslateLanguage.KANNADA, "langCodeSpeechAndText" to ""),
        "Korean" to mapOf("langCodeTranslation" to TranslateLanguage.KOREAN, "langCodeSpeechAndText" to "ko_"),
        "Lithuanian" to mapOf("langCodeTranslation" to TranslateLanguage.LITHUANIAN, "langCodeSpeechAndText" to "lt_"),
        "Latvian" to mapOf("langCodeTranslation" to TranslateLanguage.LATVIAN, "langCodeSpeechAndText" to "lv_"),
        "Macedonian" to mapOf("langCodeTranslation" to TranslateLanguage.MACEDONIAN, "langCodeSpeechAndText" to "mk_"),
        "Marathi" to mapOf("langCodeTranslation" to TranslateLanguage.MARATHI, "langCodeSpeechAndText" to "mr_"),
        "Malay" to mapOf("langCodeTranslation" to TranslateLanguage.MALAY, "langCodeSpeechAndText" to "ms_"),
        "Maltese" to mapOf("langCodeTranslation" to TranslateLanguage.MALTESE, "langCodeSpeechAndText" to "en_MT"),
        "Dutch" to mapOf("langCodeTranslation" to TranslateLanguage.DUTCH, "langCodeSpeechAndText" to "nl_"),
        "Norwegian" to mapOf("langCodeTranslation" to TranslateLanguage.NORWEGIAN, "langCodeSpeechAndText" to "nb_"),
        "Polish" to mapOf("langCodeTranslation" to TranslateLanguage.POLISH, "langCodeSpeechAndText" to "pl_"),
        "Portuguese" to mapOf("langCodeTranslation" to TranslateLanguage.PORTUGUESE, "langCodeSpeechAndText" to "pt_"),
        "Romanian" to mapOf("langCodeTranslation" to TranslateLanguage.ROMANIAN, "langCodeSpeechAndText" to "ro_"),
        "Russian" to mapOf("langCodeTranslation" to TranslateLanguage.RUSSIAN, "langCodeSpeechAndText" to "ru_"),
        "Slovak" to mapOf("langCodeTranslation" to TranslateLanguage.SLOVAK, "langCodeSpeechAndText" to "sk_"),
        "Slovenian" to mapOf("langCodeTranslation" to TranslateLanguage.SLOVENIAN, "langCodeSpeechAndText" to "sl_"),
        "Albanian" to mapOf("langCodeTranslation" to TranslateLanguage.ALBANIAN, "langCodeSpeechAndText" to "sq_"),
        "Swedish" to mapOf("langCodeTranslation" to TranslateLanguage.SWEDISH, "langCodeSpeechAndText" to "sv_"),
        "Swahili" to mapOf("langCodeTranslation" to TranslateLanguage.SWAHILI, "langCodeSpeechAndText" to "sw_"),
        "Tamil" to mapOf("langCodeTranslation" to TranslateLanguage.TAMIL, "langCodeSpeechAndText" to "ta_"),
        "Telugu" to mapOf("langCodeTranslation" to TranslateLanguage.TELUGU, "langCodeSpeechAndText" to "te_"),
        "Thai" to mapOf("langCodeTranslation" to TranslateLanguage.THAI, "langCodeSpeechAndText" to "th_"),
        "Tagalog" to mapOf("langCodeTranslation" to TranslateLanguage.TAGALOG, "langCodeSpeechAndText" to "tl_PH"),
        "Turkish" to mapOf("langCodeTranslation" to TranslateLanguage.TURKISH, "langCodeSpeechAndText" to "tr_"),
        "Ukrainian" to mapOf("langCodeTranslation" to TranslateLanguage.UKRAINIAN, "langCodeSpeechAndText"  to "ru_UA"),
        "Urdu" to mapOf("langCodeTranslation" to TranslateLanguage.URDU, "langCodeSpeechAndText" to "ur_"),
        "Vietnamese" to mapOf("langCodeTranslation" to TranslateLanguage.VIETNAMESE, "langCodeSpeechAndText" to "vi_"),
        "Chinese" to mapOf("langCodeTranslation" to TranslateLanguage.CHINESE, "langCodeSpeechAndText" to "zh_")
    )

    fun getLanguageCodeForTranslation(language: String) =
        if (language in languages.keys) {
            languages[language]?.get("langCodeTranslation")
        } else {
            null
        }

    fun getSupportedLang(): Array<String> {
        val languages = languages.keys
        return languages.toTypedArray()
    }

    fun getLanguageCodeForSpeechAndText(language: String) =
        if (language in languages.keys) {
            languages[language]?.get("langCodeSpeechAndText")
        } else {
            null
        }

    fun isLanguageSupported(language: String) = language in getSupportedLang()
}