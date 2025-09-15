package com.example.kalpeshdemo.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.kalpeshdemo.R
import com.example.kalpeshdemo.databinding.ActivityMainBinding
import com.example.kalpeshdemo.ui.empty.EmptyFragment
import com.example.kalpeshdemo.ui.portfolio.PortfolioFragment

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val fragments = mutableMapOf<Int, Pair<Fragment, Int>>()

    private val fragmentFactory = mapOf(
        R.id.nav_watchlist to { EmptyFragment() },
        R.id.nav_orders to { EmptyFragment() },
        R.id.nav_portfolio to { PortfolioFragment() },
        R.id.nav_funds to { EmptyFragment() },
        R.id.nav_invest to { EmptyFragment() }
    )

    private val titleMap = mapOf(
        R.id.nav_watchlist to R.string.string_watchlist,
        R.id.nav_orders to R.string.string_orders,
        R.id.nav_portfolio to R.string.string_portfolio,
        R.id.nav_funds to R.string.string_funds,
        R.id.nav_invest to R.string.string_invest
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_portfolio
    }

    private fun handleNavigation(itemId: Int) {
        val titleRes = titleMap[itemId] ?: R.string.app_name
        val title = getString(titleRes)

        // Get or create the fragment
        val fragment = fragments[itemId]?.first ?: run {
            val newFragment = fragmentFactory[itemId]?.invoke() ?: return@handleNavigation
            fragments[itemId] = newFragment to titleRes
            newFragment
        }

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment?.javaClass != fragment.javaClass) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, itemId.toString())
                .addToBackStack(null)
                .commit()
        }

        binding.lblTitle.text = title
    }
}