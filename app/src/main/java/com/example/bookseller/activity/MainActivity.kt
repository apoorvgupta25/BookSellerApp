package com.example.bookseller.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    val REQUEST_CODE_SIGN_IN = 1
    val TAG = "MainActivity"

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = ConfigureFirebase.getFirebaseAuth()!!

        // Google Sign in
        googleSigninButton.setOnClickListener {
            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val signInClient = GoogleSignIn.getClient(this, gso)

            val intent = signInClient.signInIntent
            startActivityForResult(intent, REQUEST_CODE_SIGN_IN)

            // In One Line todo: Example of also
//            signInClient.signInIntent.also {
//                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
//            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.i(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "signInWithCredential:success")
                    login()
                } else {
                    Log.i(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    fun loginUser(v: View){
        if (emailEditText?.text.toString() == "" || passwordEditText?.text.toString() == "") {
            Toast.makeText(applicationContext, "Email or Password is empty", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.signInWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                    login()
                else
                    Toast.makeText(applicationContext, "Unable To Login ", Toast.LENGTH_SHORT).show()
            }
        }
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

                    Toast.makeText(this@MainActivity, "Error: $errorException", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun moveToSignUp(v: View){
        startActivity(Intent(this, SignupActivity::class.java))
    }

    private fun login() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null){
            login()
        }
    }

}