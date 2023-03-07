package com.example.torwitharti

import android.animation.AnimatorSet
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.torwitharti.databinding.ActivityMainBinding
import com.example.torwitharti.vpn.ConnectionState
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var status: ConnectionState
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var connectingAnim: AnimatorSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_connect,
                R.id.navigation_dashboard
            )
        )
        navController.addOnDestinationChangedListener { _: NavController, destination: NavDestination, bundle: Bundle? ->
            if (appBarConfiguration.topLevelDestinations.contains(destination.id)) {
                navView.isVisible = true
                supportActionBar?.hide()
            } else {
                navView.isVisible = false
                supportActionBar?.show()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

