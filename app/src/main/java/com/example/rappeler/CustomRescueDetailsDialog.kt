package com.example.rappeler

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class CustomRescueDetailsDialog(
    context: Context,
    private val rescue: Rescue,
    private val token: String
) : Dialog(context) {
private lateinit var text: TextView
private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.adopt_dialog)
        text = findViewById<TextView>(R.id.textWords)
        progressBar = findViewById(R.id.progressBar)
            updateStatus(rescue)
    }

    private fun updateStatus(rescue: Rescue) {
        progressBar.visibility = ProgressBar.VISIBLE
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
                (context as? AdoptActivity)?.runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    println(e.message)
                    Toast.makeText(
                        context,
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
                        (context as? AdoptActivity)?.runOnUiThread {
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(
                                context,
                                errorMessage ?: "Unknown error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        (context as? AdoptActivity)?.runOnUiThread {
                            progressBar.visibility = ProgressBar.GONE
                            text.text = buildString {
                                append("Thank you for adopting ")
                                append(rescue.species)
                                append(". Please visit your profile to follow up on your adoption process")
                            }
                        }
                    }
                }
            }
        })
    }
}