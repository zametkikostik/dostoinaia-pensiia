package com.kostik.pensionportfolio.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.data.PortfolioRepository
import com.kostik.pensionportfolio.databinding.FragmentPortfolioBinding
import com.kostik.pensionportfolio.model.Portfolio
import com.kostik.pensionportfolio.model.PortfolioInstrument
import kotlinx.coroutines.launch

/**
 * Фрагмент портфеля - главная страница
 */
class PortfolioFragment : Fragment() {
    
    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PortfolioRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = (requireActivity().application as com.kostik.pensionportfolio.PortfolioApplication).repository
        
        loadPortfolio()
    }
    
    fun loadPortfolio() {
        lifecycleScope.launch {
            val result = repository.getPortfolio()
            result.fold(
                onSuccess = { portfolio ->
                    displayPortfolio(portfolio)
                },
                onFailure = { error ->
                    // Показать ошибку
                }
            )
        }
    }
    
    private fun displayPortfolio(portfolio: Portfolio) {
        // Формирование списка инструментов
        val allInstruments = mutableListOf<PortfolioInstrument>()
        allInstruments.addAll(portfolio.stocks.map { it.copy(category = "Акции") })
        allInstruments.addAll(portfolio.bonds.map { it.copy(category = "Облигации") })
        allInstruments.addAll(portfolio.gold.map { it.copy(category = "Золото") })
        
        // Обновление RecyclerView
        val adapter = InstrumentsAdapter(allInstruments)
        binding.recyclerViewInstruments.adapter = adapter
        
        // Обновление диаграммы
        setupPieChart(portfolio)
    }
    
    private fun setupPieChart(portfolio: Portfolio) {
        val entries = listOf(
            PieEntry(portfolio.stocks.sumOf { it.targetPercent }.toFloat(), "Акции"),
            PieEntry(portfolio.bonds.sumOf { it.targetPercent }.toFloat(), "Облигации"),
            PieEntry(portfolio.gold.sumOf { it.targetPercent }.toFloat(), "Золото")
        )
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                requireContext().getColor(R.color.blue),
                requireContext().getColor(R.color.green),
                requireContext().getColor(R.color.gold)
            )
            valueTextSize = 14f
            valueFormatter = PercentFormatter(binding.chartAllocation)
        }
        
        val data = PieData(dataSet)
        binding.chartAllocation.data = data
        binding.chartAllocation.invalidate()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
