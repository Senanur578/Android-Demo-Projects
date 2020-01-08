package com.testingviews.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.testingviews.R


const val PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID"
const val PRIMARY_CHANNEL_NAME = "PRIMARY"
const val NOTIFICATION_ID = 555

class MediaNotificationManager(service: AudioService) {

    private var audioService: AudioService? = null
    private var notificationManager: NotificationManagerCompat? = null
    private var resources: Resources? = null

    init {
        notificationManager = NotificationManagerCompat.from(service)
        audioService = service
        resources = service.resources
    }

    fun startNotify() { // playbackStatus: String
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_discover_genre)
        var icon: Int = R.drawable.ic_pause

        val playbackAction = Intent(audioService, AudioService::class.java)
        playbackAction.action = audioService?.ACTION_PAUSE
        var action: PendingIntent = PendingIntent.getService(audioService, 1, playbackAction, 0)

        if (audioService?.getStatus()!!) {
            icon = R.drawable.ic_play_arrow
            playbackAction.action = audioService?.ACTION_PLAY
            action = PendingIntent.getService(audioService, 2, playbackAction, 0)
        }

        val stopIntent = Intent(audioService, AudioService::class.java)
        stopIntent.action = audioService?.ACTION_STOP
        val stopAction: PendingIntent = PendingIntent.getService(audioService, 3, stopIntent, 0)

        val intent = Intent(audioService, AudioService::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(audioService, 0, intent, 0)

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(audioService?.getMediaSession()?.sessionToken)
            .setShowActionsInCompactView(0, 1)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopAction)
        notificationManager?.cancel(NOTIFICATION_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager: NotificationManager =
                audioService?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                PRIMARY_CHANNEL,
                PRIMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(channel)
        }


        val builder: Notification? = audioService?.let {
            NotificationCompat.Builder(it, PRIMARY_CHANNEL)
                .setAutoCancel(false)
                .setContentTitle("Title")
                .setContentText("Text")
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_radio)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_stop, "stop", stopAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setStyle(mediaStyle)
                .build()
        }

        audioService?.startForeground(NOTIFICATION_ID, builder)
    }

    fun cancelNotify() {
        audioService?.stopForeground(true)
    }


}