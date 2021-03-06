/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile

import android.app.Fragment
import android.app.FragmentTransaction
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main_layout.*
import vandyke.siamobile.about.AboutFragment
import vandyke.siamobile.about.AboutSiaActivity
import vandyke.siamobile.about.FragmentSetupRemote
import vandyke.siamobile.about.ModesActivity
import vandyke.siamobile.backend.CleanupService
import vandyke.siamobile.backend.coldstorage.ColdStorageService
import vandyke.siamobile.backend.siad.SiadMonitorService
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.files.fragments.FilesFragment
import vandyke.siamobile.hosting.fragments.HostingFragment
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.settings.GlobalPrefsListener
import vandyke.siamobile.settings.SettingsFragment
import vandyke.siamobile.terminal.TerminalFragment
import vandyke.siamobile.wallet.WalletFragment
import vandyke.siamobile.wallet.dialogs.PaperWalletFragment
import vandyke.siamobile.wallet.dialogs.WalletCreateDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var globalPrefsListener: GlobalPrefsListener

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var titleBackstack: Stack<String>
    private lateinit var menuItemBackstack: Stack<Int>
    private lateinit var classBackstack: Stack<Class<*>>
    private var currentlyVisibleFragment: Fragment? = null

    enum class Theme {
        LIGHT, DARK, AMOLED, CUSTOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        globalPrefsListener = GlobalPrefsListener(this)
        prefs.registerOnSharedPreferenceChangeListener(globalPrefsListener)
        when (prefs.theme) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.AppTheme_Light)
                appTheme = Theme.LIGHT
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.AppTheme_Light)
                appTheme = Theme.DARK
            }
            "amoled" -> {
                setTheme(R.style.AppTheme_Amoled)
                appTheme = Theme.AMOLED
            }
            "custom" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.AppTheme_Custom)
                appTheme = Theme.CUSTOM
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)
        if (appTheme == Theme.CUSTOM) {
            val b = Base64.decode(prefs.customBgBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            window.setBackgroundDrawable(BitmapDrawable(resources, bitmap))
        }

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (prefs.transparentBars) {
            toolbar.setBackgroundColor(android.R.color.transparent)
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }

        val a = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, a, true)
        backgroundColor = a.data

        defaultTextColor = TextView(this).currentTextColor

        titleBackstack = Stack<String>()
        menuItemBackstack = Stack<Int>()
        classBackstack = Stack<Class<*>>()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        // set action stuff for when drawer items are selected
        val drawerListener = NavigationView.OnNavigationItemSelectedListener { item ->
            drawerLayout?.closeDrawers()
            val menuItemId = item.itemId
            when (menuItemId) {
                R.id.drawer_item_files -> {
                    displayFragmentClass(FilesFragment::class.java, "Files", menuItemId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.drawer_item_hosting -> {
                    displayFragmentClass(HostingFragment::class.java, "Hosting", menuItemId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.drawer_item_wallet -> {
                    displayFragmentClass(WalletFragment::class.java, "Wallet", menuItemId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.drawer_item_terminal -> {
                    displayFragmentClass(TerminalFragment::class.java, "Terminal", menuItemId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.drawer_item_settings -> {
                    displayFragmentClass(SettingsFragment::class.java, "Settings", menuItemId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.drawer_item_about -> {
                    displayFragmentClass(AboutFragment::class.java, "About", menuItemId)
                    return@OnNavigationItemSelectedListener false
                }
            }
            true
        }
        navigationView.setNavigationItemSelectedListener(drawerListener)
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)

        startService(Intent(this, CleanupService::class.java))
        if (prefs.operationMode == "local_full_node")
            startService(Intent(this, SiadMonitorService::class.java))
        else if (prefs.operationMode == "cold_storage")
            startService(Intent(this, ColdStorageService::class.java))

        startService(Intent(this, WalletMonitorService::class.java))

        when (prefs.startupPage) {
            "files" -> displayFragmentClass(FilesFragment::class.java, "Files", R.id.drawer_item_files)
            "hosting" -> displayFragmentClass(HostingFragment::class.java, "Hosting", R.id.drawer_item_hosting)
            "wallet" -> displayFragmentClass(WalletFragment::class.java, "Wallet", R.id.drawer_item_wallet)
            "terminal" -> displayFragmentClass(TerminalFragment::class.java, "Terminal", R.id.drawer_item_terminal)
        }

        if (prefs.firstTime) {
            startActivityForResult(Intent(this, ModesActivity::class.java), REQUEST_MODE)
            startActivity(Intent(this, AboutSiaActivity::class.java))
            prefs.firstTime = false
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MODE) {
            if (resultCode == ModesActivity.PAPER_WALLET) {
                displayFragmentClass(PaperWalletFragment::class.java, "Generated paper wallet", null)
            } else if (resultCode == ModesActivity.COLD_STORAGE) {
                prefs.operationMode = "cold_storage"
                displayFragmentClass(WalletFragment::class.java, "Wallet", R.id.drawer_item_wallet)
                if (currentlyVisibleFragment is WalletFragment)
                    (currentlyVisibleFragment as WalletFragment).replaceExpandFrame(WalletCreateDialog())
            } else if (resultCode == ModesActivity.REMOTE_FULL_NODE) {
                prefs.operationMode = "remote_full_node"
                displayFragmentClass(FragmentSetupRemote::class.java, "Remote setup", null)
            } else if (resultCode == ModesActivity.LOCAL_FULL_NODE) {
                displayFragmentClass(WalletFragment::class.java, "Wallet", R.id.drawer_item_wallet)
                if (Utils.isSiadSupported) {
                    prefs.operationMode = "local_full_node"
                } else
                    Toast.makeText(this, "Sorry, but your device's CPU architecture is not supported by Sia's full node", Toast.LENGTH_LONG).show()
                displayFragmentClass(WalletFragment::class.java, "Wallet", R.id.drawer_item_wallet)
            }
        }
    }

    fun displayFragmentClass(clazz: Class<*>, title: String, menuItemId: Int?) {
        val className = clazz.simpleName
        val fragmentManager = fragmentManager
        var fragmentToBeDisplayed: Fragment? = fragmentManager.findFragmentByTag(className)
        val transaction = fragmentManager.beginTransaction()

        if (currentlyVisibleFragment != null) {
            if (currentlyVisibleFragment === fragmentToBeDisplayed)
                return
            transaction.hide(currentlyVisibleFragment)
        }

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        if (currentlyVisibleFragment != null)
            transaction.hide(currentlyVisibleFragment)

        if (fragmentToBeDisplayed == null) {
            try {
                fragmentToBeDisplayed = clazz.newInstance() as Fragment
                transaction.addToBackStack(className)
                transaction.add(R.id.fragment_frame, fragmentToBeDisplayed, className)
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        } else {
            transaction.show(fragmentToBeDisplayed)
        }
        setTitle(title)
        transaction.commit()
        currentlyVisibleFragment = fragmentToBeDisplayed
        titleBackstack.push(title)
        menuItemBackstack.push(menuItemId)
        classBackstack.push(clazz)
        if (menuItemId != null)
            navigationView.setCheckedItem(menuItemId)

    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else if (titleBackstack.size <= 1) {
            Utils.getDialogBuilder(this)
                    .setTitle("Quit?")
                    .setPositiveButton("Yes") { dialogInterface, i -> finish() }
                    .setNegativeButton("No", null)
                    .show()
        } else {
            titleBackstack.pop()
            menuItemBackstack.pop()
            classBackstack.pop()
            displayFragmentClass(classBackstack.pop(), titleBackstack.pop(), menuItemBackstack.pop())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    fun copyTextView(view: View) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Sia text touch copy", (view as TextView).text)
        clipboard.primaryClip = clip
        Utils.snackbar(view, "Copied selection to clipboard", Snackbar.LENGTH_SHORT)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu)
        return true
    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    companion object {
        var backgroundColor: Int = 0
        var defaultTextColor: Int = 0
        var REQUEST_MODE = 2
        var appTheme: Theme? = null
    }
}
