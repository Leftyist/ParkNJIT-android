package com.arianfarahani.parknjit.realtime

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.arianfarahani.parknjit.R
import com.arianfarahani.parknjit.data.LotObject
import kotlinx.android.synthetic.main.fragment_deck.*
import org.jetbrains.anko.toast
import java.io.Serializable
import java.net.URLEncoder


class DeckFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(activity!!).get(RealtimeViewModel::class.java)
        viewModel.getLiveData().observe(this, Observer {
            if (it != null)
                populateCards(it)
            else {
                context!!.toast("Error downloading data")
                swiperefresh.isRefreshing = false
            }
        })

        deck_card_recyclerview.setHasFixedSize(true)
        deck_card_recyclerview.layoutManager = LinearLayoutManager(activity)
        deck_card_recyclerview.adapter =
                ParkingCardsRecyclerView()
        swiperefresh.setOnRefreshListener { refreshLiveData() }

        deck_lot12_banner_warning.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val address = "27 Lock Street, Newark, NJ 07103"
            intent.data = Uri.parse(String.format("geo:0,0?q=%s", URLEncoder.encode(address)))
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_deck, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {

            R.id.menu_refresh -> {
                swiperefresh.isRefreshing = true
                refreshLiveData()
                return true
            }
        }

        // User didn't trigger a refresh, let the superclass handle this action
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val parentView = inflater.inflate(R.layout.fragment_deck, container, false)
        return parentView
    }

    override fun onStart() {
        super.onStart()

        val storedLots: Serializable? = arguments?.getSerializable(ARG_SAVED_LOTS)

        if (storedLots != null) {
            @Suppress("UNCHECKED_CAST")
            val lots = storedLots as MutableList<LotObject>
            populateCards(lots)
        } else {
            refreshLiveData()
        }
    }

    private fun showWarningBanner(shouldShow: Boolean) {
        if(shouldShow) {
            if (android.os.Build.VERSION.SDK_INT >= 21)
                view?.findViewById<AppBarLayout>(R.id.appbar)?.elevation = 0f

            deck_lot12_banner_warning.visibility = View.VISIBLE
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 21)
                view?.findViewById<AppBarLayout>(R.id.appbar)?.elevation = 4f

            deck_lot12_banner_warning.visibility = View.GONE
        }

    }

    private fun populateCards(lots: MutableList<LotObject>) {

        (deck_card_recyclerview.adapter as ParkingCardsRecyclerView).lotData = lots
        deck_card_recyclerview.adapter.notifyDataSetChanged()

        if (lots[0].available < 50)
            showWarningBanner(true)
        else
            showWarningBanner(false)

        swiperefresh.isRefreshing = false
    }

    private fun refreshLiveData() {
        swiperefresh.isRefreshing = true
        val viewModel = ViewModelProviders.of(activity!!).get(RealtimeViewModel::class.java)
        viewModel.refreshLiveData()
    }

    companion object {

        private val ARG_SAVED_LOTS = "saved_lots"

        fun newInstance(lots: Serializable?): DeckFragment {
            val fragment = DeckFragment()
            val args = Bundle()
            args.putSerializable(ARG_SAVED_LOTS, lots)
            fragment.arguments = args
            return fragment
        }
    }
}
