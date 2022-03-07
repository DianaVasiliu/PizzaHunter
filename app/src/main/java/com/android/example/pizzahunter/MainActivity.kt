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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        replaceFragment(homeFragment)

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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view, fragment)
            commit()
        }
    }
}