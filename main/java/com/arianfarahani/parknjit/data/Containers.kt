package com.arianfarahani.parknjit.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LotObject(
    @SerializedName("Name")
    var name: String,
    @SerializedName("Available")
    val available: Int,
    @SerializedName("Total")
    val total: Int,
    @SerializedName("Description")
    val description: String,
    @SerializedName("Occupied")
    val occupied: Int,
    @SerializedName("Address")
    val address: String,
    @SerializedName("SiteName")
    val sitename: String
) : Comparable<LotObject>, Serializable {

    override fun compareTo(other: LotObject): Int {
        if (this.available == other.available) return 0
        else if (this.available > other.available) return -1
        else return 1
    }
}

data class Decks(
    @SerializedName("decks")
    val decks: HashMap<Int, LotObject>
)

data class Days(
    @SerializedName("days")
    val days: MutableMap<String, MutableMap<Int,Int>>
)

