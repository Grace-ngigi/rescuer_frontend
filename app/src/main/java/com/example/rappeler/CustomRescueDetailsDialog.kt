package com.example.rappeler

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.example.rappeler.databinding.AdoptDialogBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class CustomRescueDetailsDialog(
    context: Context,
    private val token: String
) : Dialog(context) {
    private lateinit var binding: AdoptDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdoptDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnSubmit = binding.btnSubmit
        btnSubmit.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val phone = binding.editTextPhone.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            updateProfile(email, phone, password)
        }
    }

    private fun updateProfile(email: String, phone: String, password: String) {
        println("Pretending to update users profile")
        binding.progressBar.visibility = ProgressBar.VISIBLE
        val url = BuildConfig.URL + "/user"
        val client = OkHttpClient()
        val requestBody = JSONObject().apply {
            put("email", email)
            put("phone", phone)
            put("password", password)
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .put(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    requestBody.toString()
                )
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                dismissDialogWithError("Failed to update profile: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        val jsonResponse = responseBody?.let { JSONObject(it) }
                        val errorMessage = jsonResponse?.getString("message")
                        dismissDialogWithError(errorMessage ?: "Unknown error occurred")
                    } else {
                        dismiss()
                        (context as? AdoptActivity)?.runOnUiThread {
                            Toast.makeText(
                                context,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

    private fun dismissDialogWithError(errorMessage: String) {
        (context as? AdoptActivity)?.runOnUiThread {
            binding.progressBar.visibility = ProgressBar.GONE
            Toast.makeText(
                context,
                errorMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
