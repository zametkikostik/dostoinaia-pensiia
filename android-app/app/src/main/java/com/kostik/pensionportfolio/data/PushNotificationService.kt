package com.kostik.pensionportfolio.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.ui.MainActivity

/**
 * Сервис push-уведомлений
 */
class PushNotificationService : Service() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "pension_portfolio_channel"
        const val NOTIFICATION_ID_REBALANCE = 1
        const val NOTIFICATION_ID_DIVIDEND = 2
        const val NOTIFICATION_ID_COUPON = 3
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
    }
    
    /**
     * Показать уведомление о ребалансировке
     */
    fun showRebalanceNotification(ticker: String, action: String, deviation: Double) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val actionText = if (action == "BUY") "Купить" else "Продать"
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ребалансировка портфеля")
            .setContentText("$actionText $ticker (отклонение ${deviation}%)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_REBALANCE, notification)
    }
    
    /**
     * Показать уведомление о дивидендах
     */
    fun showDividendNotification(ticker: String, exDate: String, amount: Double) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Дивиденды: $ticker")
            .setContentText("Дата отсечки: $exDate, выплата: $amount ₽")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_DIVIDEND, notification)
    }
    
    /**
     * Показать уведомление о купоне
     */
    fun showCouponNotification(ticker: String, exDate: String, amount: Double) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Купон: $ticker")
            .setContentText("Дата выплаты: $exDate, сумма: $amount ₽")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_COUPON, notification)
    }
}
