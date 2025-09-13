package com.example.mini_project_week6

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Show HomeFragment by default
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, HomeFragment())
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, HomeFragment())
                    }
                    true
                }
                R.id.nav_main -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, MainFragment())
                        addToBackStack(null) // for proper back navigation
                    }
                    true
                }
                else -> false
            }
        }
    }
}
