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
        "Afrikaans" to TranslateLanguage.AFRIKAANS,
        "Arabic" to TranslateLanguage.ARABIC,
        "Belarusian" to TranslateLanguage.BELARUSIAN,
        "Bulgarian" to TranslateLanguage.BULGARIAN,
        "Bengali" to TranslateLanguage.BENGALI,
        "Catalan" to TranslateLanguage.CATALAN,
        "Czech" to TranslateLanguage.CZECH,
        "Welsh" to TranslateLanguage.WELSH,
        "Danish" to TranslateLanguage.DANISH,
        "German" to TranslateLanguage.GERMAN,
        "Greek" to TranslateLanguage.GREEK,
        "English" to TranslateLanguage.ENGLISH,
        "Esperanto" to TranslateLanguage.ESPERANTO,
        "Spanish" to TranslateLanguage.SPANISH,
        "Estonian" to TranslateLanguage.ESTONIAN,
        "Persian" to TranslateLanguage.PERSIAN,
        "Finnish" to TranslateLanguage.FINNISH,
        "French" to TranslateLanguage.FRENCH,
        "Irish" to TranslateLanguage.IRISH,
        "Galician" to TranslateLanguage.GALICIAN,
        "Gujarati" to TranslateLanguage.GUJARATI,
        "Hebrew" to TranslateLanguage.HEBREW,
        "Hindi" to TranslateLanguage.HINDI,
        "Croatian" to TranslateLanguage.CROATIAN,
        "Haitian" to TranslateLanguage.HAITIAN_CREOLE,
        "Hungarian" to TranslateLanguage.HUNGARIAN,
        "Indonesian" to TranslateLanguage.INDONESIAN,
        "Icelandic" to TranslateLanguage.ICELANDIC,
        "Italian" to TranslateLanguage.ITALIAN,
        "Japanese" to TranslateLanguage.JAPANESE,
        "Georgian" to TranslateLanguage.GEORGIAN,
        "Kannada" to TranslateLanguage.KANNADA,
        "Korean" to TranslateLanguage.KOREAN,
        "Lithuanian" to TranslateLanguage.LITHUANIAN,
        "Latvian" to TranslateLanguage.LATVIAN,
        "Macedonian" to TranslateLanguage.MACEDONIAN,
        "Marathi" to TranslateLanguage.MARATHI,
        "Malay" to TranslateLanguage.MALAY,
        "Maltese" to TranslateLanguage.MALTESE,
        "Dutch" to TranslateLanguage.DUTCH,
        "Norwegian" to TranslateLanguage.NORWEGIAN,
        "Polish" to TranslateLanguage.POLISH,
        "Portuguese" to TranslateLanguage.PORTUGUESE,
        "Romanian" to TranslateLanguage.ROMANIAN,
        "Russian" to TranslateLanguage.RUSSIAN,
        "Slovak" to TranslateLanguage.SLOVAK,
        "Slovenian" to TranslateLanguage.SLOVENIAN,
        "Albanian" to TranslateLanguage.ALBANIAN,
        "Swedish" to TranslateLanguage.SWEDISH,
        "Swahili" to TranslateLanguage.SWAHILI,
        "Tamil" to TranslateLanguage.TAMIL,
        "Telugu" to TranslateLanguage.TELUGU,
        "Thai" to TranslateLanguage.THAI,
        "Tagalog" to TranslateLanguage.TAGALOG,
        "Turkish" to TranslateLanguage.TURKISH,
        "Ukrainian" to TranslateLanguage.UKRAINIAN,
        "Urdu" to TranslateLanguage.URDU,
        "Vietnamese" to TranslateLanguage.VIETNAMESE,
        "Chinese" to TranslateLanguage.CHINESE
    )

    fun getLanguageCode(language: String) =
        if (language in languages.keys) {
            languages[language]
        } else {
            null
        }

    fun getSupportedLang(): Array<String> {
        val languages = languages.keys
        return languages.toTypedArray()
    }
}