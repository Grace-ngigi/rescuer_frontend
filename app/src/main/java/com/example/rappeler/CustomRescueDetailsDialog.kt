package com.example.rappeler

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.adopt_dialog)

        val textSpecies = findViewById<TextView>(R.id.textSpecies)
        val textAge = findViewById<TextView>(R.id.textAge)
        val textVetEvaluation = findViewById<TextView>(R.id.textVetEvaluation)
        val image = findViewById<ImageView>(R.id.imageAnimal)
        val btnAdopt = findViewById<Button>(R.id.btnAdopt)

        println("Token sent inside the dialog: $token")

        textSpecies.text = rescue.species
        textAge.text = rescue.age.toString()
        textVetEvaluation.text = rescue.vetEvaluation
        val decodedBytes = Base64.decode(rescue.imageString, Base64.DEFAULT)
        val decodedBitmap = decodedBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

        Glide.with(context)
            .load(decodedBitmap)
            .placeholder(R.drawable.ic_launcher_foreground)
//            .error(R.drawable.error_image) // Error image if loading fails
            .into(image)

        btnAdopt.setOnClickListener {
            updateStatus(rescue)
            dismiss()
        }
    }

    private fun updateStatus(rescue: Rescue) {
        val url = BuildConfig.URL + "/adopt?rescue_id=${rescue.id}"
        val client = OkHttpClient()
//        val requestBody = JSONObject().apply {
//            put("rescue_id", rescue.id)
//        }
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
                    println(e.message)
                    Toast.makeText(
                        context,
                        "Failed to create adopt: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        val jsonResponse = responseBody?.let { JSONObject(it) }
                        val errorMessage = jsonResponse?.getString("message")
                        println("Error Message: $errorMessage")
                        (context as? AdoptActivity)?.runOnUiThread {
                            Toast.makeText(
                                context,
                                errorMessage ?: "Unknown error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        (context as? AdoptActivity)?.runOnUiThread {
                            Toast.makeText(
                                context,
                                "Success: Please wait for a call for more Information",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }
}