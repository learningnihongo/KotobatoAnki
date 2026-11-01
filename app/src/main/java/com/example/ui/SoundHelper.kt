package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SoundHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null
    private var pendingTextToSpeak: String? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w("SoundHelper", "ToneGenerator init failed", e)
        }

        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val supportedLocale = listOf(
                        Locale.JAPANESE,
                        Locale.JAPAN,
                        Locale("ja", "JP"),
                        Locale("ja")
                    ).firstOrNull { loc ->
                        val res = tts?.isLanguageAvailable(loc) ?: TextToSpeech.LANG_NOT_SUPPORTED
                        res != TextToSpeech.LANG_NOT_SUPPORTED && res != TextToSpeech.LANG_MISSING_DATA
                    } ?: Locale.JAPANESE

                    val setResult = tts?.setLanguage(supportedLocale)
                    isTtsReady = setResult != TextToSpeech.LANG_MISSING_DATA && setResult != TextToSpeech.LANG_NOT_SUPPORTED

                    tts?.setPitch(1.0f)
                    tts?.setSpeechRate(0.88f) // Clear, natural speed for language learners

                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                        }
                    })

                    // Speak any text queued before initialization finished
                    pendingTextToSpeak?.let { pending ->
                        pendingTextToSpeak = null
                        speakJapanese(pending)
                    }
                } else {
                    Log.w("SoundHelper", "TextToSpeech init status: $status")
                }
            }
        } catch (e: Exception) {
            Log.w("SoundHelper", "TTS init exception", e)
        }
    }

    /**
     * Speaks the provided Japanese text with natural pronunciation.
     * Extracts Japanese Hiragana, Katakana, Kanji, and alphanumeric characters.
     */
    fun speakJapanese(text: String) {
        val clean = text
            .replace(Regex("\\[.*?\\]"), "") // Remove bracketed metadata if any
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("[^\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}ーa-zA-Z0-9\\s]"), "")
            .trim()

        if (clean.isBlank()) return

        if (!isTtsReady || tts == null) {
            pendingTextToSpeak = clean
            return
        }

        try {
            val utteranceId = "tts_nihongo_${System.currentTimeMillis()}"
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("SoundHelper", "Error speaking text", e)
            _isSpeaking.value = false
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playCorrectSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playWrongSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playFlipSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (e: Exception) {
            // ignore
        }
    }
}
