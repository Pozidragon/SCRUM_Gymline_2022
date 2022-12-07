package com.example.gymline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gymline.databinding.ActivityPreHomeScreenBinding
import com.example.gymline.databinding.ActivityRegAddPersonalDataBinding

class preHomeScreen : AppCompatActivity() {
    lateinit var binding: ActivityPreHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firstName = intent.getStringExtra("firstName").toString()

        val text = "Welcome, ${firstName}"
        binding.welcomeStr.text = text

        binding.goToHomeBtn.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
            startActivity(i)
        }



    }
}