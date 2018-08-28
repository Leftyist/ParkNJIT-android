package com.arianfarahani.parknjit

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.arianfarahani.parknjit.data.logProperties
import com.arianfarahani.parknjit.data.logProperty
import com.arianfarahani.parknjit.geofencing.GeofenceManager
import com.arianfarahani.parknjit.realtime.DeckFragment
import com.arianfarahani.parknjit.realtime.StreetFragment
import com.arianfarahani.parknjit.settings.SettingsActivity
import com.google.firebase.analytics.FirebaseAnalytics
import de.cketti.library.changelog.ChangeLog
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity() {

    private val TAG = "ParkNJITMainActivity"
    private lateinit var mGeofenceManager: GeofenceManager
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private val preferenceListener = { sharedPref: SharedPreferences, key: String ->
        if (key == getString(R.string.pref_geofencing_key)) {
            if (sharedPref.getBoolean(
                    key,
                    getString(R.string.pref_geofencing_default).toBoolean()
                )) {
                mGeofenceManager.add()
            } else {
                mGeofenceManager.remove()
            }
            sharedPref.logProperty(this, mFirebaseAnalytics, key)
        }

    }
    private val CSS = "h1 { margin-left: 8px; font-size: 1.2em; }" + "\n" +
            "h1:first-child { margin-top: 16px; }" + "\n" +
            "li { margin-left: 0px; }" + "\n" +
            "ul { padding-left: 2em; }"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        container.adapter = SectionsPagerAdapter(supportFragmentManager)
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        mGeofenceManager = GeofenceManager(this)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean(
                getString(R.string.pref_geofencing_key),
                getString(R.string.pref_geofencing_default).toBoolean()
            )) {
            mGeofenceManager.add()
        } else {
            mGeofenceManager.remove()
        }

        prefs.logProperties(this, mFirebaseAnalytics)

        val cl = ChangeLog(this, CSS)
        if (cl.isFirstRun) {
            cl.logDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                return true
            }
            R.id.action_about -> {
                showAboutDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(com.arianfarahani.parknjit.R.string.app_name))
        val sb = StringBuilder()

        sb.append(getString(com.arianfarahani.parknjit.R.string.text_version))
        sb.append(": ")
        sb.append(getString(com.arianfarahani.parknjit.R.string.version))

        builder.setMessage(sb.toString())
        builder.setNeutralButton(getString(com.arianfarahani.parknjit.R.string.changelog_full_title)) { dialog, which ->
            dialog.cancel()
            ChangeLog(this, CSS).fullLogDialog.show()
        }
        builder.setNegativeButton(getString(com.arianfarahani.parknjit.R.string.text_close)) { dialog, which -> dialog.cancel() }
        builder.show()
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.

            if (position == 0) {
                val cachedLotData = intent.getSerializableExtra("lots")
                return DeckFragment.newInstance(cachedLotData)
            } else return StreetFragment.newInstance()
        }

        override fun getCount(): Int {
            // Show 2 Total pages.
            return 2
        }
    }
}
