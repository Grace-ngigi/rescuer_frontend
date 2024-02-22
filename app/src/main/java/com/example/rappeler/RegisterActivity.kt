package com.example.rappeler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty()) {
                editTextEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                editTextPhone.error = "Phone is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                editTextPassword.error = "Password is required"

            }

            val url = "http://127.0.0.1:5100/api/v1/register"
            val client = OkHttpClient()

            val requestBody = FormBody.Builder()
                .add("email", email)
                .add("phone", phone)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to register", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            throw IOException("Unexpected code $response")
                        }
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "User registered successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Optionally, you can finish the activity here or navigate to another screen
                        }
                    }
                }
            })
        }
    }
}