package com.ssoaharison.recall.util

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.ssoaharison.recall.helper.InternetChecker
import java.util.Locale

class LanguageUtil {
    private val languages = mapOf(
        "Afrikaans" to mapOf("langCodeTranslation" to TranslateLanguage.AFRIKAANS, "langCodeSpeechAndText" to "af"),
        "Arabic" to mapOf("langCodeTranslation" to TranslateLanguage.ARABIC, "langCodeSpeechAndText" to "ar-SA"),
        "Belarusian" to mapOf("langCodeTranslation" to TranslateLanguage.BELARUSIAN, "langCodeSpeechAndText" to "be"),
        "Bulgarian" to mapOf("langCodeTranslation" to TranslateLanguage.BULGARIAN, "langCodeSpeechAndText" to "bg"),
        "Bengali" to mapOf("langCodeTranslation" to TranslateLanguage.BENGALI, "langCodeSpeechAndText" to "bn"),
        "Catalan" to mapOf("langCodeTranslation" to TranslateLanguage.CATALAN, "langCodeSpeechAndText" to "ca"),
        "Czech" to mapOf("langCodeTranslation" to TranslateLanguage.CZECH, "langCodeSpeechAndText" to "cs-CZ"),
        "Welsh" to mapOf("langCodeTranslation" to TranslateLanguage.WELSH, "langCodeSpeechAndText" to "cy"),
        "Danish" to mapOf("langCodeTranslation" to TranslateLanguage.DANISH, "langCodeSpeechAndText" to "da-DK"),
        "German" to mapOf("langCodeTranslation" to TranslateLanguage.GERMAN, "langCodeSpeechAndText" to "de-DE"),
        "German (Austria)" to mapOf("langCodeTranslation" to TranslateLanguage.GERMAN, "langCodeSpeechAndText" to "de-AT"),
        "German (Switzerland)" to mapOf("langCodeTranslation" to TranslateLanguage.GERMAN, "langCodeSpeechAndText" to "de-CH"),
        "Greek" to mapOf("langCodeTranslation" to TranslateLanguage.GREEK, "langCodeSpeechAndText" to "el-GR"),
        "English (Australia)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-AU"),
        "English (Canada)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-CA"),
        "English (United Kingdom)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-GB"),
        "English (Ireland)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-IE"),
        "English (India)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-IN"),
        "English (New Zealand)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-NZ"),
        "English (United States)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-US"),
        "English (South Africa)" to mapOf("langCodeTranslation" to TranslateLanguage.ENGLISH, "langCodeSpeechAndText" to "en-ZA"),
        "Esperanto" to mapOf("langCodeTranslation" to TranslateLanguage.ESPERANTO, "langCodeSpeechAndText" to "eo"),
        "Spanish (Argentina)" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es-AR"),
        "Spanish (Chile)" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es-CL"),
        "Spanish (Columbia)" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es-CO"),
        "Spanish (Spain)" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es-ES"),
        "Spanish (Mexico)" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es-MX"),
        "Spanish (United States)" to mapOf("langCodeTranslation" to TranslateLanguage.SPANISH, "langCodeSpeechAndText" to "es-US"),
        "Estonian" to mapOf("langCodeTranslation" to TranslateLanguage.ESTONIAN, "langCodeSpeechAndText" to "et"),
        "Persian" to mapOf("langCodeTranslation" to TranslateLanguage.PERSIAN, "langCodeSpeechAndText" to "fa"),
        "Finnish" to mapOf("langCodeTranslation" to TranslateLanguage.FINNISH, "langCodeSpeechAndText" to "fi-FI"),
        "French (Belgium)" to mapOf("langCodeTranslation" to TranslateLanguage.FRENCH,  "langCodeSpeechAndText" to "fr-BE"),
        "French (Canada)" to mapOf("langCodeTranslation" to TranslateLanguage.FRENCH,  "langCodeSpeechAndText" to "fr-CA"),
        "French (Switzerland)" to mapOf("langCodeTranslation" to TranslateLanguage.FRENCH,  "langCodeSpeechAndText" to "fr-CH"),
        "French (France)" to mapOf("langCodeTranslation" to TranslateLanguage.FRENCH,  "langCodeSpeechAndText" to "fr-FR"),
        "Irish" to mapOf("langCodeTranslation" to TranslateLanguage.IRISH, "langCodeSpeechAndText" to "ga"),
        "Galician" to mapOf("langCodeTranslation" to TranslateLanguage.GALICIAN, "langCodeSpeechAndText" to "gl"),
        "Gujarati" to mapOf("langCodeTranslation" to TranslateLanguage.GUJARATI, "langCodeSpeechAndText" to "gu"),
        "Hebrew" to mapOf("langCodeTranslation" to TranslateLanguage.HEBREW, "langCodeSpeechAndText" to "he-IL"),
        "Hindi" to mapOf("langCodeTranslation" to TranslateLanguage.HINDI, "langCodeSpeechAndText" to "hi-IN"),
        "Croatian" to mapOf("langCodeTranslation" to TranslateLanguage.CROATIAN, "langCodeSpeechAndText" to "hr"),
        "Haitian" to mapOf("langCodeTranslation" to TranslateLanguage.HAITIAN_CREOLE, "langCodeSpeechAndText" to "fr_HT"),
        "Hungarian" to mapOf("langCodeTranslation" to TranslateLanguage.HUNGARIAN, "langCodeSpeechAndText" to "hu-HU"),
        "Indonesian" to mapOf("langCodeTranslation" to TranslateLanguage.INDONESIAN, "langCodeSpeechAndText" to "id-ID"),
        "Icelandic" to mapOf("langCodeTranslation" to TranslateLanguage.ICELANDIC, "langCodeSpeechAndText" to "is"),
        "Italian (Switzerland)" to mapOf("langCodeTranslation" to TranslateLanguage.ITALIAN, "langCodeSpeechAndText" to "it-CH"),
        "Italian (Italy)" to mapOf("langCodeTranslation" to TranslateLanguage.ITALIAN, "langCodeSpeechAndText" to "it-IT"),
        "Japanese" to mapOf("langCodeTranslation" to TranslateLanguage.JAPANESE, "langCodeSpeechAndText" to "ja-JP"),
        "Georgian" to mapOf("langCodeTranslation" to TranslateLanguage.GEORGIAN, "langCodeSpeechAndText" to "ka"),
        "Kannada" to mapOf("langCodeTranslation" to TranslateLanguage.KANNADA, "langCodeSpeechAndText" to "kn"),
        "Korean" to mapOf("langCodeTranslation" to TranslateLanguage.KOREAN, "langCodeSpeechAndText" to "ko-KR"),
        "Lithuanian" to mapOf("langCodeTranslation" to TranslateLanguage.LITHUANIAN, "langCodeSpeechAndText" to "lt"),
        "Latvian" to mapOf("langCodeTranslation" to TranslateLanguage.LATVIAN, "langCodeSpeechAndText" to "lv"),
        "Macedonian" to mapOf("langCodeTranslation" to TranslateLanguage.MACEDONIAN, "langCodeSpeechAndText" to "mk"),
        "Marathi" to mapOf("langCodeTranslation" to TranslateLanguage.MARATHI, "langCodeSpeechAndText" to "mr"),
        "Malay" to mapOf("langCodeTranslation" to TranslateLanguage.MALAY, "langCodeSpeechAndText" to "ms"),
        "Maltese" to mapOf("langCodeTranslation" to TranslateLanguage.MALTESE, "langCodeSpeechAndText" to "en_MT"),
        "Dutch (Belgium)" to mapOf("langCodeTranslation" to TranslateLanguage.DUTCH, "langCodeSpeechAndText" to "nl-BE"),
        "Dutch (The Netherlands)" to mapOf("langCodeTranslation" to TranslateLanguage.DUTCH, "langCodeSpeechAndText" to "nl-NL"),
        "Norwegian" to mapOf("langCodeTranslation" to TranslateLanguage.NORWEGIAN, "langCodeSpeechAndText" to "no-NO"),
        "Polish" to mapOf("langCodeTranslation" to TranslateLanguage.POLISH, "langCodeSpeechAndText" to "pl-PL"),
        "Portuguese (Brazil)" to mapOf("langCodeTranslation" to TranslateLanguage.PORTUGUESE, "langCodeSpeechAndText" to "pt-BR"),
        "Portuguese (Portugal)" to mapOf("langCodeTranslation" to TranslateLanguage.PORTUGUESE, "langCodeSpeechAndText" to "pt-PT"),
        "Romanian" to mapOf("langCodeTranslation" to TranslateLanguage.ROMANIAN, "langCodeSpeechAndText" to "ro-RO"),
        "Russian" to mapOf("langCodeTranslation" to TranslateLanguage.RUSSIAN, "langCodeSpeechAndText" to "ru-RU"),
        "Slovak" to mapOf("langCodeTranslation" to TranslateLanguage.SLOVAK, "langCodeSpeechAndText" to "sk-SK"),
        "Slovenian" to mapOf("langCodeTranslation" to TranslateLanguage.SLOVENIAN, "langCodeSpeechAndText" to "sl"),
        "Albanian" to mapOf("langCodeTranslation" to TranslateLanguage.ALBANIAN, "langCodeSpeechAndText" to "sq"),
        "Swedish" to mapOf("langCodeTranslation" to TranslateLanguage.SWEDISH, "langCodeSpeechAndText" to "sv-SE"),
        "Swahili" to mapOf("langCodeTranslation" to TranslateLanguage.SWAHILI, "langCodeSpeechAndText" to "sw"),
        "Tamil (India)" to mapOf("langCodeTranslation" to TranslateLanguage.TAMIL, "langCodeSpeechAndText" to "ta-IN"),
        "Tamil (Sri Lanka)" to mapOf("langCodeTranslation" to TranslateLanguage.TAMIL, "langCodeSpeechAndText" to "ta-LK"),
        "Telugu" to mapOf("langCodeTranslation" to TranslateLanguage.TELUGU, "langCodeSpeechAndText" to "te"),
        "Thai" to mapOf("langCodeTranslation" to TranslateLanguage.THAI, "langCodeSpeechAndText" to "th-TH"),
        "Tagalog" to mapOf("langCodeTranslation" to TranslateLanguage.TAGALOG, "langCodeSpeechAndText" to "tl-PH"),
        "Turkish" to mapOf("langCodeTranslation" to TranslateLanguage.TURKISH, "langCodeSpeechAndText" to "tr-TR"),
        "Ukrainian" to mapOf("langCodeTranslation" to TranslateLanguage.UKRAINIAN, "langCodeSpeechAndText"  to "ru-UA"),
        "Urdu" to mapOf("langCodeTranslation" to TranslateLanguage.URDU, "langCodeSpeechAndText" to "ur"),
        "Vietnamese" to mapOf("langCodeTranslation" to TranslateLanguage.VIETNAMESE, "langCodeSpeechAndText" to "vi"),
        "Chinese (China)" to mapOf("langCodeTranslation" to TranslateLanguage.CHINESE, "langCodeSpeechAndText" to "zh-CN"),
        "Chinese (Hong Kong)" to mapOf("langCodeTranslation" to TranslateLanguage.CHINESE, "langCodeSpeechAndText" to "zh-CN"),
        "Chinese (Taiwan)" to mapOf("langCodeTranslation" to TranslateLanguage.CHINESE, "langCodeSpeechAndText" to "zh-TW"),
    )

    fun getLanguageCodeForTranslation(language: String?) =
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

    fun getLanguageCodeForTextToSpeech(language: String) =
        if (language in languages.keys) {
            languages[language]?.get("langCodeSpeechAndText")
        } else {
            null
        }

    fun isLanguageSupported(language: String) = language in getSupportedLang()

    fun getLanguageByCode(code: String): String? {
        languages.forEach { (t, u) ->
            if (u["langCodeTranslation"] == code) {
                return t
            }
        }
        return null
    }

    fun getLocalLanguage(): String? {
        languages.forEach { (t, u) ->
            if (u["langCodeTranslation"] == Locale.getDefault().language) {
                return t
            }
        }
        return null
    }

    fun detectLanguage(
        text: String,
        onError: (String?) -> Unit,
        onLanguageUnIdentified: (String?) -> Unit,
        onLanguageNotSupported: () -> Unit,
        onSuccess: (String) -> Unit
    ) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    onLanguageUnIdentified(getLocalLanguage())
                } else {
                    val language = getLanguageByCode(languageCode)
                    if (language.isNullOrBlank()) {
                        onLanguageNotSupported()
                    } else {
                        onSuccess(language)
                    }
                }
            }
            .addOnFailureListener {
                onError(getLocalLanguage())
            }
    }

    fun startTranslation(
        text: String,
        definitionLanguage: String?,
        contentLanguage: String?,
        onStartTranslation: (TranslationLanguagesMode) -> Unit,
        onLanguageDetectionLanguageNotSupported: () -> Unit,
    ) {
        val tl = if (definitionLanguage.isNullOrBlank()) null else getLanguageCodeForTranslation(definitionLanguage)
        if (contentLanguage.isNullOrBlank()) {
            detectLanguage(
                text = text,
                onError = { localLanguage ->
                    onStartTranslation(
                        TranslationLanguagesMode(
                            getLanguageCodeForTranslation(localLanguage),
                            tl
                        )
                    )
                },
                onLanguageUnIdentified = { localLanguage ->
                    onStartTranslation(
                        TranslationLanguagesMode(
                            getLanguageCodeForTranslation(localLanguage),
                            tl
                        )
                    )
                },
                onLanguageNotSupported = {
                    onLanguageDetectionLanguageNotSupported()
                },
                onSuccess = { detectedLanguage ->
                    onStartTranslation(
                        TranslationLanguagesMode(
                            getLanguageCodeForTranslation(detectedLanguage),
                            tl
                        )
                    )
                },
            )
        } else {
            onStartTranslation(
                TranslationLanguagesMode(
                    getLanguageCodeForTranslation(contentLanguage),
                    tl
                )
            )
        }
    }

    fun prepareTranslation(
        context: Context,
        fl: String?,
        tl: String?,
        onDownloadingLanguageMode: () -> Unit,
        onMissingCardDefinitionLanguage: () -> Unit,
        onModelDownloadingFailure: () -> Unit,
        onSuccess: (TranslatorModel) -> Unit,
        onNoInternet: () -> Unit,
        onInternetViaCellular: (TranslatorModel) -> Unit
    ) {
        if (fl != null && tl != null) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(fl)
                .setTargetLanguage(tl)
                .build()
            val appTranslator = Translation.getClient(options)
            var conditions: DownloadConditions
            val modelManager = RemoteModelManager.getInstance()
            modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { models ->
                    val modelsLanguages = models.map { it.language }
                    if (fl !in modelsLanguages || tl !in modelsLanguages) {
                        onDownloadingLanguageMode()
                        InternetChecker().isOnline(
                            context,
                            onNoInternet = { onNoInternet() },
                            onInternetViaEthernet = {
                                conditions = DownloadConditions.Builder()
                                    .requireWifi()
                                    .build()
                                onSuccess(TranslatorModel(conditions, appTranslator))
                            },
                            onInternetViaWifi = {
                                conditions = DownloadConditions.Builder()
                                    .requireWifi()
                                    .build()
                                onSuccess(TranslatorModel(conditions, appTranslator))
                            },
                            onInternetViaCellular = {
                                conditions = DownloadConditions.Builder()
                                    .build()
                                onInternetViaCellular(TranslatorModel(conditions, appTranslator) )
                            }
                        )
                    } else {
                        conditions = DownloadConditions.Builder()
                            .requireWifi()
                            .build()
                        onSuccess(TranslatorModel(conditions, appTranslator))
                    }
                }
                .addOnFailureListener {
                    onModelDownloadingFailure()
                }
        } else {
            onMissingCardDefinitionLanguage()
        }
    }

    fun translate(
        appTranslator: Translator,
        conditions: DownloadConditions,
        text: String,
        onSuccess: (String) -> Unit,
        onTranslationFailure: (Exception) -> Unit,
        onModelDownloadingFailure: () -> Unit
    ) {
        appTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                appTranslator.translate(text)
                    .addOnSuccessListener { translation ->
                        onSuccess(translation)
                    }
                    .addOnFailureListener { exception ->
                        onTranslationFailure(exception)
                    }
            }
            .addOnFailureListener {
                onModelDownloadingFailure()
            }
    }

}

data class TranslationLanguagesMode(
    val fl: String?,
    val tl: String?
)

data class TranslatorModel(
    val condition: DownloadConditions,
    val appTranslator: Translator
)