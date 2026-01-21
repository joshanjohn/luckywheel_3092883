package com.griffith.luckywheel.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
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
    
    private var currentVolume: Float = 0.5f
    private var isMuted: Boolean = false

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
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                Log.d(TAG, "Music playing")
            }
        }
    }

    private fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                Log.d(TAG, "Music paused")
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        serviceScope.launch {
            // Signal cleanup if needed
        }
        Log.d(TAG, "Service destroyed")
    }
}
