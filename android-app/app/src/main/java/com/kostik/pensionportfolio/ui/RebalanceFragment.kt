package com.kostik.pensionportfolio.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.data.PortfolioRepository
import com.kostik.pensionportfolio.databinding.FragmentRebalanceBinding
import kotlinx.coroutines.launch

/**
 * Фрагмент ребалансировки
 */
class RebalanceFragment : Fragment() {
    
    private var _binding: FragmentRebalanceBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PortfolioRepository
    private lateinit var adapter: RebalanceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRebalanceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = (requireActivity().application as com.kostik.pensionportfolio.PortfolioApplication).repository
        
        adapter = RebalanceAdapter(emptyList())
        binding.recyclerViewRecommendations.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecommendations.adapter = adapter
        
        binding.buttonCalculate.setOnClickListener {
            calculateRebalance()
        }
        
        // Автоматический расчёт при загрузке
        calculateRebalance()
    }
    
    fun calculateRebalance() {
        lifecycleScope.launch {
            val portfolioResult = repository.getPortfolio()
            val portfolio = portfolioResult.getOrNull() ?: return@launch
            
            val rebalanceResult = repository.calculateRebalance(portfolio)
            
            rebalanceResult.fold(
                onSuccess = { rebalance ->
                    displayRebalance(rebalance)
                },
                onFailure = { error ->
                    // Показать ошибку
                }
            )
        }
    }
    
    private fun displayRebalance(rebalance: com.kostik.pensionportfolio.model.RebalanceResult) {
        binding.tvTotalValue.text = formatMoney(rebalance.totalValue.toDouble())
        binding.tvNeedsRebalance.text = if (rebalance.needsRebalance) "Требуется" else "Не требуется"
        binding.tvNeedsRebalance.setTextColor(
            if (rebalance.needsRebalance) Color.RED else Color.GREEN
        )
        
        // Показываем рекомендации
        adapter = RebalanceAdapter(rebalance.recommendations)
        binding.recyclerViewRecommendations.adapter = adapter
        
        // Показываем блок с рекомендациями только если они есть
        binding.layoutRecommendations.visibility = 
            if (rebalance.recommendations.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun formatMoney(value: Double): String {
        return String.format("%,d ₽", value.toLong())
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
