package com.kostik.pensionportfolio.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kostik.pensionportfolio.data.PortfolioRepository
import com.kostik.pensionportfolio.databinding.FragmentDividendsBinding
import com.kostik.pensionportfolio.model.Dividend
import com.kostik.pensionportfolio.model.PortfolioInstrument
import kotlinx.coroutines.launch

/**
 * Фрагмент дивидендов и купонов
 */
class DividendsFragment : Fragment() {
    
    private var _binding: FragmentDividendsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PortfolioRepository
    private lateinit var dividendsAdapter: DividendsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDividendsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = (requireActivity().application as com.kostik.pensionportfolio.PortfolioApplication).repository
        
        dividendsAdapter = DividendsAdapter(emptyList())
        binding.recyclerViewDividends.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDividends.adapter = dividendsAdapter
        
        loadDividendsCalendar()
    }
    
    private fun loadDividendsCalendar() {
        lifecycleScope.launch {
            val portfolioResult = repository.getPortfolio()
            val portfolio = portfolioResult.getOrNull() ?: return@launch
            
            val allDividends = mutableListOf<DividendItem>()
            
            // Загрузка дивидендов по акциям
            portfolio.stocks.forEach { stock ->
                val dividendsResult = repository.getDividends(stock.ticker)
                dividendsResult.onSuccess { dividends ->
                    dividends.forEach { div ->
                        allDividends.add(DividendItem(stock.ticker, stock.name, div.exDate, div.dividendAmount, "Дивиденд"))
                    }
                }
            }
            
            // Загрузка купонов по облигациям
            portfolio.bonds.forEach { bond ->
                val couponsResult = repository.getCoupons(bond.ticker)
                couponsResult.onSuccess { coupons ->
                    coupons.forEach { coupon ->
                        allDividends.add(DividendItem(bond.ticker, bond.name, coupon.exDate, coupon.couponAmount, "Купон"))
                    }
                }
            }
            
            // Сортировка по дате
            val sortedDividends = allDividends.sortedBy { it.exDate }
            dividendsAdapter = DividendsAdapter(sortedDividends)
            binding.recyclerViewDividends.adapter = dividendsAdapter
            
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Модель элемента дивидендов/купонов
 */
data class DividendItem(
    val ticker: String,
    val name: String,
    val exDate: String,
    val amount: Double,
    val type: String
)
