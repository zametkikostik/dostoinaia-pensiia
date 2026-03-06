package com.kostik.pensionportfolio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kostik.pensionportfolio.databinding.ItemDividendBinding
import com.kostik.pensionportfolio.ui.DividendItem

/**
 * Адаптер для списка дивидендов/купонов
 */
class DividendsAdapter(
    private val items: List<DividendItem>
) : RecyclerView.Adapter<DividendsAdapter.ViewHolder>() {
    
    class ViewHolder(private val binding: ItemDividendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DividendItem) {
            binding.tvTicker.text = item.ticker
            binding.tvName.text = item.name
            binding.tvDate.text = item.exDate
            binding.tvAmount.text = "${item.amount} ₽"
            binding.tvType.text = item.type
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDividendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
}
