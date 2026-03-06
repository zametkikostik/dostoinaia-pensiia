package com.kostik.pensionportfolio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.databinding.ItemCalendarEventBinding
import com.kostik.pensionportfolio.model.CalendarEvent
import com.kostik.pensionportfolio.model.EventType
import com.kostik.pensionportfolio.model.Priority

/**
 * Адаптер для календаря событий
 */
class CalendarAdapter(
    private val events: List<CalendarEvent>
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
    
    class ViewHolder(private val binding: ItemCalendarEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: CalendarEvent) {
            binding.tvEventTitle.text = event.title
            binding.tvEventDescription.text = event.description
            binding.tvEventDate.text = event.date
            
            // Цвет иконки в зависимости от типа
            val (iconRes, iconColor) = when (event.type) {
                EventType.REBALANCE -> Pair(R.drawable.ic_rebalance, R.color.orange)
                EventType.DIVIDEND -> Pair(R.drawable.ic_dividends, R.color.green)
                EventType.COUPON -> Pair(R.drawable.ic_coupon, R.color.blue)
                EventType.REPORT -> Pair(R.drawable.ic_report, R.color.gray)
            }
            
            binding.ivEventIcon.setImageResource(iconRes)
            binding.ivEventIcon.setColorFilter(ContextCompat.getColor(binding.root.context, iconColor))
            
            // Приоритет
            val priorityColor = when (event.priority) {
                Priority.HIGH -> ContextCompat.getColor(binding.root.context, R.color.red)
                Priority.MEDIUM -> ContextCompat.getColor(binding.root.context, R.color.orange)
                Priority.LOW -> ContextCompat.getColor(binding.root.context, R.color.gray)
            }
            
            binding.viewPriorityIndicator.setBackgroundColor(priorityColor)
            
            // Статус выполнения
            if (event.isCompleted) {
                binding.tvEventTitle.alpha = 0.5f
                binding.tvEventDescription.alpha = 0.5f
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }
    
    override fun getItemCount() = events.size
}
