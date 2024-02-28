package com.example.rappeler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rappeler.databinding.ActivityAdoptBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

var binding : ActivityAdoptBinding? = null
class AdoptActivity : AppCompatActivity(), AdoptAdapter.OnItemClickListener {
    private val TOKEN_KEY = "access_token"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdoptAdapter
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAdoptBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.tbAdopt)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Ready To Adopt"
        binding?.tbAdopt?.setNavigationOnClickListener { onBackPressed() }
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString(TOKEN_KEY, null).toString()
        proceed(token)
    }
    private fun proceed(token: String) {
        println("User token: $token")
        recyclerView = binding?.rvRescues!!
        recyclerView.layoutManager = LinearLayoutManager(this)
        getRescues(token)
    }
    override fun onItemClick(rescue: Rescue) {
        val customDialog = CustomRescueDetailsDialog(this, rescue, token)
        customDialog.setOnDismissListener {
            getRescues(token)
        }
        customDialog.show()
    }
    private fun getRescues(token: String) {
        val url = BuildConfig.URL + "ready/rescues"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    println(e.message)
                    Toast.makeText(
                        applicationContext,
                        "Failed to fetch rescues: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        val jsonResponse = responseBody?.let { JSONObject(it) }
                        val errorMessage = jsonResponse?.getString("message")

                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                errorMessage ?: "Unknown error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val rescuesList = parseRescues(responseBody)
                        // Update UI on the main thread
                        runOnUiThread {
                            // Create adapter and set it to the RecyclerView
                            adapter = AdoptAdapter(rescuesList, this@AdoptActivity)
                            recyclerView.adapter = adapter
                        }
                    }
                }
            }
        })
    }

    private fun parseRescues(responseBody: String?): List<Rescue> {
        val rescuesList = mutableListOf<Rescue>()
        responseBody?.let {
            val jsonResponseArray = JSONArray(it)
            for (i in 0 until jsonResponseArray.length()) {
                val rescueJson = jsonResponseArray.getJSONObject(i)
                val rescue = Rescue(
                    id = rescueJson.optString("id"),
                    species = rescueJson.optString("species"),
                    age = rescueJson.optInt("age"),
                    vetEvaluation = rescueJson.optString("vet_evaluation"),
                    imageString = rescueJson.optString("image_url")
                )
                rescuesList.add(rescue)
            }
        }
        return rescuesList
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
