package com.kostik.pensionportfolio.data

import com.kostik.pensionportfolio.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Репозиторий для работы с данными портфеля
 */
class PortfolioRepository(private val api: PensionApi) {
    
    /**
     * Получить котировки для списка тикеров
     */
    suspend fun getQuotes(tickers: List<String>): Result<List<Quote>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getQuotes(tickers.joinToString(","))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.quotes)
            } else {
                Result.failure(Exception("Ошибка API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить текущий портфель
     */
    suspend fun getPortfolio(): Result<Portfolio> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPortfolio()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.portfolio)
            } else {
                Result.failure(Exception("Ошибка API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранить портфель
     */
    suspend fun savePortfolio(portfolio: Portfolio): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.savePortfolio(PortfolioRequest(portfolio))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка сохранения: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить аналитику портфеля
     */
    suspend fun getAnalytics(): Result<PortfolioAnalytics> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAnalytics()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.analytics)
            } else {
                Result.failure(Exception("Ошибка API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Рассчитать ребалансировку
     */
    suspend fun calculateRebalance(
        portfolio: Portfolio,
        holdings: Map<String, Int> = emptyMap()
    ): Result<RebalanceResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.calculateRebalance(RebalanceRequest(portfolio, holdings))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.rebalance)
            } else {
                Result.failure(Exception("Ошибка API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить дивиденды по инструменту
     */
    suspend fun getDividends(ticker: String): Result<List<Dividend>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDividends(ticker)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.dividends)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить купоны по облигации
     */
    suspend fun getCoupons(ticker: String): Result<List<Coupon>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCoupons(ticker)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.coupons)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить статус торговой сессии
     */
    suspend fun getSessionStatus(): Result<SessionStatus> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSessionStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.session)
            } else {
                Result.failure(Exception("Ошибка API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: PortfolioRepository? = null
        
        fun getInstance(apiUrl: String): PortfolioRepository {
            return INSTANCE ?: synchronized(this) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                
                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                val retrofit = Retrofit.Builder()
                    .baseUrl(apiUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                val api = retrofit.create(PensionApi::class.java)
                val repository = PortfolioRepository(api)
                INSTANCE = repository
                repository
            }
        }
    }
}
