package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class SoundHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w("SoundHelper", "ToneGenerator init failed", e)
        }

        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.JAPANESE)
                    isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                    tts?.setPitch(1.0f)
                    tts?.setSpeechRate(0.9f)
                }
            }
        } catch (e: Exception) {
            Log.w("SoundHelper", "TTS init failed", e)
        }
    }

    fun speakJapanese(text: String) {
        if (!isTtsReady || tts == null) return
        val clean = text.replace(Regex("[^\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}ーa-zA-Z0-9]"), "")
        if (clean.isNotBlank()) {
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "tts_id_${System.currentTimeMillis()}")
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
