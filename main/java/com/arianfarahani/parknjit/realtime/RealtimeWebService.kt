package com.arianfarahani.parknjit.realtime

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.arianfarahani.parknjit.data.Decks
import com.arianfarahani.parknjit.data.LotObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object RealtimeWebService {

    private val TAG = "RealtimeWebService"
    private val liveClient =
        Retrofit.Builder()
            .baseUrl("https://mobile.njit.edu/parking/")
            .addConverterFactory(
                GsonConverterFactory.create()
            ).client(OkHttpClient.Builder().build())
            .build()
            .create(NJITParkService::class.java)

    private interface NJITParkService {
        @GET("cached.php")
        fun getData(): Call<Decks>
    }

    interface RealTimeDirectPull {
        fun onReturn(data: MutableList<LotObject>?)
    }

    private fun getLiveData(onReturn: (MutableList<LotObject>?) -> Unit) {
        val call = liveClient.getData()

        call.enqueue(object : Callback<Decks> {
            override fun onResponse(
                call: Call<Decks>,
                response: Response<Decks>
            ) {

                val data = response.body()?.decks?.values?.filter {
                    it.sitename != "FENS1"
                }?.toMutableList()

                //shorten one of the names
                data?.find { it.name.equals("Science & Technology Park Garage") }?.name =
                        "Science & Tech Garage"

                data?.sort()

                Log.d(TAG, data.toString())
                onReturn(data)
            }

            override fun onFailure(call: Call<Decks>, t: Throwable) {
                Log.d(TAG, t.toString())
                onReturn(null)
            }
        })
    }

    fun refreshLiveData(listener: RealTimeDirectPull) {
        getLiveData { listener.onReturn(it) }
    }

    fun refreshLiveData(listener: MutableLiveData<MutableList<LotObject>>) {
        getLiveData { listener.value = it }
    }
}