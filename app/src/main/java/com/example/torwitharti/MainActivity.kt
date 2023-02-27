package com.example.torwitharti

import android.animation.AnimatorSet
import android.os.Bundle
import android.view.ViewAnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.torwitharti.databinding.ActivityMainBinding
import com.example.torwitharti.utils.center
import com.example.torwitharti.utils.createStatusBarAnimation
import com.example.torwitharti.vpn.ConnectionState
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), StatusBarProgressInterface {

    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

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

    override fun setStatus(vpnStatus: ConnectionState) {

        when (vpnStatus) {
            ConnectionState.INIT -> {
                binding.progressSlider.setBackgroundResource(android.R.color.transparent)
                binding.toolbar.setBackgroundResource(R.color.purpleDark)
                binding.toolbar.title = ""
            }
            ConnectionState.CONNECTING -> {
                binding.progressSlider.setBackgroundResource(R.drawable.gradient_progress)
                val gradientAnim = createStatusBarAnimation(
                    binding.progressSlider.background,
                    MainActivity@ this,
                    intArrayOf(R.color.connectingRainbowStart, R.color.connectingRainbowEnd)
                )

                val center = binding.progressSlider.center()
                val revealAnim = ViewAnimationUtils.createCircularReveal(
                    binding.progressSlider,
                    center.x,
                    center.y,
                    0f,
                    binding.progressSlider.width.toFloat()
                )

                AnimatorSet().apply {
                    play(revealAnim).before(gradientAnim)
                    start()
                }
            }
            ConnectionState.PAUSED -> {
                binding.progressSlider.setBackgroundResource(R.color.yellowNormal)

                val center = binding.progressSlider.center()
                val revealAnim = ViewAnimationUtils.createCircularReveal(
                    binding.progressSlider,
                    center.x,
                    center.y,
                    0f,
                    binding.progressSlider.width.toFloat()
                )
                revealAnim.interpolator = DecelerateInterpolator()
                revealAnim.duration = 1000
                revealAnim.start()
            }
            ConnectionState.CONNECTED -> {

            }
            ConnectionState.DISCONNECTED -> {}
            ConnectionState.CONNECTION_ERROR -> {}
            ConnectionState.DISCONNECTING -> {}
        }
    }
}

