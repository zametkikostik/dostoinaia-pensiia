package com.kostik.pensionportfolio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kostik.pensionportfolio.databinding.ItemInstrumentBinding
import com.kostik.pensionportfolio.model.PortfolioInstrument

/**
 * Адаптер для списка инструментов
 */
class InstrumentsAdapter(
    private val instruments: List<PortfolioInstrument>
) : RecyclerView.Adapter<InstrumentsAdapter.ViewHolder>() {
    
    class ViewHolder(private val binding: ItemInstrumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(instrument: PortfolioInstrument) {
            binding.tvTicker.text = instrument.ticker
            binding.tvName.text = instrument.name
            binding.tvPercent.text = "${instrument.targetPercent}%"
            binding.tvCategory.text = instrument.category
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstrumentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(instruments[position])
    }
    
    override fun getItemCount() = instruments.size
}
