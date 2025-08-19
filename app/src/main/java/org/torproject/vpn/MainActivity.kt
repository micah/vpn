package org.torproject.vpn

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.torproject.vpn.databinding.ActivityMainBinding
import org.torproject.vpn.utils.applyInsetsToViewPadding
import org.torproject.vpn.utils.navigateSafe

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainActivityViewModel
    lateinit var navController: NavController
    companion object {
        const val KEY_ACTION = "ACTION"
        val ACTION_REQUEST_VPN_PERMISSON = MainActivity::class.java.simpleName + ".ACTION_REQUEST_VPN_PERMISSON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        applyInsetsToViewPadding(navView, true, false, true, true)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_connect,
                R.id.navigation_help,
                R.id.configure_fragment
            )
        )
        navController.addOnDestinationChangedListener { _: NavController, destination: NavDestination, _: Bundle? ->
            if (appBarConfiguration.topLevelDestinations.contains(destination.id)) {
                showBottomNavigationView(navView)
            } else {
                hideBottomNavigationView(navView)
            }
        }

        manuallyAdjustBottomNavItemIconsSize()

        handleIntentAction()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.guideScreenVisibility.collect { visible ->

                        navView.post {
                            if (visible && appBarConfiguration.topLevelDestinations.contains(navController.currentDestination?.id)) {
                                showBottomNavigationView(navView)
                            } else {
                                hideBottomNavigationView(navView)
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.navView.post {
            viewModel.setHeight(binding.navView.height)
        }
    }

    private fun handleIntentAction() {
        intent?.let { intent ->
            when (intent.action) {
                ACTION_REQUEST_VPN_PERMISSON -> {
                    val bundle = Bundle()
                    bundle.putString(KEY_ACTION, ACTION_REQUEST_VPN_PERMISSON)
                    navController.navigateSafe(R.id.navigation_connect, bundle)
                }

                else -> {}
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentAction()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun manuallyAdjustBottomNavItemIconsSize() {
        //There's default primary color tint that needs to be removed
        binding.navView.itemIconTintList = null;

        //loop over each menuItem to change it's icons' width and height
        val menuView = binding.navView.getChildAt(0) as ViewGroup
        for (i in 0 until menuView.childCount) {

            val iconView =
                menuView.getChildAt(i)
                    .findViewById<View>(com.google.android.material.R.id.navigation_bar_item_icon_view)
            val layoutParams = iconView.layoutParams
            val displayMetrics = resources.displayMetrics
            layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 28f,
                displayMetrics
            ).toInt()
            layoutParams.width = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 60f,
                displayMetrics
            ).toInt()
            iconView.layoutParams = layoutParams
        }
    }

    private fun hideBottomNavigationView(view: BottomNavigationView) {
        view.clearAnimation()
        view.animate().translationY(view.height.toFloat()).duration = 300
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = true
        }
    }

    private fun showBottomNavigationView(view: BottomNavigationView) {
        view.clearAnimation()
        view.animate().translationY(0f).duration = 300
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

