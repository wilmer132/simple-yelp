package edu.stanford.simpleyelp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "NmDksfEVLoYB0oejFzttfXLx9tSYciWLGDT7WJDMIj9V_u3u1bd07oZWfdWTE_lmimycdxcQ5t1MFKBYUXBPWXJJH16nyBlj7epu_K2SAkWq4YL4q3YHM7XRD-CTYXYx"
class MainActivity : AppCompatActivity() {
    private lateinit var rvRestaurants: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvRestaurants = findViewById<RecyclerView>(R.id.rvRestaurants)

        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants)

        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val retrofit =
            Retrofit.
                Builder().baseUrl(BASE_URL).
                addConverterFactory(GsonConverterFactory.create())
                .build()
        val yelpService = retrofit.create(YelpService::class.java)

        yelpService.searchRestaurants("Bearer $API_KEY","Avocado Toast", "New York")
            .enqueue(object : Callback<YelpSearchResult> {
                override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Did not receive valid response from Yelp API... exiting")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    if (!isOnline()) {
                        Log.w(TAG, "Device is not connected. Please connect to internet prior to using the app... exiting")
                        Toast.makeText(this@MainActivity, "Device must be connected to the Internet.", Toast.LENGTH_LONG).show()
                        return
                    }
                    Log.i(TAG, "onFailure $t")
                }
            })
    }

    fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }
}