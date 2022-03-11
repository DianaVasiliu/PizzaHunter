package com.android.example.pizzahunter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.example.pizzahunter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val menuFragment = MenuFragment()
    private val infoFragment = InfoFragment()
    private val profileLoggedOutFragment = ProfileLoggedOutFragment()
    private var currentFragmentIndex: Int = 0
    private var currentFragment: Fragment = homeFragment

    companion object {
        private const val CURRENT_FRAGMENT_INDEX = "CURRENT_FRAGMENT_INDEX"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt(CURRENT_FRAGMENT_INDEX)
        }

        currentFragment = indexToFragment(currentFragmentIndex)
        replaceFragment(currentFragment)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home_nav_button -> replaceFragment(homeFragment)
                R.id.menu_nav_button -> replaceFragment(menuFragment)
                R.id.info_nav_button -> replaceFragment(infoFragment)
                R.id.profile_nav_button -> replaceFragment(profileLoggedOutFragment)
            }
            true
        }
    }

    public override fun onStart() {
        super.onStart()
        Database.checkUserLoggedIn()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(CURRENT_FRAGMENT_INDEX, currentFragmentIndex)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view, fragment)
            commit()
        }
        currentFragmentIndex = fragmentToIndex(fragment)
    }

    private fun fragmentToIndex(fragment: Fragment) : Int {
        return when(fragment) {
            homeFragment -> 0
            menuFragment -> 1
            infoFragment -> 2
            profileLoggedOutFragment -> 3
            else -> 0
        }
    }

    private fun indexToFragment(idx: Int) : Fragment {
        return when(idx) {
            0 -> homeFragment
            1 -> menuFragment
            2 -> infoFragment
            3 -> profileLoggedOutFragment
            else -> homeFragment
        }
    }
}