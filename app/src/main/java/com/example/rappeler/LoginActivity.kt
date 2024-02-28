package com.example.rappeler
import com.example.rappeler.BuildConfig

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.rappeler.databinding.ActivityLoginBinding
import com.example.rappeler.databinding.ActivityRegisterBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                return@setOnClickListener
            }

//            val url = "http://192.168.97.226:5100/api/v1/login"
            val url = BuildConfig.URL + "login"
            val client = OkHttpClient()

            val requestBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody.toString()))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        println(e.message)
                        Toast.makeText(applicationContext, "Failed to login: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val responseBodyString = response.body?.string()
                        if (!response.isSuccessful) {
                            val jsonResponse = responseBodyString?.let { JSONObject(it) }
                            val errorMessage = jsonResponse?.getString("message")

                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    errorMessage ?: "Unknown error occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val jsonResponse = responseBodyString?.let { it1 -> JSONObject(it1) }
                            val accessToken = jsonResponse?.getString("access_token")

                            println("Access Token: $accessToken")
                            accessToken?.let {
                                sharedPreferences.edit().putString("access_token", it).apply()
                            }
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "User Logged In",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            })
        }
    }
}