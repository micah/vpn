package org.torproject.vpn

import android.animation.AnimatorSet
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import org.torproject.vpn.R
import org.torproject.vpn.databinding.ActivityMainBinding
import org.torproject.vpn.vpn.ConnectionState
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var status: ConnectionState
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var connectingAnim: AnimatorSet

    companion object {
        val KEY_ACTION = "ACTION"
        val ACTION_REQUEST_VPN_PERMISSON = MainActivity::class.java.simpleName + ".ACTION_REQUEST_VPN_PERMISSON"
    }

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

        intent?.let { intent ->
            when (intent.action) {
                ACTION_REQUEST_VPN_PERMISSON -> {
                    val bundle =  Bundle()
                    bundle.putString(KEY_ACTION, ACTION_REQUEST_VPN_PERMISSON)
                    navController.navigate(R.id.navigation_connect, bundle)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun manuallyAdjustBottomNavItemIconsSize() {
        //There's default primary color tint that needs to be removed
        binding.navView.itemIconTintList = null;

        //loop over each menuItem to change it's icons' width and height
        val menuView = binding.navView.getChildAt(0) as BottomNavigationMenuView
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
    }

    private fun showBottomNavigationView(view: BottomNavigationView) {
        view.clearAnimation()
        view.animate().translationY(0f).duration = 300
    }
}

