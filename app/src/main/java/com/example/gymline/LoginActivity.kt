package com.example.gymline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gymline.databinding.ActivityLoginBinding
import com.example.gymline.databinding.ActivityPasswordResetBinding
import com.example.gymline.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    lateinit var launcher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null){
                    firebaseAuthWithGoogle(account.idToken!!)
                }

            } catch(e: ApiException){

            }
        }

        binding.loginBtn.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                if(binding.emailLogin.text.toString() != "" && binding.passwordLogin.text.toString() != ""){
                    auth.signInWithEmailAndPassword(binding.emailLogin.text.toString(), binding.passwordLogin.text.toString())
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "signInWithEmail:success")
                                val user = auth.currentUser
                                checkAuthState()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.(Check email adress or password)",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                else{
                    Toast.makeText(baseContext, "Fill up all fields",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.signInGoogle3.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                signInWithGoogle()
            }
        }

        binding.backSignUp.setOnClickListener {
            finish()
        }

        binding.FrgtPassBtn.setOnClickListener {
            if (!InternetConn.internetIsConnected()){
                Toast.makeText(
                    baseContext, "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                val i = Intent(this, PasswordReset::class.java)
                startActivity(i)
            }

        }
    }

    private fun checkAuthState(){
        if(auth.currentUser != null){
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
            startActivity(i)
        }
    }
    private fun signInWithGoogle(){
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }
    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)

    }
    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                Log.d("MyLog", "Google Sign In done")
                checkAuthState()

            }
            else{
                Log.d("MyLog", "Error")
            }
        }
    }
}