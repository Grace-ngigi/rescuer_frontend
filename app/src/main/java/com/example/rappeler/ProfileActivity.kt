package com.example.rappeler

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rappeler.databinding.ActivityProfileBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbProfile)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "My Profile"
        binding.tbProfile.setNavigationOnClickListener { onBackPressed() }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("access_token", null).toString()

        getProfile()

        binding.tvRescue.setOnClickListener {
            val intent = Intent(this@ProfileActivity, MyAnimalActivity::class.java)
            intent.putExtra("source","rescue")
            startActivity(intent)
        }
        binding.tvAdopt.setOnClickListener {
            val intent = Intent(this@ProfileActivity, MyAnimalActivity::class.java)
            intent.putExtra("source","adopt")
            startActivity(intent)
        }

        binding.ivLogout.setOnClickListener {
            logout()
        }
        binding.ivEdit.setOnClickListener {
            editProfile()
        }
    }

    private fun editProfile() {
        val customDialog = CustomRescueDetailsDialog(this, token)
        customDialog.show()
    }

    private fun logout() {
        sharedPreferences.edit().remove("access_token").apply()
        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun getProfile() {
            val url = BuildConfig.URL + "/profile"
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
                        val jsonResponse = responseBody?.let { JSONObject(it) }
                        if (!response.isSuccessful) {
                            val errorMessage = jsonResponse?.getString("message")

                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    errorMessage ?: "Unknown error occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            runOnUiThread {
                                if (jsonResponse != null) {
                                    binding.tvEmail.text = jsonResponse.getString("email")
                                    binding.tvPhone.text = jsonResponse.getString("phone")
                                }
                            }
                        }
                    }
                }
            })
        }
}