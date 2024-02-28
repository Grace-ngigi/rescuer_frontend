package com.example.rappeler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.rappeler.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRescue.setOnClickListener {
            val intent = Intent(this@HomeActivity, RescueActivity::class.java)
            startActivity(intent)
        }
        binding.btnAdopt.setOnClickListener {
            val intent = Intent(this@HomeActivity, AdoptActivity::class.java)
            startActivity(intent)
        }
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
        }
    }


}