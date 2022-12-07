package com.example.gymline

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.gymline.databinding.ActivityRegAddPersonalDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar;

class RegAddPersonalData : AppCompatActivity() {
    lateinit var binding: ActivityRegAddPersonalDataBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegAddPersonalDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://gymline-33603-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        binding.saveBtn.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                val firstName = intent.getStringExtra("firstName").toString().trim().toCharArray()[0].uppercase() + intent.getStringExtra("firstName").toString().trim().drop(1)
                val lastName = intent.getStringExtra("lastName").toString().trim().toCharArray()[0].uppercase() + intent.getStringExtra("lastName").toString().trim().drop(1)

                val birthdate = binding.regBirthDate.text.toString().trim()
                val weight = binding.regWeight.text.toString().trim()
                val height = binding.regHeight.text.toString().trim()
                var gender = "";
                if(binding.regGender.isChecked){gender = "Female"}
                else{gender = "Male"}

                if(birthdate != "" && weight != "" && height != "" && gender != ""){
                    if(weight.toInt() >= 35){
                        if(height.toInt() >= 110){
                            val user = User(firstName, lastName, gender, birthdate, weight, height )
                            if(uid != null){
                                databaseReference.child(uid).setValue(user).addOnCompleteListener{
                                    if(it.isSuccessful){
                                        val i = Intent(this, preHomeScreen::class.java)
                                        i.putExtra("firstName", firstName)
                                        startActivity(i)
                                    }
                                    else{
                                        Toast.makeText(this@RegAddPersonalData,
                                            "Failed to update profile", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        else{
                            Toast.makeText(this@RegAddPersonalData,
                                "Sorry, min height is 110cm", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this@RegAddPersonalData,
                            "Sorry, min weight is 35kg", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this@RegAddPersonalData,
                        "Fill up all fields", Toast.LENGTH_SHORT).show()
                }
            }



        }
        binding.btnDate.setOnClickListener {
            if (!binding.datePicker.isVisible){
                val datePicker = binding.datePicker
                datePicker.isVisible = true
                binding.apply {
                    textView.isVisible = false
                    textView4.isVisible = false
                    imageView.isVisible = false
                    regBirthDate.isVisible = true
                }
                val today = Calendar.getInstance()
                today.add(Calendar.YEAR, -3) //Goes 10 Year Back in time ^^
                val upperLimit: Long = today.timeInMillis
                datePicker.maxDate = upperLimit;
                datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)

                ) { view, year, month, day ->
                    val month = month + 1
                    val msg = "$day/$month/$year"
                    binding.regBirthDate.setText(msg)
                }
            }
            else{
                binding.apply {
                    datePicker.isVisible = false
                    textView.isVisible = true
                    textView4.isVisible = true
                    imageView.isVisible = true
                    regBirthDate.isVisible = true
                }

            }


        }

        binding.regHeight.filters = arrayOf<InputFilter>(MinMaxFilter(1, 235))
        binding.regWeight.filters = arrayOf<InputFilter>(MinMaxFilter(1, 250))
    }

}