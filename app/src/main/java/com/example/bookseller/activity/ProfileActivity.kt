package com.example.bookseller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient

    //edit profile
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //user
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)

        //toolbar
        setSupportActionBar(toolbarProfile)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //navigation drawer
        val toggle = ActionBarDrawerToggle(this,drawerLayoutProfile,R.string.open,R.string.close)
        drawerLayoutProfile.addDrawerListener(toggle)
        toggle.syncState()
        navigationViewProfile.setNavigationItemSelectedListener(this)
        navigationViewProfile.setCheckedItem(R.id.profile)


        val source = Source.CACHE                       //storing data in cache, and getting data from the cache
        ConfigureFirebase.getUserDocRef().get(source).addOnSuccessListener {documentSnapshot ->
            if(documentSnapshot.exists()){
                val name = documentSnapshot.get("name").toString()
                val email = documentSnapshot.get("email").toString()
                val number = documentSnapshot.get("phone").toString()

                nameEditText.setText(name)
                emailEditText.setText(email)
                phoneEditText.setText(number)
            }
        }

//        todo: using coroutine with firebase
//        GlobalScope.launch(Dispatchers.IO) {
//            val documentSnapshot = ConfigureFirebase.getUserDocRef().get().await()
//
//            val name = documentSnapshot.get("name").toString()
//            val email = documentSnapshot.get("email").toString()
//
//            withContext(Dispatchers.Main){
//                nameEditText.setText(name)
//                emailEditText.setText(email)
//            }
//        }

        editSaveProfileTextView.setOnClickListener {
            if(!isEditing) {
                isEditing = true
                editSaveProfileTextView.text = "Save Edits"

                val pos = nameEditText.text.length
                nameEditText.isFocusableInTouchMode = true
                nameEditText.setSelection(pos)
                val pos2 = phoneEditText.text!!.length
                phoneEditText.isFocusableInTouchMode = true
                phoneEditText.setSelection(pos2)

            }
            else {
                isEditing = false
                editSaveProfileTextView.text = "Edit Profile"
                nameEditText.isFocusable = false
                phoneEditText.isFocusable = false

                saveInfo()
            }
        }

    }
    
    //save user info
    fun saveInfo(){
        val updatedName = nameEditText.text.toString()
        val updatedNumber = phoneEditText.text.toString()

        val updateMap = hashMapOf(
            "name" to updatedName,
            "phone" to updatedNumber
        )

        ConfigureFirebase.getUserDocRef()
            .update(updateMap as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Info Updated", Toast.LENGTH_SHORT).show()
            }
    }

    // Navigation Drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Handler().postDelayed({
            when (item.itemId){
                R.id.homeActivity2 -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.myBooksActivity2 -> startActivity(Intent(this, MyBooksActivity::class.java))
                R.id.logout -> logout()
                R.id.profile -> {}
                R.id.bookmarksActivity -> startActivity(Intent(this, BookmarkedBooksActivity::class.java))
            }
        },200)
        drawerLayoutProfile.closeDrawer(GravityCompat.START)
        return true
    }

    // logout
    private fun logout(){
        mAuth.signOut()
        signInClient.signOut()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onBackPressed() {
        if(drawerLayoutProfile.isDrawerOpen(GravityCompat.START)){
            drawerLayoutProfile.closeDrawer(GravityCompat.START)
        }
        else super.onBackPressed()
    }

}