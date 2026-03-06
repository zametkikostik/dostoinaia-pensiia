package com.kostik.pensionportfolio.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kostik.pensionportfolio.databinding.ActivitySettingsBinding

/**
 * Активность настроек
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
