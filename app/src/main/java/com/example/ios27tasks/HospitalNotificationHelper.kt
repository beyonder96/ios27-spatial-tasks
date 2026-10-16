package com.example.ios27tasks

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object HospitalNotificationHelper {

    const val CHANNEL_ID = "hospital_care_notifications_channel"
    private const val CHANNEL_NAME = "Hotelaria Hospitalar & Plantão"
    private const val CHANNEL_DESC = "Lembretes de rotinas de leitos, passagens de plantão e metas de autocuidado"

    /**
     * Cria o canal de notificações obrigatório para Android 8.0 (API 26) ou superior.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = 0xFF00C7BE.toInt() // Glacial Teal
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Verifica se o aplicativo possui permissão ativa para emitir notificações no aparelho.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Envia uma notificação formatada do aplicativo de Hotelaria Hospitalar.
     */
    fun sendHospitalNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 1001
    ): Boolean {
        createNotificationChannels(context)

        if (!hasNotificationPermission(context)) {
            return false
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIconRes = android.R.drawable.ic_popup_reminder

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF00C7BE.toInt()) // Glacial Teal
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Dispara uma notificação de confirmação e teste para a usuária.
     */
    fun sendTestNotification(context: Context): Boolean {
        return sendHospitalNotification(
            context = context,
            title = "🏨 Hotelaria Hospitalar — Notificações Ativas",
            message = "Tudo pronto! Você receberá lembretes de rotinas de leitos, passagens de plantão e pausas saudáveis de autocuidado.",
            notificationId = 1002
        )
    }
}
