package com.example.zarur

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.zarur.app.LocaleManager
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.repository.LanguageRepository
import com.example.zarur.presentation.explore.ExploreFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var languageRepository: LanguageRepository

    @Inject
    lateinit var appPreferences: AppPreferences

    private var destinationListener: NavController.OnDestinationChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val language = languageRepository.getLanguage().first()
            LocaleManager.applyLocale(this@MainActivity, language)
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Dynamically set start destination without Splash screen
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val startDest = R.id.exploreFragment
        navGraph.setStartDestination(startDest)
        navController.graph = navGraph

        // Automatically add status bar padding to fragments EXCEPT full-screen views
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                if ((f !is NavHostFragment) && 
                    (f !is ExploreFragment) &&
                    (f.javaClass.simpleName != "ProductDetailFragment")) {
                    val initialPaddingTop = v.paddingTop
                    ViewCompat.setOnApplyWindowInsetsListener(v) { view, insets ->
                        val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                        view.updatePadding(top = initialPaddingTop + statusBars.top)
                        insets
                    }
                }
            }
        }, true)
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            destinationListener?.let { navController.removeOnDestinationChangedListener(it) }
            destinationListener = NavController.OnDestinationChangedListener { _, destination, _ ->
                // Bottom Nav Visibility
                when (destination.id) {
                    R.id.exploreFragment,
                    R.id.favoritesFragment,
                    R.id.messageFragment,
                    R.id.profileFragment -> bottomNav.visibility = View.VISIBLE
                    else -> bottomNav.visibility = View.GONE
                }
            }
            navController.addOnDestinationChangedListener(destinationListener!!)

            bottomNav.updatePadding(bottom = navBars.bottom)
            insets
        }

        bottomNav.setupWithNavController(navController)
    }
}
