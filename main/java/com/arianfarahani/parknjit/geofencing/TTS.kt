package com.arianfarahani.parknjit.geofencing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener
import java.util.*


class TTS : Service(), TextToSpeech.OnInitListener, OnUtteranceCompletedListener {
    private var mTts: TextToSpeech? = null
    private var spokenText: String? = null

    override fun onCreate() {
        mTts = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        spokenText = intent.getStringExtra(TEXT_INTENT_KEY)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = mTts!!.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                mTts!!.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }

    override fun onUtteranceCompleted(uttId: String) {
        stopSelf()
    }

    override fun onDestroy() {
        if (mTts != null) {
            mTts!!.stop()
            mTts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        val TEXT_INTENT_KEY = "tts_text"
    }
}