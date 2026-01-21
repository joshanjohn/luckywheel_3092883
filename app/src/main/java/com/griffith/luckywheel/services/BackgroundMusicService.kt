package com.griffith.luckywheel.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.griffith.luckywheel.MainActivity
import com.griffith.luckywheel.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Foreground music service for reliable background playback
class BackgroundMusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var dataStoreService: DataStoreService
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private var currentVolume: Float = 0.5f
    private var isMuted: Boolean = false
    private var shouldBePlaying: Boolean = false // Track if we should be playing

    companion object {
        private const val CHANNEL_ID = "MusicServiceChannel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "BackgroundMusicService"
        
        const val ACTION_PLAY = "com.griffith.luckywheel.PLAY"
        const val ACTION_PAUSE = "com.griffith.luckywheel.PAUSE"
        const val ACTION_STOP = "com.griffith.luckywheel.STOP"
    }

    override fun onCreate() {
        super.onCreate()
        dataStoreService = DataStoreService(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        loadPreferencesAndInitialize()
    }

    private fun loadPreferencesAndInitialize() {
        serviceScope.launch {
            launch {
                dataStoreService.getMusicVolume().collect { volume ->
                    currentVolume = volume
                    updateVolume()
                }
            }
            launch {
                dataStoreService.getMusicMuted().collect { muted ->
                    isMuted = muted
                    updateVolume()
                }
            }
            initializePlayer()
        }
    }

    private fun initializePlayer() {
        try {
            val resourceId = resources.getIdentifier("bgmusic", "raw", packageName)
            if (resourceId != 0) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(this@BackgroundMusicService, android.net.Uri.parse("android.resource://$packageName/$resourceId"))
                    isLooping = true
                    prepare()
                    updateVolume()
                }
                Log.d(TAG, "Music player initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize player", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stopSelf()
            else -> {
                // Default behavior: start and play
                startForeground(NOTIFICATION_ID, createNotification())
                play()
            }
        }
        return START_STICKY
    }

    private fun play() {
        if (mediaPlayer == null) initializePlayer()
        
        // Request audio focus before playing
        val result = requestAudioFocus()
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            shouldBePlaying = true
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    Log.d(TAG, "Music playing")
                }
            }
        } else {
            Log.w(TAG, "Audio focus not granted")
        }
    }

    private fun pause() {
        shouldBePlaying = false
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                Log.d(TAG, "Music paused")
            }
        }
        // Don't abandon audio focus here - we might resume soon
    }

    private fun updateVolume() {
        val vol = if (isMuted) 0f else currentVolume
        mediaPlayer?.setVolume(vol, vol)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Background Music Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lucky Wheel Arcade")
            .setContentText("Keeping the magic alive...")
            .setSmallIcon(R.mipmap.lucky_wheel_logo)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun requestAudioFocus(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback if we should be playing
                if (shouldBePlaying) {
                    mediaPlayer?.start()
                    updateVolume()
                    Log.d(TAG, "Audio focus gained, resuming")
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback
                pause()
                abandonAudioFocus()
                Log.d(TAG, "Audio focus lost")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time: pause playback
                mediaPlayer?.pause()
                Log.d(TAG, "Audio focus lost transient")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost focus but can duck (lower volume)
                mediaPlayer?.setVolume(currentVolume * 0.2f, currentVolume * 0.2f)
                Log.d(TAG, "Audio focus duck")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shouldBePlaying = false
        abandonAudioFocus()
        mediaPlayer?.release()
        mediaPlayer = null
        serviceScope.launch {
            // Signal cleanup if needed
        }
        Log.d(TAG, "Service destroyed")
    }
}
