package com.arianfarahani.parknjit.settings

import android.os.Bundle
import com.arianfarahani.parknjit.R


class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
