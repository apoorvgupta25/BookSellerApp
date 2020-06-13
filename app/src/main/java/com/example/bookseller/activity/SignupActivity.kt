package com.example.bookseller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.android.synthetic.main.activity_main.*

class SignupActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

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
                }
                else{
                    val errorException: String
                    try{
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException){
                        errorException = "Enter a stronger password!"
                    } catch (e: FirebaseAuthInvalidCredentialsException){
                        errorException = "Please, type a valid email"
                    } catch (e: FirebaseAuthUserCollisionException){
                        errorException = "This account has already been registered"
                    } catch(e: Exception ){
                        errorException = "when registering user: " + e.message
                        e.printStackTrace()
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

        val userId = ConfigureFirebase.getUserId()
        val userEmail = ConfigureFirebase.getUserEmail()

        val email = userEmail?.trim { it <= ' ' }
        val l = email?.indexOf("@")
        val username = l?.let { email.substring(0, it) }

        val userData = hashMapOf(
            "email" to userEmail,
            "name" to username,
            "bookmarks" to arrayListOf<String>()
        )

        //Collection + Add -> Generate random collection Uid - useful for single book collection
        if (userId != null) {
            ConfigureFirebase.getUserDocRef()
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (!documentSnapshot.exists()) {
                        ConfigureFirebase.getUserDocRef().set(userData)
                    }
                }
        }
    }
}