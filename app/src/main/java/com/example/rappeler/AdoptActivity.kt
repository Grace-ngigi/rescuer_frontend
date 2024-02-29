package com.example.rappeler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rappeler.databinding.ActivityAdoptBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class AdoptActivity : AppCompatActivity(), AdoptAdapter.OnItemClickListener {
    private val TOKEN_KEY = "access_token"
    private lateinit var binding : ActivityAdoptBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdoptAdapter
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAdoptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbAdopt)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Ready To Adopt"
        binding.tbAdopt.setNavigationOnClickListener { onBackPressed() }
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString(TOKEN_KEY, null).toString()
        proceed()
    }
    private fun proceed() {
        println("User token: $token")
        recyclerView = binding.rvRescues
        recyclerView.layoutManager = LinearLayoutManager(this)
        getRescues(token)
    }
    override fun onItemClick(rescue: Rescue) {

        updateStatus(rescue)
//        val customDialog = CustomRescueDetailsDialog(this, rescue, token)
//        customDialog.setOnDismissListener {
//            getRescues(token)
//        }
//        customDialog.show()
    }
    private fun updateStatus(rescue: Rescue) {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        val url = BuildConfig.URL + "/adopt?rescue_id=${rescue.id}"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    "{}"
                )
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    binding.progressBar.visibility = ProgressBar.GONE
                    println(e.message)
                    Toast.makeText(
                        this@AdoptActivity,
                        "Failed to create adopt: ${e.message}",
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
                        println("Error Message: $errorMessage")
                        runOnUiThread {
                            binding.progressBar?.visibility = ProgressBar.GONE
                            Toast.makeText(
                                this@AdoptActivity,
                                errorMessage ?: "Unknown error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        runOnUiThread {
                            binding.progressBar.visibility = ProgressBar.GONE
                            val text = buildString {
                                append("Thank you for adopting ")
                                append(rescue.species)
                                append(". Please visit your profile to follow up on your adoption process")
                            }
                            Toast.makeText(this@AdoptActivity, text, Toast.LENGTH_LONG).show()
                        }
                        getRescues(token)
                    }
                }
            }
        })
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
//                            adapter = AdoptAdapter(rescuesList, this@AdoptActivity)
//                            recyclerView.adapter = adapter
                            if (rescuesList.isEmpty()) {
                                // If the list is empty, set the visibility of RecyclerView to GONE
                                recyclerView.visibility = View.GONE
                                // Show the placeholder text
                                binding.tvPlaceHolder.visibility = View.VISIBLE
                                binding.tvPlaceHolder.text = "No Animals Available to Adopt"
                            } else {
                                // If the list is not empty, set the visibility of RecyclerView to VISIBLE
                                recyclerView.visibility = View.VISIBLE
                                // Hide the placeholder text
                                binding.tvPlaceHolder.visibility = View.GONE
                                // Create adapter and set it to the RecyclerView
                                adapter = AdoptAdapter(rescuesList, this@AdoptActivity)
                                recyclerView.adapter = adapter
                            }
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

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
