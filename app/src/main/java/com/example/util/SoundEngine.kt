package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-synthesized sound PCM arrays
    private val footstepPcm: ShortArray by lazy { synthesizeFootstep() }
    private val collisionPcm: ShortArray by lazy { synthesizeCollision() }
    private val slashPcm: ShortArray by lazy { synthesizeSlash() }
    private val shootPcm: ShortArray by lazy { synthesizeShoot() }
    private val alarmPcm: ShortArray by lazy { synthesizeAlarm() }
    private val gemPcm: ShortArray by lazy { synthesizeGem() }

    fun playFootstep() = playPcm(footstepPcm)
    fun playCollision() = playPcm(collisionPcm)
    fun playSlash() = playPcm(slashPcm)
    fun playShoot() = playPcm(shootPcm)
    fun playAlarm() = playPcm(alarmPcm)
    fun playGem() = playPcm(gemPcm)

    private fun playPcm(pcm: ShortArray) {
        scope.launch {
            try {
                val sampleRate = 22050
                val channelConfig = AudioFormat.CHANNEL_OUT_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT

                val minSizeInBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                val minSizeInShorts = minSizeInBytes / 2
                val finalPcm = if (pcm.size < minSizeInShorts) {
                    ShortArray(minSizeInShorts).apply {
                        System.arraycopy(pcm, 0, this, 0, pcm.size)
                    }
                } else {
                    pcm
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(finalPcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(finalPcm, 0, finalPcm.size)
                audioTrack.play()

                // Wait for the active sound length to complete
                val activeDurationMs = (pcm.size * 1000L) / sampleRate
                delay(activeDurationMs + 60L)

                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun synthesizeFootstep(): ShortArray {
        val duration = 0.04f // 40ms short thud
        val sampleRate = 22050
        val numSamples = (sampleRate * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 110f - 70f * (t / duration) // Descending sweep
            val envelope = 1.0f - (t / duration)
            val angle = 2.0 * PI * freq * t
            val sineVal = sin(angle)
            val noiseVal = (Math.random() * 2.0 - 1.0) * 0.12f
            buffer[i] = ((sineVal + noiseVal) * envelope * 8500).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeCollision(): ShortArray {
        val duration = 0.09f // 90ms heavy damp thud
        val sampleRate = 22050
        val numSamples = (sampleRate * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 150f - 110f * (t / duration) // 150Hz down to 40Hz
            val envelope = 1.0f - (t / duration)
            val angle = 2.0 * PI * freq * t
            val sineVal = sin(angle)
            val noiseVal = (Math.random() * 2.0 - 1.0) * 0.22f
            buffer[i] = ((sineVal + noiseVal) * envelope * 12500).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeSlash(): ShortArray {
        val duration = 0.16f // 160ms slash slice
        val sampleRate = 22050
        val numSamples = (sampleRate * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 1900f - 1500f * (t / duration) // Swift slice sweep
            val envelope = sin((t / duration).toDouble() * PI).toFloat() * (1.0f - (t / duration))
            val angle = 2.0 * PI * freq * t
            val waveVal = if (sin(angle) > 0) 1.0f else -1.0f
            val noiseVal = (Math.random() * 2.0 - 1.0).toFloat() * 0.45f
            buffer[i] = ((waveVal * 0.28f + noiseVal * 0.72f) * envelope * 13500).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeShoot(): ShortArray {
        val duration = 0.18f // 180ms shooter blaster
        val sampleRate = 22050
        val numSamples = (sampleRate * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 2300f - 2100f * (t / duration)
            val envelope = 1.0f - (t / duration)
            val angle = 2.0 * PI * freq * t
            val waveVal = if (sin(angle) > 0) 1.0f else -1.0f
            val noiseVal = (Math.random() * 2.0 - 1.0).toFloat() * 0.15f
            buffer[i] = ((waveVal + noiseVal) * envelope * 10500).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeAlarm(): ShortArray {
        val duration = 0.25f // 250ms high dual-tone alert beep
        val sampleRate = 22050
        val numSamples = (sampleRate * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val baseFreq = if (t % 0.08f < 0.04f) 880f else 660f // alternating A5 and E5
            val envelope = 1.0f - (t / duration) * 0.5f // gradual drop-off
            val angle = 2.0 * PI * baseFreq * t
            val sineVal = sin(angle)
            buffer[i] = (sineVal * envelope * 10000).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeGem(): ShortArray {
        val duration = 0.15f // 150ms rewarding rising arpeggio
        val sampleRate = 22050
        val numSamples = (sampleRate * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val baseFreq = when {
                t < 0.05f -> 523.25f  // C5
                t < 0.10f -> 659.25f  // E5
                else -> 783.99f       // G5
            }
            val envelope = 1.0f - (t / duration)
            val angle = 2.0 * PI * baseFreq * t
            val sineVal = sin(angle)
            buffer[i] = (sineVal * envelope * 12500).toInt().toShort()
        }
        return buffer
    }
}
