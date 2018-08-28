package com.arianfarahani.parknjit.realtime

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.arianfarahani.parknjit.R


class StreetFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mapView = view!!.findViewById<WebView>(R.id.googlemap)
        mapView.webViewClient = WebViewClient()
        val webSettings = mapView.getSettings()
        webSettings.setJavaScriptEnabled(true)
        //mapView.loadUrl("https://www.google.com/")
        mapView.loadUrl("https://www.google.com/maps/d/u/0/embed?mid=18OYaTlGo9vijxsNLpijiOriELpg&ll=40.74200798654736%2C-74.17874604999997&z=17")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_street, container, false)
    }

    companion object {

        fun newInstance(): StreetFragment {
            return StreetFragment()
        }
    }
}// Required empty public constructor
