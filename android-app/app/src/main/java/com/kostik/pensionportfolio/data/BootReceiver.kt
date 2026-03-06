package com.kostik.pensionportfolio.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Получатель уведомления о загрузке устройства
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Можно запустить сервис проверки уведомлений
            // val serviceIntent = Intent(context, CheckNotificationsService::class.java)
            // context.startForegroundService(serviceIntent)
        }
    }
}
