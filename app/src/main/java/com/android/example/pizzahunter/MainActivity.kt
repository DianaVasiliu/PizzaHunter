package com.android.example.pizzahunter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.example.pizzahunter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val menuFragment = MenuFragment()
    private val infoFragment = InfoFragment()
    private val profileLoggedInFragment = ProfileLoggedInFragment()
    private val profileLoggedOutFragment = ProfileLoggedOutFragment()
    private lateinit var profileFragment: Fragment
    private var currentFragmentIndex: Int = 0
    private var currentFragment: Fragment = homeFragment

    companion object {
        private const val CURRENT_FRAGMENT_INDEX = "CURRENT_FRAGMENT_INDEX"
        private const val CURRENT_PROFILE_FRAGMENT_INDEX = "CURRENT_PROFILE_FRAGMENT_INDEX"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt(CURRENT_FRAGMENT_INDEX)
            profileFragment = when(savedInstanceState.getInt(CURRENT_PROFILE_FRAGMENT_INDEX)) {
                1 -> profileLoggedOutFragment
                else -> profileLoggedInFragment
            }
        }

        currentFragment = indexToFragment(currentFragmentIndex)
        replaceFragment(currentFragment)

        setOnAuthStateChangeListener()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home_nav_button -> replaceFragment(homeFragment)
                R.id.menu_nav_button -> replaceFragment(menuFragment)
                R.id.info_nav_button -> replaceFragment(infoFragment)
                R.id.profile_nav_button -> replaceFragment(profileFragment)
            }
            true
        }
    }

    public override fun onStart() {
        super.onStart()
        Database.checkUserLoggedIn()

        profileFragment = if (Database.isUserLoggedIn()) {
            profileLoggedInFragment
        } else {
            profileLoggedOutFragment
        }

        if (currentFragmentIndex == 3) {
            replaceFragment(profileFragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val profileIndex = when(profileFragment) {
            profileLoggedOutFragment -> 1
            else -> 2
        }

        outState.putInt(CURRENT_FRAGMENT_INDEX, currentFragmentIndex)
        outState.putInt(CURRENT_PROFILE_FRAGMENT_INDEX, profileIndex)
    }

    private fun setOnAuthStateChangeListener() {
        Database.onAuthStateChange {
            if (Database.isUserLoggedIn()) {
                profileFragment = profileLoggedInFragment
            } else {
                profileFragment = profileLoggedOutFragment
                if (currentFragmentIndex == 3) {
                    replaceFragment(profileFragment)
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        if (!isDestroyed) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, fragment)
                commit()
            }
            currentFragmentIndex = fragmentToIndex(fragment)
        }
    }

    private fun fragmentToIndex(fragment: Fragment) : Int {
        return when(fragment) {
            homeFragment -> 0
            menuFragment -> 1
            infoFragment -> 2
            profileFragment -> 3
            else -> 0
        }
    }

    private fun indexToFragment(idx: Int) : Fragment {
        return when(idx) {
            0 -> homeFragment
            1 -> menuFragment
            2 -> infoFragment
            3 -> profileFragment
            else -> homeFragment
        }
    }

    fun showLoadingScreen(value: Boolean) {
        if (value) {
            binding.loadingScreen.visibility = View.VISIBLE
        }
        else {
            binding.loadingScreen.visibility = View.GONE
        }
    }
}