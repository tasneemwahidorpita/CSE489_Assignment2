package com.example.mediaportal

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.mediaportal.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Setup Hamburger Icon Toggle for Navigation Drawer
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.menu_home,
            R.string.menu_home
        )
        binding.drawerLayout.addDrawerListener(toggle)
        binding.drawerLayout.addDrawerListener(object : androidx.drawerlayout.widget.DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) = Unit
            override fun onDrawerClosed(drawerView: android.view.View) = Unit
            override fun onDrawerStateChanged(newState: Int) = Unit
            override fun onDrawerOpened(drawerView: android.view.View) {
                val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (current is HomeFragment) binding.navView.setCheckedItem(R.id.nav_home)
            }
        })
        toggle.syncState()
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_white)

        binding.navView.setNavigationItemSelectedListener(this)

        // Register modern OnBackPressedCallback to handle drawer closing
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Initial default destination screen
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), getString(R.string.title_home), R.id.nav_home)
        }
    }

    fun selectDrawerItem(menuId: Int) {
        when (menuId) {
            R.id.nav_home -> replaceFragment(HomeFragment(), getString(R.string.title_home), R.id.nav_home)
            R.id.nav_broadcast -> replaceFragment(BroadcastReceiverFragment(), getString(R.string.title_broadcast_receiver), R.id.nav_broadcast)
            R.id.nav_image -> replaceFragment(ImageScaleFragment(), getString(R.string.title_image_scale), R.id.nav_image)
            R.id.nav_video -> replaceFragment(VideoFragment(), getString(R.string.title_video), R.id.nav_video)
            R.id.nav_audio -> replaceFragment(AudioFragment(), getString(R.string.title_audio), R.id.nav_audio)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectDrawerItem(item.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment, title: String, menuId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        binding.toolbar.title = title
        binding.navView.setCheckedItem(menuId)
    }
}
