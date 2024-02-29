package com.example.rappeler

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.Manifest
import android.content.pm.PackageManager
import android.health.connect.datatypes.units.Length
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.ImageView
import android.widget.Toast
import com.example.rappeler.databinding.ActivityRescueBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class RescueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRescueBinding
    private lateinit var imageUrl: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token : String


    private val CAMERA_REQUEST_CODE = 1001
    private val PERMISSION_REQUEST_CODE = 1002
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRescueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tbRescue)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "RESCUE"
        binding.tbRescue.setNavigationOnClickListener { onBackPressed() }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("access_token", null).toString()
        binding.btnSubmit.setOnClickListener {
            submitRescue(token)
        }
        binding.btnPhoto.setOnClickListener {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestPermission()
            }
        }
    }

    // Function to validate input fields
    private fun validateInput(): Boolean {
        val species = binding.etSpecies.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (species.isEmpty()) {
            binding.etSpecies.error = "Species is required"
            return false
        }
        if (age.isEmpty()) {
            binding.etAge.error = "Age is required"
            return false
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            return false
        }
        if (location.isEmpty()) {
            binding.etLocation.error = "Location is required"
            return false
        }
        return true
    }

    private fun submitRescue(savedToken: String) {
        if (!validateInput()) {
            return
        }

        val url = BuildConfig.URL + "rescues"
        val client = OkHttpClient()

        val requestBody = JSONObject().apply {
            put("species", binding.etSpecies.text.toString().trim())
            put("age", binding.etAge.text.toString().trim())
            put("description", binding.etDescription.text.toString().trim())
            put("location", binding.etLocation.text.toString().trim())
            put("image_url", imageUrl)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $savedToken")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    requestBody.toString()
                )
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    println(e.message)
                    Toast.makeText(
                        applicationContext,
                        "Failed to submit Rescue: ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.toString()
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
                    }
//                    val statusCode = response.code
//                    val jsonResponse = responseBody?.let { JSONObject(it) }
                    val intent = Intent(this@RescueActivity, HomeActivity::class.java)
                    startActivity(intent)
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Rescue Submitted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission required to take photos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.ivImage.setImageBitmap(imageBitmap)
            val base64Image = encodeImageToBase64(imageBitmap)
            imageUrl = base64Image
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}