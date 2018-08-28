package com.arianfarahani.parknjit.history

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.arianfarahani.parknjit.MainActivity
import com.arianfarahani.parknjit.R
import com.arianfarahani.parknjit.data.LotObject
import kotlinx.android.synthetic.main.activity_deck_detail.*
import java.io.Serializable


class DeckDetailsActivity : AppCompatActivity() {

    lateinit var lotData: LotObject

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck_detail)

        lotData =
                (intent.getSerializableExtra(getString(R.string.intent_key_lot_data))) as LotObject

        detail_toolbar.title = lotData.name
        setSupportActionBar(detail_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        when (lotData.sitename) {
            "PARK" -> detailBannerImage.setImageResource(R.drawable.banner_parking_deck)
            "Science & Tech Garage" -> detailBannerImage.setImageResource(R.drawable.banner_stpg)
            "Lot 10" -> detailBannerImage.setImageResource(R.drawable.banner_lot_10)
            "FENS2" -> detailBannerImage.setImageResource(R.drawable.banner_fenster_2)
        }

        /**
         * savedInstanceState is non-null when there is fragment state
         * saved from previous configurations of this activity
         * In this case, the fragment will automatically be re-added
         * to its container so we don't need to manually add it.
         */
        if (savedInstanceState == null) {
            val fragment = DeckDetailFragment.newInstance(lotData as Serializable)
            supportFragmentManager.beginTransaction()
                .add(R.id.deck_detail_container, fragment)
                .commit()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpTo(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
