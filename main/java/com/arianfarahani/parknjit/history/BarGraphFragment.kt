package com.arianfarahani.parknjit.history

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arianfarahani.parknjit.R
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_bar_graph.*
import java.util.*

class BarGraphFragment : Fragment() {

    lateinit var dayOfWeek: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dayOfWeek = arguments!!.getString(ARG_SAVED_LOTS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bar_graph, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        barChartLoading.visibility = View.VISIBLE
        setupChart()
        setTotal(arguments!!.getInt(ARG_TOTAL_SPACES))

        val viewModel = ViewModelProviders.of(activity!!).get(HistoryViewModel::class.java)
        viewModel.getHistoryData().observe(this, Observer {
            if (it != null) {
                val data = it.days[dayOfWeek]
                if (data != null)
                    loadData(data)
            }
        })
    }

    private fun setupChart() {
        detailBarChart.setDrawGridBackground(false)
        detailBarChart.axisLeft.setDrawTopYLabelEntry(false)
        detailBarChart.axisLeft.setDrawZeroLine(false)
        detailBarChart.axisLeft.setDrawAxisLine(false)
        detailBarChart.axisLeft.setDrawLabels(false)

        detailBarChart.axisRight.setDrawTopYLabelEntry(false)
        detailBarChart.axisRight.setDrawZeroLine(false)
        detailBarChart.axisRight.setDrawAxisLine(false)
        detailBarChart.axisRight.setDrawLabels(false)

        detailBarChart.xAxis.setDrawGridLines(false)
        detailBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        detailBarChart.xAxis.labelCount = 6
        detailBarChart.xAxis.valueFormatter =
                MyXAxisValueFormatter()
        detailBarChart.xAxis.textSize = 12f

        detailBarChart.legend.isEnabled = false
        detailBarChart.description = null

        detailBarChart.setPinchZoom(false)
        detailBarChart.disableScroll()
        detailBarChart.isDoubleTapToZoomEnabled = false
        detailBarChart.extraBottomOffset = 4f
    }

    private fun setTotal(totalSpaces: Int) {
        detailBarChart.axisRight.removeAllLimitLines()

        val ll = LimitLine(totalSpaces.toFloat(), "total Spaces")
        ll.lineColor = Color.GRAY
        ll.lineWidth = 2f
        ll.textColor = Color.GRAY
        ll.textSize = 10f
        ll.enableDashedLine(50f, 15f, 5f)
        ll.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        detailBarChart.axisRight.addLimitLine(ll)
        detailBarChart.axisRight.setDrawLimitLinesBehindData(true)
    }

    private fun loadData(data: MutableMap<Int, Int>) {
        val entryList = ArrayList<BarEntry>()

        for (i in 6..22) {
            entryList.add(
                BarEntry(
                    i.toFloat(),
                    data.getValue(i).toFloat()
                )
            )
        }

        val dataSet = BarDataSet(entryList, null)
        dataSet.setDrawValues(false)
        dataSet.color = context!!.resources.getColor(R.color.googleBlue)

        val barData = BarData(dataSet)
        barData.isHighlightEnabled = false
        detailBarChart.data = barData
        detailBarChart.notifyDataSetChanged()
        detailBarChart.invalidate()

        val max = (detailBarChart.axisRight.limitLines.get(0).limit)

        detailBarChart.axisRight.axisMinimum = 0f
        detailBarChart.axisRight.axisMaximum = max * 1.1f
        detailBarChart.axisLeft.axisMinimum = 0f
        detailBarChart.axisLeft.axisMaximum = max * 1.1f

        barChartLoading.visibility = View.GONE
    }

    companion object {

        private val ARG_SAVED_LOTS = "chart_day"
        private val ARG_TOTAL_SPACES = "total_spaces"

        fun newInstance(position: Int, total: Int): BarGraphFragment {
            val fragment = BarGraphFragment()
            val args = Bundle()
            args.putString(
                ARG_SAVED_LOTS,
                getDay(position)
            )
            args.putInt(ARG_TOTAL_SPACES, total)
            fragment.arguments = args
            return fragment
        }

        fun getDay(numDay: Int): String {
            when (numDay) {
                0 -> return "Monday"
                1 -> return "Tuesday"
                2 -> return "Wednesday"
                3 -> return "Thursday"
                4 -> return "Friday"
                5 -> return "Saturday"
                6 -> return "Sunday"
                else -> return "Monday"
            }
        }

        class MyXAxisValueFormatter : IAxisValueFormatter {

            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                // "value" represents the position of the label on the axis (x or y)
                if (value < 12)
                    return value.toInt().toString() + "a"
                else if (value.toInt() == 12)
                    return value.toInt().toString() + "p"
                else
                    return (value - 12).toInt().toString() + "p"
            }
        }
    }
}