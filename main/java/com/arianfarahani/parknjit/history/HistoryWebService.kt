package com.arianfarahani.parknjit.history

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.arianfarahani.parknjit.data.Days
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


object HistoryWebService {
    private val TAG = "HistoryWebService"

    private val historyClient =
        Retrofit.Builder()
            .baseUrl("http://www.parknjit.com/history/")
            .addConverterFactory(
                GsonConverterFactory.create()
            ).client(OkHttpClient.Builder().build())
            .build()
            .create(HistoryService::class.java)

    private interface HistoryService {
        @POST("getit.php")
        @FormUrlEncoded
        fun getData(
            @Field("park") park: String,
            @Field("start_time") start: String,
            @Field("end_time") end: String
        )
                : Call<Days>
    }

    interface HistoryDirectPull {
        fun onReturn(data: Days?)
    }

    private fun getHistoryData(
        park: String,
        start: String,
        end: String,
        onReturn: (Days?) -> Unit
    ) {
        val call = historyClient.getData(park, start, end)

        call.enqueue(object : Callback<Days> {
            override fun onResponse(call: Call<Days>, response: Response<Days>) {
                val data = response.body()
                onReturn(data)
            }

            override fun onFailure(call: Call<Days>?, t: Throwable?) {
                Log.d(TAG, t.toString())
                onReturn(null)
            }
        })
    }

    fun refreshHistoryData(
        park: String,
        start: String,
        end: String,
        listener: HistoryDirectPull
    ) {
        getHistoryData(park, start, end) { listener.onReturn(it) }
    }

    fun refreshHistoryData(
        park: String,
        start: String,
        end: String,
        listener: MutableLiveData<Days>
    ) {
        getHistoryData(park, start, end) {
            listener.value = it
        }
    }
}