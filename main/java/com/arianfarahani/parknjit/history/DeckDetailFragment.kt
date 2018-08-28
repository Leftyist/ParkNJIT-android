package com.arianfarahani.parknjit.history


import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.arianfarahani.parknjit.R
import com.arianfarahani.parknjit.data.LotObject
import kotlinx.android.synthetic.main.deck_detail.*
import java.io.Serializable
import java.util.*


class DeckDetailFragment : Fragment() {

    lateinit var lot: LotObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lotData: Serializable? = arguments?.getSerializable(ARG_SAVED_LOTS)

        if (lotData != null) {
            @Suppress("UNCHECKED_CAST")
            lot = lotData as LotObject
        } else {
            throw java.lang.InstantiationException("Failed to provide lot data to fragment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.deck_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        detailLiveText.text = detailLiveText.text.toString().replace(
            "%x",
            lot.available.toString()
        )

        val filterOptions = resources.getStringArray(R.array.list_filter_options)
        val filterTitle = getString(R.string.list_filter_title)
        detailFilterImageButton.setOnClickListener {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(filterTitle)
            builder.setItems(filterOptions, DialogInterface.OnClickListener { dialog, which ->
                //toast("Selected: " + which)
                //TODO: filter based on semester
            })
            builder.show()
        }

        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val curDay = when (day) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        detailDaySpinner.setSelection(curDay)
        detailDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                barChartViewPager.setCurrentItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        barChartViewPager.setAdapter(ChartSliderPagerAdapter(fragmentManager!!))
        barChartViewPager.setCurrentItem(curDay)
        barChartViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                detailDaySpinner.setSelection(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }
        })

        ViewModelProviders.of(activity!!).get(HistoryViewModel::class.java)
            .refreshHistoryData("`" + lot.sitename + "`", "0", "10000000")
    }

    private inner class ChartSliderPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return BarGraphFragment.newInstance(
                position,
                lot.total
            )
        }

        override fun getCount(): Int {
            return 7
        }
    }

    companion object {

        private val ARG_SAVED_LOTS = "lot_info"

        fun newInstance(lot: Serializable?): DeckDetailFragment {
            val fragment = DeckDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_SAVED_LOTS, lot)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
