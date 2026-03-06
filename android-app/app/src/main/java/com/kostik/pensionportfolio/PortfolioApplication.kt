package com.kostik.pensionportfolio

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kostik.pensionportfolio.data.PortfolioRepository

/**
 * Application class
 */
class PortfolioApplication : Application() {
    
    lateinit var repository: PortfolioRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализация репозитория
        repository = PortfolioRepository.getInstance(
            BuildConfig.API_BASE_URL
        )
        
        // Создание канала уведомлений
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = NOTIFICATION_CHANNEL_ID
            val channelName = "Пенсионный портфель"
            val channelDescription = "Уведомления о портфеле"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "pension_portfolio_channel"
    }
}
