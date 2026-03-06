package com.kostik.pensionportfolio.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kostik.pensionportfolio.databinding.ActivityInstrumentDetailBinding

/**
 * Активность деталей инструмента
 */
class InstrumentDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityInstrumentDetailBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityInstrumentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Получение данных из intent
        val ticker = intent.getStringExtra("ticker") ?: return
        loadInstrumentDetails(ticker)
    }
    
    private fun loadInstrumentDetails(ticker: String) {
        // Загрузка деталей инструмента
        supportActionBar?.title = ticker
    }
}
