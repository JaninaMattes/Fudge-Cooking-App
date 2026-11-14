package com.mobilesystems.feedme

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobilesystems.feedme.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val searchView: SearchView? = null
    private val searchItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askForNotificationPermission()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navigation
        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.navigation_shoppingList,
                    R.id.navigation_inventorylist,
                    R.id.navigation_dashboard,
                    R.id.navigation_recipes,
                ),
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = searchItem?.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.user_profile -> {
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.userProfileFragment)
            }
            R.id.favorite_recipes -> {
                Toast.makeText(this, "Not yet implemented!!", Toast.LENGTH_SHORT).show()
            } R.id.logout -> {
                navController.navigateUp()
                navController.navigate(R.id.logoutFragment)
            }
            R.id.more -> {
                Toast.makeText(this, "Not yet implemented!!", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Barcode Scanning with ZXing
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        // This is important, otherwise the result will not be passed to the fragment
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult is called.")
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchView?.clearFocus()
        searchView?.setQuery("", false)
        searchItem?.collapseActionView()
        // This method can be used when a query is submitted eg. creating search history using SQLite DB
        Toast.makeText(this@MainActivity, "Looking for $query", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Filter for text
        // adapter.filter(query)
        return false
    }

    /**
     * Asks for the permission to send a notification.
     *
     * Android 13 and later drop every notification of an application that
     * holds no permission, and they drop it without a word. The user is asked
     * once; a refusal stops no other part of the application.
     */
    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val granted =
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST,
            )
        }
    }
}

/** Number that identifies the question for the notification permission. */
private const val NOTIFICATION_PERMISSION_REQUEST = 1001
