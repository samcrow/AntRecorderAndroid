package org.samcrow.neofelis.antrecorder

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class ButtonSounds(ctx: Context) {
    private val sounds: SoundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build()
        )
        .setMaxStreams(2)
        .build()
    private val inSoundId: Int
    private val outSoundId: Int

    init {
        inSoundId = sounds.load(ctx, R.raw.ping_high, 1)
        outSoundId = sounds.load(ctx, R.raw.ping_low, 1)
    }

    fun playInSound() {
        sounds.play(inSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }
    fun playOutSound() {
        sounds.play(outSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun release() {
        sounds.release()
    }
}