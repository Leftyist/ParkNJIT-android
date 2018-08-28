package com.arianfarahani.parknjit.history

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.arianfarahani.parknjit.data.Days

class HistoryViewModel : ViewModel() {


    private val historyParkingData = MutableLiveData<Days>()

    fun getHistoryData(): LiveData<Days> {
        return historyParkingData
    }

    fun refreshHistoryData(park: String, start: String, end: String) {
        HistoryWebService.refreshHistoryData(park, start, end, historyParkingData)
    }
}