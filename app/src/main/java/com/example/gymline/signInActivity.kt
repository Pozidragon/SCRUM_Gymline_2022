package com.example.gymline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.gymline.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlin.system.exitProcess


class signInActivity : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignInBinding

    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceVersions: DatabaseReference

    private lateinit var sEmail: String
    private lateinit var sPassword: String
    private lateinit var sFirstName: String
    private lateinit var sLastName: String

    private lateinit var versions: Version


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        versionControl()
        auth = Firebase.auth
        databaseReference =
            FirebaseDatabase.getInstance("https://gymline-33603-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users")
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }

            } catch (e: ApiException) {

            }
        }
        binding.signInGoogle.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else signInWithGoogle()

        }

        checkAuthState()


        binding.registerBtn.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                sEmail = binding.regEmail.text.toString()
                sPassword = binding.regPassword.text.toString()
                sFirstName = binding.regFirstName.text.toString()
                sLastName = binding.regLastName.text.toString()
                if (sEmail != "" && sPassword != "" && sFirstName != "" && sLastName != "") {
                    auth.createUserWithEmailAndPassword(sEmail, sPassword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("TAG", "createUserWithEmail:success")
                                val user = auth.currentUser
                                if (auth.currentUser != null) {
                                    val i = Intent(this, RegAddPersonalData::class.java)
                                    i.putExtra("firstName", sFirstName )
                                    i.putExtra("lastName", sLastName)
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(i)
                                    finish()
                                    startActivity(i)
                                }
                            } else {
                                Log.w("TAG", "createUserWithEmail:failure", task.exception)
                                if (sPassword.length < 6){
                                    Toast.makeText(
                                        baseContext, "Password must be at least 6 chars long",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else{
                                    Toast.makeText(
                                        baseContext, "Incorrect email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                } else {
                    Toast.makeText(
                        baseContext, "Fill up all fields.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }



        }
        binding.toLoginPage.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }

    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)

    }

    private fun signInWithGoogle() {
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("MyLog", "Google Sign In done")
                checkAuthState()
            } else {
                Log.d("MyLog", "Error")
            }
        }
    }

    private fun checkAuthState() {
        if (auth.currentUser != null) {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
            startActivity(i)
        }
    }

    private fun versionControl() {
        val version1 = resources.getString(R.string.version)
        databaseReferenceVersions = FirebaseDatabase.getInstance("https://gymline-33603-default-rtdb.europe-west1.firebasedatabase.app").getReference("Version")
        databaseReferenceVersions.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                versions = snapshot.getValue(Version::class.java)!!
                if(versions.version.toString() != version1){
                    Toast.makeText(
                        baseContext, "New version is available",
                        Toast.LENGTH_LONG
                    ).show()
                    this@signInActivity.finish()
                    exitProcess(0)
                }


            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    baseContext, "Error",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

}