package com.arianfarahani.parknjit.realtime

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.arianfarahani.parknjit.data.LotObject

class RealtimeViewModel : ViewModel() {
    private val liveParkingData = MutableLiveData<MutableList<LotObject>>()


    fun getLiveData(): LiveData<MutableList<LotObject>> {
        return liveParkingData
    }

    fun refreshLiveData() {
        WebService.refreshLiveData(liveParkingData)
    }

}