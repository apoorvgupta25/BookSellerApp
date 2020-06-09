package com.example.bookseller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = ConfigureFirebase.getFirebaseAuth()

    }
    
    fun loginUser(v: View){
        if (emailEditText?.text.toString() == "" || passwordEditText?.text.toString() == "") {
            Toast.makeText(applicationContext, "Email or Password is empty", Toast.LENGTH_SHORT).show()
        } else {
            mAuth?.signInWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                    login()
                else
                    Toast.makeText(applicationContext, "Unable To Login ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveToSignUp(v: View){
        startActivity(Intent(this, SignupActivity::class.java))
    }

    fun signupUser(v: View){
        if (emailEditText?.text.toString() == "" || passwordEditText?.text.toString() == "") {
            Toast.makeText(applicationContext, "Email or Password is empty", Toast.LENGTH_SHORT)
                .show()
        } else {
            mAuth?.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())?.addOnCompleteListener(this) { task ->
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



    private fun login() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

}