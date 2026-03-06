package com.kostik.pensionportfolio.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.kostik.pensionportfolio.R
import com.kostik.pensionportfolio.databinding.ActivityMainBinding

/**
 * Главная активность
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Настройка навигации
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_portfolio, R.id.nav_calendar, R.id.nav_rebalance, R.id.nav_analytics)
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        
        // Настройка SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener {
            navController.currentDestination?.id?.let { id ->
                when (id) {
                    R.id.nav_portfolio -> refreshPortfolio(navController)
                    R.id.nav_calendar -> refreshCalendar(navController)
                    R.id.nav_rebalance -> refreshRebalance(navController)
                    R.id.nav_analytics -> refreshAnalytics(navController)
                }
            }
        }
    }
    
    private fun refreshPortfolio(navController: androidx.navigation.NavController) {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        (fragment as? PortfolioFragment)?.loadPortfolio()
        binding.swipeRefresh.isRefreshing = false
    }
    
    private fun refreshCalendar(navController: androidx.navigation.NavController) {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        (fragment as? CalendarFragment)?.loadCalendarEvents()
        binding.swipeRefresh.isRefreshing = false
    }
    
    private fun refreshRebalance(navController: androidx.navigation.NavController) {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        (fragment as? RebalanceFragment)?.calculateRebalance()
        binding.swipeRefresh.isRefreshing = false
    }
    
    private fun refreshAnalytics(navController: androidx.navigation.NavController) {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        (fragment as? AnalyticsFragment)?.loadAnalytics()
        binding.swipeRefresh.isRefreshing = false
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(android.content.Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
