package com.arianfarahani.parknjit.realtime

import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.arianfarahani.parknjit.R
import com.arianfarahani.parknjit.data.LotObject
import com.arianfarahani.parknjit.history.DeckDetailsActivity
import com.github.lzyzsd.circleprogress.DonutProgress
import java.io.Serializable
import java.net.URLEncoder

class ParkingCardsRecyclerView :
    RecyclerView.Adapter<ParkingCardsRecyclerView.ViewHolder>() {

    var lotData: MutableList<LotObject> = mutableListOf()

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        val nameText = view.findViewById<TextView>(R.id.deck_card_name)
        val progressArc = view.findViewById<DonutProgress>(R.id.deck_arc_progress)
        val availableText = view.findViewById<TextView>(R.id.deck_text_num_spots)
        val navigateButton = view.findViewById<Button>(R.id.deck_button_navigate)
        val detailsButton = view.findViewById<Button>(R.id.deck_button_details)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.deck_card_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lot = lotData[position]

        holder.nameText.text = lot.name
        holder.availableText.text = lot.available.toString()

        val progressPercent = (lot.occupied.toFloat() / lot.total.toFloat()) * 100
        holder.progressArc.text = progressPercent.toInt().toString() + "%"
        holder.progressArc.max = lot.total

        //the bar resets after 100 so just cap it there
        if (lot.occupied > lot.total)
            holder.progressArc.progress = lot.total.toFloat()
        else
            holder.progressArc.progress = lot.occupied.toFloat()

        val context = holder.view.context

        holder.navigateButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(String.format("geo:0,0?q=%s", URLEncoder.encode(lot.address)))
            if (intent.resolveActivity(context.packageManager) != null) {
                ContextCompat.startActivity(context, intent, null)
            }
        }

        holder.detailsButton.setOnClickListener {
            val intent = Intent(context, DeckDetailsActivity::class.java)
            intent.putExtra(context.getString(R.string.intent_key_lot_data), lot as Serializable)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return lotData.size
    }


}