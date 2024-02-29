package com.example.rappeler
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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

class RegisterActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val phone = binding.editTextPhone.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.editTextEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                binding.editTextPhone.error = "Phone is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password is required"
                return@setOnClickListener
            }

            val url = BuildConfig.URL + "register"
            val client = OkHttpClient()

            val requestBody = JSONObject().apply {
                put("email", email)
                put("phone", phone)
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
                        Toast.makeText(applicationContext, "Failed to register: ${e.message}", Toast.LENGTH_SHORT)
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
                            val jsonResponse = responseBodyString?.let { JSONObject(it) }
                            val accessToken = jsonResponse?.getString("access_token")

                            println("Access Token: $accessToken")
                            accessToken?.let {
                                sharedPreferences.edit().putString("access_token", it).apply()
                            }
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            startActivity(intent)
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "User registered successfully",
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
