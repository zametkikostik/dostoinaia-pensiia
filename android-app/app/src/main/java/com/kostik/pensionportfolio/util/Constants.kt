package com.kostik.pensionportfolio.util

/**
 * Константы приложения
 */
object Constants {
    
    // API
    const val API_TIMEOUT = 30000L // 30 секунд
    
    // Ребалансировка
    const val REBALANCE_THRESHOLD = 5 // 5% порог отклонения
    
    // Кэширование
    const val CACHE_EXPIRY_QUOTES = 60000L // 1 минута
    const val CACHE_EXPIRY_ANALYTICS = 300000L // 5 минут
    
    // Уведомления
    const val NOTIFICATION_CHANNEL_ID = "pension_portfolio_channel"
    const val NOTIFICATION_ID_REBALANCE = 1
    const val NOTIFICATION_ID_DIVIDEND = 2
    const val NOTIFICATION_ID_COUPON = 3
    
    // Preferences
    const val PREF_NAME = "pension_portfolio_prefs"
    const val PREF_TINKOFF_TOKEN = "tinkoff_token"
    const val PREF_PORTFOLIO_ID = "portfolio_id"
    
    // Форматы
    const val DATE_FORMAT = "dd.MM.yyyy"
    const val MONEY_FORMAT = "%,d ₽"
    const val PERCENT_FORMAT = "%.2f%%"
}
