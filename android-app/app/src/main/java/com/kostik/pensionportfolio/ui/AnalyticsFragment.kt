package com.kostik.pensionportfolio.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.data.PortfolioRepository
import com.kostik.pensionportfolio.databinding.FragmentAnalyticsBinding
import kotlinx.coroutines.launch

/**
 * Фрагмент аналитики
 */
class AnalyticsFragment : Fragment() {
    
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PortfolioRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = (requireActivity().application as com.kostik.pensionportfolio.PortfolioApplication).repository
        
        loadAnalytics()
    }
    
    fun loadAnalytics() {
        lifecycleScope.launch {
            val result = repository.getAnalytics()
            result.fold(
                onSuccess = { analytics ->
                    displayAnalytics(analytics)
                },
                onFailure = { error ->
                    // Показать ошибку
                }
            )
        }
    }
    
    private fun displayAnalytics(analytics: com.kostik.pensionportfolio.model.PortfolioAnalytics) {
        // Общая стоимость
        binding.tvTotalValue.text = formatMoney(analytics.totalValue)
        
        // Изменение за день
        val changeText = if (analytics.dailyChange >= 0) {
            "+${analytics.dailyChange}%"
        } else {
            "${analytics.dailyChange}%"
        }
        binding.tvDailyChange.text = changeText
        binding.tvDailyChange.setTextColor(
            if (analytics.dailyChange >= 0) Color.GREEN else Color.RED
        )
        
        // Аллокация
        binding.progressStocks.progress = analytics.allocation.stocks
        binding.progressBonds.progress = analytics.allocation.bonds
        binding.progressGold.progress = analytics.allocation.gold
        
        binding.tvStocksValue.text = "${analytics.allocation.stocks}%"
        binding.tvBondsValue.text = "${analytics.allocation.bonds}%"
        binding.tvGoldValue.text = "${analytics.allocation.gold}%"
        
        // Доходность
        binding.tvDividendYieldValue.text = "${analytics.dividendYield}%"
        binding.tvCouponYieldValue.text = "${analytics.couponYield}%"
        
        // Прогноз пенсии
        binding.tvPensionFutureValue.text = formatMoney(analytics.pensionForecast.futureValue.toDouble())
        binding.tvMonthlyIncome.text = formatMoney(analytics.pensionForecast.monthlyPassiveIncome.toDouble())
        
        // График роста
        setupGrowthChart(analytics)
    }
    
    private fun setupGrowthChart(analytics: com.kostik.pensionportfolio.model.PortfolioAnalytics) {
        val entries = analytics.pensionForecast.yearlyBreakdown.map {
            Entry(it.year.toFloat(), it.value.toFloat())
        }
        
        val dataSet = LineDataSet(entries, "Рост портфеля").apply {
            color = requireContext().getColor(R.color.blue)
            valueTextColor = Color.GRAY
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = requireContext().getColor(R.color.blue_light)
        }
        
        binding.chartGrowth.data = LineData(dataSet)
        binding.chartGrowth.invalidate()
    }
    
    private fun formatMoney(value: Double): String {
        return String.format("%,d ₽", value.toLong())
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
