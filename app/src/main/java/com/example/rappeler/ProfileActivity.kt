package com.example.rappeler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rappeler.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbProfile)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "My Profile"
        binding.tbProfile.setNavigationOnClickListener { onBackPressed() }
    }
}