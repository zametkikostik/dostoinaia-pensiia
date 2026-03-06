package com.kostik.pensionportfolio.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kostik.pensionportfolio.data.PortfolioRepository
import com.kostik.pensionportfolio.databinding.FragmentCalendarBinding
import com.kostik.pensionportfolio.model.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Фрагмент календаря событий
 */
class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PortfolioRepository
    private lateinit var adapter: CalendarAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = (requireActivity().application as com.kostik.pensionportfolio.PortfolioApplication).repository
        
        adapter = CalendarAdapter(emptyList())
        binding.recyclerViewCalendar.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCalendar.adapter = adapter
        
        loadCalendarEvents()
    }
    
    fun loadCalendarEvents() {
        lifecycleScope.launch {
            val events = mutableListOf<CalendarEvent>()
            
            // 1. Загрузить портфель
            val portfolioResult = repository.getPortfolio()
            val portfolio = portfolioResult.getOrNull() ?: return@launch
            
            // 2. Рассчитать ребалансировку
            val rebalanceResult = repository.calculateRebalance(portfolio)
            val rebalance = rebalanceResult.getOrNull()
            
            // 3. Добавить события ребалансировки
            if (rebalance != null && rebalance.needsRebalance) {
                rebalance.recommendations.forEach { rec ->
                    val action = if (rec.action == "BUY") RebalanceAction.BUY else RebalanceAction.SELL
                    val priority = when {
                        Math.abs(rec.deviation) > 15 -> Priority.HIGH
                        Math.abs(rec.deviation) > 10 -> Priority.MEDIUM
                        else -> Priority.LOW
                    }
                    
                    events.add(CalendarEvent(
                        id = "rebalance_${rec.ticker}",
                        title = if (action == RebalanceAction.BUY) "КУПИТЬ ${rec.ticker}" else "ПРОДАТЬ ${rec.ticker}",
                        description = rec.message,
                        date = getCurrentDate(),
                        type = EventType.REBALANCE,
                        priority = priority,
                        relatedTicker = rec.ticker
                    ))
                }
            }
            
            // 4. Добавить события дивидендов
            portfolio.stocks.forEach { stock ->
                val dividendsResult = repository.getDividends(stock.ticker)
                dividendsResult.onSuccess { dividends ->
                    dividends.forEach { div ->
                        events.add(CalendarEvent(
                            id = "div_${stock.ticker}_${div.exDate}",
                            title = "Дивиденды: ${stock.name}",
                            description = "Дата отсечки: ${div.exDate}, выплата: ${div.dividendAmount} ₽",
                            date = div.exDate,
                            type = EventType.DIVIDEND,
                            priority = Priority.HIGH,
                            relatedTicker = stock.ticker
                        ))
                    }
                }
            }
            
            // 5. Добавить события купонов
            portfolio.bonds.forEach { bond ->
                val couponsResult = repository.getCoupons(bond.ticker)
                couponsResult.onSuccess { coupons ->
                    coupons.forEach { coupon ->
                        events.add(CalendarEvent(
                            id = "coup_${bond.ticker}_${coupon.exDate}",
                            title = "Купон: ${bond.name}",
                            description = "Дата выплаты: ${coupon.exDate}, сумма: ${coupon.couponAmount} ₽",
                            date = coupon.exDate,
                            type = EventType.COUPON,
                            priority = Priority.MEDIUM,
                            relatedTicker = bond.ticker
                        ))
                    }
                }
            }
            
            // 6. Сортировка по дате и приоритету
            val sortedEvents = events.sortedWith(compareBy({ it.date }, { 
                when (it.priority) {
                    Priority.HIGH -> 0
                    Priority.MEDIUM -> 1
                    Priority.LOW -> 2
                }
            }))
            
            adapter = CalendarAdapter(sortedEvents)
            binding.recyclerViewCalendar.adapter = adapter
            
            // Показать статистику
            val rebalanceCount = events.count { it.type == EventType.REBALANCE }
            val dividendCount = events.count { it.type == EventType.DIVIDEND }
            val couponCount = events.count { it.type == EventType.COUPON }
            
            binding.tvRebalanceCount.text = rebalanceCount.toString()
            binding.tvDividendCount.text = dividendCount.toString()
            binding.tvCouponCount.text = couponCount.toString()
            
            // Показать сообщение если нет событий
            binding.layoutEmpty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewCalendar.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
