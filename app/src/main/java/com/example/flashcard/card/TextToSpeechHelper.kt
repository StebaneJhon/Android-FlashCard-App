package com.example.flashcard.card

import com.google.mlkit.nl.translate.TranslateLanguage

class TextToSpeechHelper {

    private val languages = mapOf(
        "Afrikaans" to mapOf("languageCode" to TranslateLanguage.AFRIKAANS),
        "Arabic" to mapOf("languageCode" to TranslateLanguage.ARABIC),
        "Belarusian" to mapOf("languageCode" to TranslateLanguage.BELARUSIAN),
        "Bulgarian" to mapOf("languageCode" to TranslateLanguage.BULGARIAN),
        "Bengali" to mapOf("languageCode" to TranslateLanguage.BENGALI),
        "Catalan" to mapOf("languageCode" to TranslateLanguage.CATALAN),
        "Czech" to mapOf("languageCode" to TranslateLanguage.CZECH),
        "Welsh" to mapOf("languageCode" to TranslateLanguage.WELSH),
        "Danish" to mapOf("languageCode" to TranslateLanguage.DANISH),
        "German" to mapOf("languageCode" to TranslateLanguage.GERMAN),
        "Greek" to mapOf("languageCode" to TranslateLanguage.GREEK),
        "English" to mapOf("languageCode" to TranslateLanguage.ENGLISH),
        "Esperanto" to mapOf("languageCode" to TranslateLanguage.ESPERANTO),
        "Spanish" to mapOf("languageCode" to TranslateLanguage.SPANISH),
        "Estonian" to mapOf("languageCode" to TranslateLanguage.ESTONIAN,),
        "Persian" to mapOf("languageCode" to TranslateLanguage.PERSIAN),
        "Finnish" to mapOf("languageCode" to TranslateLanguage.FINNISH),
        "French" to mapOf("languageCode" to TranslateLanguage.FRENCH),
        "Irish" to mapOf("languageCode" to TranslateLanguage.IRISH),
        "Galician" to mapOf("languageCode" to TranslateLanguage.GALICIAN),
        "Gujarati" to mapOf("languageCode" to TranslateLanguage.GUJARATI),
        "Hebrew" to mapOf("languageCode" to TranslateLanguage.HEBREW),
        "Hindi" to mapOf("languageCode" to TranslateLanguage.HINDI),
        "Croatian" to mapOf("languageCode" to TranslateLanguage.CROATIAN),
        "Haitian" to mapOf("languageCode" to TranslateLanguage.HAITIAN_CREOLE),
        "Hungarian" to mapOf("languageCode" to TranslateLanguage.HUNGARIAN),
        "Indonesian" to mapOf("languageCode" to TranslateLanguage.INDONESIAN),
        "Icelandic" to mapOf("languageCode" to TranslateLanguage.ICELANDIC),
        "Italian" to mapOf("languageCode" to TranslateLanguage.ITALIAN),
        "Japanese" to mapOf("languageCode" to TranslateLanguage.JAPANESE),
        "Georgian" to mapOf("languageCode" to TranslateLanguage.GEORGIAN),
        "Kannada" to mapOf("languageCode" to TranslateLanguage.KANNADA),
        "Korean" to mapOf("languageCode" to TranslateLanguage.KOREAN),
        "Lithuanian" to mapOf("languageCode" to TranslateLanguage.LITHUANIAN),
        "Latvian" to mapOf("languageCode" to TranslateLanguage.LATVIAN),
        "Macedonian" to mapOf("languageCode" to TranslateLanguage.MACEDONIAN),
        "Marathi" to mapOf("languageCode" to TranslateLanguage.MARATHI),
        "Malay" to mapOf("languageCode" to TranslateLanguage.MALAY),
        "Maltese" to mapOf("languageCode" to TranslateLanguage.MALTESE),
        "Dutch" to mapOf("languageCode" to TranslateLanguage.DUTCH),
        "Norwegian" to mapOf("languageCode" to TranslateLanguage.NORWEGIAN),
        "Polish" to mapOf("languageCode" to TranslateLanguage.POLISH),
        "Portuguese" to mapOf("languageCode" to TranslateLanguage.PORTUGUESE),
        "Romanian" to mapOf("languageCode" to TranslateLanguage.ROMANIAN),
        "Russian" to mapOf("languageCode" to TranslateLanguage.RUSSIAN),
        "Slovak" to mapOf("languageCode" to TranslateLanguage.SLOVAK),
        "Slovenian" to mapOf("languageCode" to TranslateLanguage.SLOVENIAN),
        "Albanian" to mapOf("languageCode" to TranslateLanguage.ALBANIAN),
        "Swedish" to mapOf("languageCode" to TranslateLanguage.SWEDISH),
        "Swahili" to mapOf("languageCode" to TranslateLanguage.SWAHILI),
        "Tamil" to mapOf("languageCode" to TranslateLanguage.TAMIL),
        "Telugu" to mapOf("languageCode" to TranslateLanguage.TELUGU),
        "Thai" to mapOf("languageCode" to TranslateLanguage.THAI),
        "Tagalog" to mapOf("languageCode" to TranslateLanguage.TAGALOG),
        "Turkish" to mapOf("languageCode" to TranslateLanguage.TURKISH),
        "Ukrainian" to mapOf("languageCode" to TranslateLanguage.UKRAINIAN),
        "Urdu" to mapOf("languageCode" to TranslateLanguage.URDU),
        "Vietnamese" to mapOf("languageCode" to TranslateLanguage.VIETNAMESE),
        "Chinese" to mapOf("languageCode" to TranslateLanguage.CHINESE)
    )

    fun getSupportedLang(): Array<String> {
        val languages = languages.keys
        return languages.toTypedArray()
    }

    fun getLanguageCodeForTextToSpeech(language: String) =
        if (language in languages.keys) {
            languages[language]?.get("languageCode")
        } else {
            null
        }

}