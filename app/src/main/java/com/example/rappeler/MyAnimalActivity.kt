package com.example.rappeler

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rappeler.databinding.ActivityMyAnimalBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MyAnimalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyAnimalBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token: String
    private var adapter: MyAnimalAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAnimalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbAnimal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tbAnimal.setNavigationOnClickListener { onBackPressed() }

        // Set an empty adapter initially
        adapter = MyAnimalAdapter(emptyList())
        binding.rvAnimal.adapter = adapter
        binding.rvAnimal.layoutManager = LinearLayoutManager(this@MyAnimalActivity)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("access_token", null).toString()

        val intent = intent

        when (val source = intent.getStringExtra("source")) {
            "rescue" -> {
                supportActionBar?.title = "My $source"
                retrieveMyRescues(source)
            }
            "adopt" -> {
                supportActionBar?.title = "My $source"
                retrieveMyAdopts(source)
            }
            else -> {
                Toast.makeText(this@MyAnimalActivity, "Some other intent coming from I don't know where",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieveMyAdopts(source : String) {
        val url = "${BuildConfig.URL}user/adopts"
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
                    Toast.makeText(
                        applicationContext,
                        "Failed to fetch adopts: ${e.message}",
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
                        val adoptsList = parseAnimals(responseBody)
                        runOnUiThread {
                            setAdapterAndUpdateList(adoptsList, source)
                        }
                    }
                }
            }
        })
    }

    private fun retrieveMyRescues(source: String) {
        val url = "${BuildConfig.URL}user/rescues"
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
                        val rescuesList = parseAnimals(responseBody)
                        runOnUiThread {
                            setAdapterAndUpdateList(rescuesList, source)
                        }
                    }
                }
            }
        })
    }

    private fun setAdapterAndUpdateList(list: List<Rescue>, source: String) {
        if (list.isEmpty()) {
            // If the list is empty, set the visibility of RecyclerView to GONE
            binding.rvAnimal.visibility = View.GONE
            // Show the placeholder text
            binding.tvPlaceHolder.visibility = View.VISIBLE
            binding.tvPlaceHolder.text = "No Animals Available to Adopt"
        } else {
            // If the list is not empty, set the visibility of RecyclerView to VISIBLE
            binding.rvAnimal.visibility = View.VISIBLE
            // Hide the placeholder text
            binding.tvPlaceHolder.visibility = View.GONE
            // Create adapter and set it to the RecyclerView
            adapter?.updateList(list) ?: run {
                adapter = MyAnimalAdapter(list)
                binding.rvAnimal.adapter = adapter
                binding.rvAnimal.layoutManager = LinearLayoutManager(this@MyAnimalActivity)
            }
        }
        supportActionBar?.title = "My $source"
    }

    private fun parseAnimals(responseBody: String?): List<Rescue> {
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
                    imageString = rescueJson.optString("image_url"),
                    gender = rescueJson.optString("gender"),
                    color = rescueJson.optString("color"),
                    status = rescueJson.optString("status")
                )
                rescuesList.add(rescue)
            }
        }
        return rescuesList
    }
}