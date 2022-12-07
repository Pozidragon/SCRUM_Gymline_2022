package com.example.gymline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.gymline.databinding.ActivityLoginBinding
import com.example.gymline.databinding.ActivityPasswordResetBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PasswordReset : AppCompatActivity() {
    lateinit var binding: ActivityPasswordResetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button2.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                val emailAddress = binding.editTextTextPersonName.text.toString()

                Firebase.auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(baseContext, "Confirmation list was sent. Check your mailbox.",
                                Toast.LENGTH_LONG).show()

                            finish()
                        }
                        else{
                            Toast.makeText(baseContext, "Email address isn't found",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }

        }


    }
}