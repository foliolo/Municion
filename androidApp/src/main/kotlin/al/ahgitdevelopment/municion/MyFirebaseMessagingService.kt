package al.ahgitdevelopment.municion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives FCM pushes and shows a notification (notification payloads while the app is foreground;
 * background payloads are auto-displayed by the system). Mirrors the `develop` behaviour. iOS push
 * is handled separately via APNs + a Swift coordinator (see MIGRATION_REPORT §4.F).
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.body?.let { sendNotification(it) }
    }

    private fun sendNotification(messageBody: String) {
        val intent =
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(notificationManager)

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .build()

        notificationManager.notify(0, notification)
    }

    private fun ensureChannel(manager: NotificationManager) {
        // minSdk is 26 (Oreo), so the notification channel is always required.
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    private companion object {
        const val CHANNEL_ID = "fcm_default_channel"
    }
}
