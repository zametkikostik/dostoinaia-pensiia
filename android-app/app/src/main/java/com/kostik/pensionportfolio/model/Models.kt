package com.kostik.pensionportfolio.model

import com.google.gson.annotations.SerializedName

/**
 * Модель котировки инструмента
 */
data class Quote(
    val ticker: String,
    val name: String = "",
    val price: Double,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val volume: Long = 0,
    val currency: String = "RUB"
)

/**
 * Модель инструмента портфеля
 */
data class PortfolioInstrument(
    val ticker: String,
    val name: String,
    val targetPercent: Int,
    val category: String = ""
)

/**
 * Модель портфеля
 */
data class Portfolio(
    val stocks: List<PortfolioInstrument>,
    val bonds: List<PortfolioInstrument>,
    val gold: List<PortfolioInstrument>
)

/**
 * Модель дивиденда
 */
data class Dividend(
    val exDate: String,
    val dividendAmount: Double,
    val currency: String,
    val period: String
)

/**
 * Модель купона
 */
data class Coupon(
    val exDate: String,
    val couponAmount: Double,
    val currency: String,
    val period: String
)

/**
 * Модель аналитики портфеля
 */
data class PortfolioAnalytics(
    val totalValue: Double,
    val totalChange: Double,
    val allocation: Allocation,
    val dividendYield: Double,
    val couponYield: Double,
    val totalYield: Double,
    val pensionForecast: PensionForecast,
    val dailyChange: Double
)

data class Allocation(
    val stocks: Int,
    val bonds: Int,
    val gold: Int
)

data class PensionForecast(
    val yearsToRetirement: Int,
    val futureValue: Long,
    val monthlyPassiveIncome: Long,
    val yearlyBreakdown: List<YearlyBreakdown>
)

data class YearlyBreakdown(
    val year: Int,
    val value: Long
)

/**
 * Модель ребалансировки
 */
data class RebalanceResult(
    val totalValue: Long,
    val currentAllocation: List<AllocationItem>,
    val recommendations: List<RebalanceRecommendation>,
    val needsRebalance: Boolean,
    val threshold: Int
)

data class AllocationItem(
    val ticker: String,
    val percent: Double,
    val targetPercent: Int
)

data class RebalanceRecommendation(
    val ticker: String,
    val name: String,
    val type: String,
    val action: String,
    val currentPercent: Double,
    val targetPercent: Int,
    val deviation: Double,
    val message: String
)

/**
 * Статус торговой сессии
 */
data class SessionStatus(
    val isOpen: Boolean,
    val currentTime: String,
    val marketOpen: String,
    val marketClose: String,
    val message: String
)

/**
 * Позиция в портфеле
 */
data class Position(
    val ticker: String,
    val name: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double,
    val totalValue: Double,
    val dailyChangePercent: Double
)

/**
 * Счёт
 */
data class Account(
    val id: String,
    val name: String,
    val type: String,
    val currency: String
)
