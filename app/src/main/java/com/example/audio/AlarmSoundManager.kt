package com.example.audio

import android.content.Context
import android.database.ContentObserver
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import com.example.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

class AlarmSoundManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var synthJob: Job? = null
    private var previewJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var volumeObserver: ContentObserver? = null
    private var isEnforcingVolume = false

    data class BuiltInSound(
        val id: String,
        val fileName: String,
        val displayName: String,
        val description: String,
        val tag: String = "Loud"
    )

    companion object {
        val FALLBACK_SOUNDS = listOf(
            BuiltInSound("extreme_siren", "classic-Alert.wav", "🚨 Extreme Siren Alert", "Piercing multi-pitch siren sweeps (Impossible to sleep)", "Emergency"),
            BuiltInSound("atomic_evacuation", "Coooooo.mp3", "☢️ Nuclear Evacuation Alarm", "Deep relentless warning warhorn beats", "Aggressive"),
            BuiltInSound("police_pursuit", "Alert.mp3", "🚓 Police Pursuit Warble", "Rapid alternating high-frequency wake tones", "Piercing"),
            BuiltInSound("rooster_loud", "chicken.wav", "🐓 Super Loud Morning Rooster", "Lively aggressive wake-up morning harmonics", "Loud"),
            BuiltInSound("energetic_siren", "li-li-funny-sound.mp3", "😂 Lili Lili Funny Sound", "High-tempo vibrant pulsing synth melody", "Funny"),
            BuiltInSound("sunrise_bells", "Kiring-Kiring.mp3", "🔔 Sunrise Acoustic Bells", "Resonant bright morning harmony bells", "Acoustic")
        )

        private var cachedSounds: List<BuiltInSound>? = null

        fun loadSounds(context: Context): List<BuiltInSound> {
            cachedSounds?.let { return it }

            val loadedList = try {
                val inputStream = context.assets.open("sounds.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                val list = mutableListOf<BuiltInSound>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        BuiltInSound(
                            id = obj.getString("id"),
                            fileName = obj.optString("fileName", "${obj.getString("id")}.wav"),
                            displayName = obj.getString("displayName"),
                            description = obj.getString("description"),
                            tag = obj.optString("tag", "Loud")
                        )
                    )
                }
                if (list.isNotEmpty()) list else FALLBACK_SOUNDS
            } catch (e: Exception) {
                Log.d("AlarmSoundManager", "sounds.json not found or failed parsing, using fallback sounds: ${e.message}")
                FALLBACK_SOUNDS
            }

            cachedSounds = loadedList
            return loadedList
        }

        @Suppress("DEPRECATION")
        val BUILT_IN_SOUNDS = FALLBACK_SOUNDS
    }

    /**
     * Starts continuous alarm playback with volume enforcement and vibration.
     */
    fun startAlarmRinging(
        soundType: SoundType,
        soundUriOrName: String,
        isVibrate: Boolean
    ) {
        stopAll()

        // 1. Maximize Volume and enforce
        maximizeVolume()
        startVolumeEnforcement()

        // 2. Start sound playback
        if (soundType == SoundType.CUSTOM && soundUriOrName.startsWith("content://")) {
            playCustomUri(Uri.parse(soundUriOrName), isLooping = true)
        } else {
            playBuiltInOrAssetSound(soundUriOrName, isLooping = true)
        }

        // 3. Start Vibration
        if (isVibrate) {
            startVibration()
        }
    }

    /**
     * Preview sound for 5 seconds in create/edit screen
     */
    fun previewSound(soundType: SoundType, soundUriOrName: String, onFinished: () -> Unit) {
        stopPreview()
        previewJob = scope.launch {
            if (soundType == SoundType.CUSTOM && soundUriOrName.startsWith("content://")) {
                withContextUi {
                    playCustomUri(Uri.parse(soundUriOrName), isLooping = false)
                }
            } else {
                playBuiltInOrAssetSound(soundUriOrName, isLooping = false)
            }
            delay(5000)
            stopPreview()
            withContextUi { onFinished() }
        }
    }

    private fun playBuiltInOrAssetSound(soundKey: String, isLooping: Boolean) {
        val sounds = loadSounds(context)
        val soundInfo = sounds.find { it.id == soundKey }
        val fileName = soundInfo?.fileName?.takeIf { it.isNotBlank() } ?: "$soundKey.wav"

        try {
            stopSoundOnly()
            val afd = context.assets.openFd(fileName)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.w("AlarmSoundManager", "Could not play asset sound $fileName, falling back to synthesizer", e)
            playBuiltInSynthesized(soundKey, isLooping)
        }
    }

    fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        stopSoundOnly()
    }

    fun stopAll() {
        stopVolumeEnforcement()
        stopSoundOnly()
        stopVibration()
    }

    private fun stopSoundOnly() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
        synthJob?.cancel()
        synthJob = null
    }

    private fun maximizeVolume() {
        try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
            val maxMusicVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVol, 0)
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Could not set max volume", e)
        }
    }

    /**
     * Volume Enforcement: Ensures volume stays maxed while ringing
     */
    private fun startVolumeEnforcement() {
        if (isEnforcingVolume) return
        isEnforcingVolume = true

        volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                if (isEnforcingVolume) {
                    maximizeVolume()
                }
            }
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                volumeObserver!!
            )
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Failed to register volume observer", e)
        }
    }

    private fun stopVolumeEnforcement() {
        isEnforcingVolume = false
        volumeObserver?.let {
            try {
                context.contentResolver.unregisterContentObserver(it)
            } catch (e: Exception) {
                Log.e("AlarmSoundManager", "Failed to unregister volume observer", e)
            }
            volumeObserver = null
        }
    }

    private fun startVibration() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 600, 300, 600, 300, 1000, 400)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 600, 300, 600, 300, 1000, 400), 0)
            }
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Error stopping vibrator", e)
        }
    }

    private fun playCustomUri(uri: Uri, isLooping: Boolean) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Failed to play custom URI, falling back to default", e)
            playDefaultRingtone(isLooping)
        }
    }

    private fun playDefaultRingtone(isLooping: Boolean) {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alertUri)
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            playBuiltInSynthesized("extreme_siren", isLooping)
        }
    }

    /**
     * Synthesizes rich, loud melodic and alarm tones via AudioTrack PCM audio in real-time.
     */
    private fun playBuiltInSynthesized(soundKey: String, isLooping: Boolean) {
        synthJob?.cancel()
        synthJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            try {
                audioTrack.play()

                val notes: List<Pair<Double, Int>> = when (soundKey) {
                    "extreme_siren" -> listOf(
                        Pair(1200.0, 100), Pair(1500.0, 100), Pair(1800.0, 100), Pair(2200.0, 120),
                        Pair(1800.0, 90), Pair(1400.0, 90), Pair(1100.0, 90), Pair(2400.0, 160),
                        Pair(0.0, 40)
                    )
                    "atomic_evacuation" -> listOf(
                        Pair(440.0, 300), Pair(880.0, 250), Pair(330.0, 300), Pair(990.0, 350),
                        Pair(0.0, 150), Pair(1100.0, 200), Pair(0.0, 100)
                    )
                    "heavy_fire" -> listOf(
                        Pair(1760.0, 70), Pair(0.0, 30), Pair(1760.0, 70), Pair(0.0, 30),
                        Pair(2093.0, 80), Pair(0.0, 40), Pair(2637.0, 120), Pair(0.0, 80)
                    )
                    "police_pursuit" -> listOf(
                        Pair(900.0, 120), Pair(1350.0, 120), Pair(1800.0, 140), Pair(1350.0, 120),
                        Pair(900.0, 120), Pair(0.0, 50)
                    )
                    "rooster_loud" -> listOf(
                        Pair(554.37, 120), Pair(783.99, 140), Pair(1046.50, 220), Pair(1318.51, 380),
                        Pair(0.0, 300), Pair(1046.50, 200), Pair(0.0, 200)
                    )
                    "energetic_siren" -> listOf(
                        Pair(587.33, 160), Pair(880.00, 160), Pair(1174.66, 160),
                        Pair(880.00, 160), Pair(587.33, 160), Pair(0.0, 120)
                    )
                    "digital_pulse" -> listOf(
                        Pair(1046.50, 90), Pair(0.0, 40), Pair(1046.50, 90),
                        Pair(0.0, 40), Pair(1318.51, 160), Pair(0.0, 200)
                    )
                    "sunrise_bells" -> listOf(
                        Pair(523.25, 250), Pair(659.25, 250), Pair(783.99, 350),
                        Pair(1046.50, 450), Pair(0.0, 300)
                    )
                    "zen_flute" -> listOf(
                        Pair(440.00, 300), Pair(523.25, 300), Pair(587.33, 350),
                        Pair(659.25, 400), Pair(783.99, 450), Pair(0.0, 300)
                    )
                    "cosmic_dawn" -> listOf(
                        Pair(392.00, 200), Pair(493.88, 200), Pair(587.33, 220),
                        Pair(739.99, 300), Pair(880.00, 400), Pair(0.0, 250)
                    )
                    else -> listOf( // gentle_melody
                        Pair(440.00, 220), Pair(554.37, 220), Pair(659.25, 260),
                        Pair(880.00, 380), Pair(739.99, 260), Pair(659.25, 400),
                        Pair(0.0, 400)
                    )
                }

                do {
                    for (note in notes) {
                        if (!isActive) break
                        val freq = note.first
                        val durationMs = note.second
                        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                        val samples = ShortArray(numSamples)

                        if (freq > 0) {
                            for (i in 0 until numSamples) {
                                val t = i.toDouble() / sampleRate
                                val attack = min(1.0, i.toDouble() / (sampleRate * 0.015))
                                val decay = min(1.0, (numSamples - i).toDouble() / (sampleRate * 0.02))
                                val envelope = attack * decay
                                // Add dual harmonic for rich and piercing loudness
                                val fundamental = sin(2.0 * PI * freq * t)
                                val harmonic2 = 0.4 * sin(4.0 * PI * freq * t)
                                val harmonic3 = 0.2 * sin(6.0 * PI * freq * t)
                                val wave = (fundamental + harmonic2 + harmonic3) * envelope * 0.95
                                val sample = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                                samples[i] = sample.toShort()
                            }
                        }

                        var offset = 0
                        while (offset < numSamples && isActive) {
                            val written = audioTrack.write(
                                samples,
                                offset,
                                numSamples - offset,
                                AudioTrack.WRITE_BLOCKING
                            )
                            if (written <= 0) break
                            offset += written
                        }
                    }
                } while (isLooping && isActive)
            } catch (e: Exception) {
                Log.e("AlarmSoundManager", "Error in synthesized playback", e)
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) { }
            }
        }
    }

    private suspend fun withContextUi(action: suspend () -> Unit) {
        kotlinx.coroutines.withContext(Dispatchers.Main) {
            action()
        }
    }
}
