package com.example.focusbuilder.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.focusbuilder.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private var tapSoundId: Int = 0
    private var successSoundId: Int = 0
    private var retrySoundId: Int = 0

    fun init(context: Context) {
        if (soundPool != null) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let {
            tapSoundId = it.load(context, R.raw.tap, 1)
            successSoundId = it.load(context, R.raw.success, 1)
            retrySoundId = it.load(context, R.raw.retry, 1)
        }
    }

    fun playTap() {
        soundPool?.play(tapSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playSuccess() {
        soundPool?.play(successSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playRetry() {
        soundPool?.play(retrySoundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
