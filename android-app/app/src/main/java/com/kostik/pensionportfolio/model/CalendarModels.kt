package com.kostik.pensionportfolio.model

/**
 * Событие в календаре
 */
data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val type: EventType,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val relatedTicker: String? = null
)

/**
 * Тип события
 */
enum class EventType {
    REBALANCE,      // Ребалансировка
    DIVIDEND,       // Дивиденды
    COUPON,         // Купон
    REPORT          // Отчётность
}

/**
 * Приоритет события
 */
enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Рекомендация по ребалансировке для календаря
 */
data class RebalanceCalendarItem(
    val ticker: String,
    val name: String,
    val action: RebalanceAction,
    val currentPercent: Double,
    val targetPercent: Int,
    val deviation: Double,
    val recommendedDate: String,
    val priority: Priority
)

/**
 * Действие ребалансировки
 */
enum class RebalanceAction {
    BUY,    // Купить
    SELL    // Продать
}
