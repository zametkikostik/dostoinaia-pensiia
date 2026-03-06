package com.kostik.pensionportfolio.data

import com.kostik.pensionportfolio.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API интерфейс для подключения к Cloudflare Worker
 */
interface PensionApi {
    
    /**
     * Получить котировки инструментов
     */
    @GET("api/quotes")
    suspend fun getQuotes(@Query("tickers") tickers: String): Response<QuotesResponse>
    
    /**
     * Получить портфель
     */
    @GET("api/portfolio")
    suspend fun getPortfolio(): Response<PortfolioResponse>
    
    /**
     * Сохранить портфель
     */
    @POST("api/portfolio")
    suspend fun savePortfolio(@Body portfolio: PortfolioRequest): Response<SaveResponse>
    
    /**
     * Получить аналитику портфеля
     */
    @GET("api/analytics")
    suspend fun getAnalytics(): Response<AnalyticsResponse>
    
    /**
     * Рассчитать ребалансировку
     */
    @POST("api/rebalance")
    suspend fun calculateRebalance(@Body rebalanceRequest: RebalanceRequest): Response<RebalanceResponse>
    
    /**
     * Получить дивиденды по инструменту
     */
    @GET("api/dividends")
    suspend fun getDividends(@Query("ticker") ticker: String): Response<DividendsResponse>
    
    /**
     * Получить купоны по облигации
     */
    @GET("api/coupons")
    suspend fun getCoupons(@Query("ticker") ticker: String): Response<CouponsResponse>
    
    /**
     * Получить статус торговой сессии
     */
    @GET("api/session")
    suspend fun getSessionStatus(): Response<SessionResponse>
    
    /**
     * Прокси к Tinkoff API - получить счета
     */
    @POST("tinkoff/tinvest/api/users/accounts")
    suspend fun getTinkoffAccounts(@Body request: Map<String, Any>): Response<TinkoffResponse>
    
    /**
     * Прокси к Tinkoff API - получить портфель
     */
    @POST("tinkoff/tinvest/api/operations/get-portfolio")
    suspend fun getTinkoffPortfolio(@Body request: PortfolioRequest): Response<TinkoffResponse>
    
    /**
     * Прокси к Tinkoff API - последние цены
     */
    @POST("tinkoff/tinvest/api/market/last-prices")
    suspend fun getTinkoffLastPrices(@Body request: LastPricesRequest): Response<TinkoffResponse>
}

// Response модели

data class QuotesResponse(
    val quotes: List<Quote>
)

data class PortfolioResponse(
    val portfolio: Portfolio,
    val isDefault: Boolean
)

data class PortfolioRequest(
    val portfolio: Portfolio
)

data class SaveResponse(
    val success: Boolean,
    val message: String
)

data class AnalyticsResponse(
    val analytics: PortfolioAnalytics
)

data class RebalanceRequest(
    val portfolio: Portfolio,
    val holdings: Map<String, Int> = emptyMap()
)

data class RebalanceResponse(
    val rebalance: RebalanceResult
)

data class DividendsResponse(
    val ticker: String,
    val dividends: List<Dividend>
)

data class CouponsResponse(
    val ticker: String,
    val coupons: List<Coupon>
)

data class SessionResponse(
    val session: SessionStatus
)

data class TinkoffResponse(
    val payload: Any?
)

data class LastPricesRequest(
    val instruments: List<InstrumentRequest>
)

data class InstrumentRequest(
    val instrumentType: String,
    val figi: String
)
