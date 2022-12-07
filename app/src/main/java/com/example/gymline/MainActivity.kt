package com.example.gymline

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.gymline.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog
    private lateinit var user: User
    private lateinit var uid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        uid = auth.currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance("https://gymline-33603-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")

        binding.btmMenu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.profile_item -> {
                    Toast.makeText(this@MainActivity,
                        "Current page: Profile", Toast.LENGTH_SHORT).show()
                }
                R.id.logout -> {
                    if (!InternetConn.internetIsConnected()){
                        Toast.makeText(
                            baseContext, "No internet connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        auth.signOut()
                        val i = Intent(this, signInActivity::class.java)
                        startActivity(i)
                        finish()
                    }

                }
                R.id.courses -> {
                    val i = Intent(this, CoursesActivity::class.java)
                    startActivity(i)
                    finish()
                }
            }
            true
        }

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance("https://gymline-33603-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        if (!InternetConn.internetIsConnected()){
            Toast.makeText(
                baseContext, "No internet connection",
                Toast.LENGTH_SHORT
            ).show()
        }
        else{
            if(uid.isNotEmpty()){
                getUserData()
        }

        }

        binding.saveProfileDataBtn.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                if(binding.editBirthDate.text.toString().trim() != "" && binding.editWeight.text.toString().trim() != "" && binding.editHeight.text.toString().trim() != ""
                    && binding.editFirstName.text.toString().trim() != "" && binding.editLastName.text.toString().trim()  != ""){

                    val firstName = binding.editFirstName.text.toString().trim().toCharArray()[0].uppercase() + binding.editFirstName.text.toString().trim().drop(1)
                    val lastName = binding.editLastName.text.toString().trim().toCharArray()[0].uppercase() + binding.editLastName.text.toString().trim().drop(1)

                    val birthdate = binding.editBirthDate.text.toString().trim()
                    val weight = binding.editWeight.text.toString().trim()
                    val height = binding.editHeight.text.toString().trim()
                    var gender = "";
                    if(binding.regGender.isChecked){gender = "Female"}
                    else{gender = "Male"}

                    val user = User(firstName, lastName, gender, birthdate, weight, height )
                    if(weight.toInt() >= 35){
                        if(height.toInt() >= 110){
                            if(uid != null){
                                databaseReference.child(uid).setValue(user).addOnCompleteListener{
                                    if(it.isSuccessful){
                                        val i = Intent(this, preHomeScreen::class.java)
                                        i.putExtra("firstName", firstName)
                                        startActivity(i)

                                        if(this::imageUri.isInitialized) uploadProfilePic()
                                    }
                                    else{

                                        Toast.makeText(this@MainActivity,
                                            "Failed to update profile", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }} else{Toast.makeText(this@MainActivity,
                            "Sorry, min height is 110cm", Toast.LENGTH_SHORT).show()}}

                    else{
                        Toast.makeText(this@MainActivity,
                            "Sorry, min weight is 35kg", Toast.LENGTH_SHORT).show()
                    }

                }
                else{
                    Toast.makeText(this@MainActivity,
                        "Fill up all fields", Toast.LENGTH_SHORT).show()
                }

            }

        }
        binding.btnDate.setOnClickListener {
            if (!binding.datePicker.isVisible) {
                val datePicker = binding.datePicker
                binding.apply {
                    datePicker.isVisible = true
                    cm.isVisible = false
                    kg.isVisible = false
                    saveProfileDataBtn.isVisible = false
                    regGender.isVisible = false
                    textView7.isVisible = false
                }
                val today = Calendar.getInstance()
                today.add(Calendar.YEAR, -3) //Goes 10 Year Back in time ^^
                val upperLimit: Long = today.timeInMillis
                datePicker.maxDate = upperLimit;
                datePicker.init(
                    today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)

                ) { view, year, month, day ->
                    val month = month + 1
                    val msg = "$day/$month/$year"
                    binding.editBirthDate.setText(msg)
                }
            } else {
                binding.apply {
                    datePicker.isVisible = false
                    cm.isVisible = true
                    kg.isVisible = true
                    saveProfileDataBtn.isVisible = true
                    regGender.isVisible = true
                    textView7.isVisible = true
                }

            }


        }

        binding.circleImageView.setOnClickListener{

            selectImage()

        }
        binding.editHeight.filters = arrayOf<InputFilter>(MinMaxFilter(1, 235))
        binding.editWeight.filters = arrayOf<InputFilter>(MinMaxFilter(1, 250))
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent, 100)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK){
            imageUri = data?.data!!

            binding.circleImageView.setImageURI(imageUri)
        }
    }

    private fun getUserData(){
        databaseReference.child(uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue(User::class.java) != null){
                    showProgressBar()
                    user = snapshot.getValue(User::class.java)!!
                    val displayName = user.firstName + " " + user.lastName
                    binding.displayName.text = displayName
                    binding.editFirstName.setText(user.firstName)
                    binding.editLastName.setText(user.lastName)
                    binding.editWeight.setText(user.weight)
                    binding.editHeight.setText(user.height)
                    binding.editBirthDate.setText(user.birthdate)
                    if (user.gender == "Female"){
                        binding.regGender.isChecked = true
                    }
                    getUserProfilePic()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressBar()
            }

        })
    }

    private fun uploadProfilePic(){

        storageReference = FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
        storageReference.putFile(imageUri).addOnSuccessListener {

        }.addOnFailureListener{

            Toast.makeText(this@MainActivity,
                "Failed to upload the image", Toast.LENGTH_SHORT).show()
        }


    }

    private fun getUserProfilePic(){
        storageReference = FirebaseStorage.getInstance().reference.child("Users/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {

            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            binding.circleImageView.setImageBitmap(bitmap)

            hideProgressBar()
        }.addOnFailureListener{
            hideProgressBar()
        }
    }

    private fun showProgressBar(){
        dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        if (!this@MainActivity.isFinishing) {
            dialog.show()
        }

    }

    private fun hideProgressBar(){
        dialog.dismiss()
    }


}