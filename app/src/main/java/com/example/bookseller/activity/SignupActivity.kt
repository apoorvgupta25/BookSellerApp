package com.example.bookseller.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class SignupActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = ConfigureFirebase.getFirebaseAuth()!!
    }

    fun moveToSignUp(v: View){
        startActivity(Intent(this, SignupActivity::class.java))
    }

    fun signupUser(v: View){
        if (emailEditText?.text.toString() == "" || passwordEditText?.text.toString() == "") {
            Toast.makeText(applicationContext, "Email or Password is empty", Toast.LENGTH_SHORT)
                .show()
        } else {
            mAuth.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    login()

                    //ConfigureFirebase.getDatabaseReference()?.child("users")?.child(task.result?.user?.uid.toString())?.child("email")?.setValue(emailEditText.text.toString())
                }
                else{
                    var errorException = ""
                    try{
                        throw task.exception!!;
                    } catch (e: FirebaseAuthWeakPasswordException){
                        errorException = "Enter a stronger password!";
                    } catch (e: FirebaseAuthInvalidCredentialsException){
                        errorException = "Please, type a valid email";
                    } catch (e: FirebaseAuthUserCollisionException){
                        errorException = "This account has already been registered";
                    } catch(e: Exception ){
                        errorException = "when registering user: " + e.message;
                        e.printStackTrace();
                    }

                    Toast.makeText(this@SignupActivity, "Error: $errorException", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun login() {
        startActivity(Intent(this, HomeActivity::class.java))
        saveUserInDB()
    }

    // Save new User
    private fun saveUserInDB() {
//        mAuth = Firebase.auth
//
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        signInClient = GoogleSignIn.getClient(this, gso)

        val userId = ConfigureFirebase.getUserId()
        val userEmail = ConfigureFirebase.getUserEmail()

        val email = userEmail?.trim { it <= ' ' }
        val l = email?.indexOf("@")
        val username = l?.let { email.substring(0, it) }

        val userData = hashMapOf(
            "email" to userEmail,
            "name" to username
        )

        //Collection + Add -> Generate random collection Uid - useful for single book collection
        if (userId != null) {
            ConfigureFirebase.getUserDbRef(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (!documentSnapshot.exists()) {
                        ConfigureFirebase.getUserDbRef(userId).set(userData)
                    }
                }
        }
    }
}