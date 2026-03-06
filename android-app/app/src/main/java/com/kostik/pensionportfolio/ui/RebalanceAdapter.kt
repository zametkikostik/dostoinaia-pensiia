package com.kostik.pensionportfolio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.databinding.ItemRebalanceRecommendationBinding
import com.kostik.pensionportfolio.model.RebalanceRecommendation

/**
 * Адаптер для рекомендаций по ребалансировке
 */
class RebalanceAdapter(
    private val recommendations: List<RebalanceRecommendation>
) : RecyclerView.Adapter<RebalanceAdapter.ViewHolder>() {
    
    class ViewHolder(private val binding: ItemRebalanceRecommendationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recommendation: RebalanceRecommendation) {
            binding.tvTicker.text = recommendation.ticker
            binding.tvName.text = recommendation.name
            
            // Действие
            val actionText = if (recommendation.action == "BUY") "КУПИТЬ" else "ПРОДАТЬ"
            binding.tvActionBadge.text = actionText
            
            // Цвет бейджа и индикатора
            val (badgeColor, indicatorColor) = if (recommendation.action == "BUY") {
                Pair(R.color.green, R.color.green)
            } else {
                Pair(R.color.red, R.color.red)
            }
            
            binding.tvActionBadge.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, badgeColor)
            )
            binding.viewActionIndicator.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, indicatorColor)
            )
            
            // Проценты
            binding.tvCurrentPercent.text = "${recommendation.currentPercent}%"
            binding.tvTargetPercent.text = "${recommendation.targetPercent}%"
            
            // Отклонение
            val deviationText = if (recommendation.deviation > 0) {
                "+${recommendation.deviation}%"
            } else {
                "${recommendation.deviation}%"
            }
            binding.tvDeviation.text = "Отклонение: $deviationText"
            
            // Прогресс бар (масштабируем до 30% макс)
            val deviationPercent = Math.abs(recommendation.deviation).toInt().coerceAtMost(30)
            binding.progressDeviation.progress = deviationPercent * 3
            
            // Сообщение
            binding.tvMessage.text = recommendation.message
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRebalanceRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }
    
    override fun getItemCount() = recommendations.size
}
