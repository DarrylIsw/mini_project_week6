package com.example.mini_project_week6

// Android + support library imports
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView

// Main activity that hosts fragments with bottom navigation
class HomeActivity : AppCompatActivity() {

    // Lifecycle method called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Set layout with fragment container + bottom navigation

        // Show HomeFragment by default when activity starts
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, HomeFragment())
        }

        // Setup BottomNavigationView to switch fragments
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Navigate to HomeFragment
                R.id.nav_home -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, HomeFragment())
                    }
                    true
                }
                // Navigate to MainFragment
                R.id.nav_main -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, MainFragment())
                        addToBackStack(null) // allow back navigation to HomeFragment
                    }
                    true
                }
                else -> false // default case
            }
        }
    }
}
