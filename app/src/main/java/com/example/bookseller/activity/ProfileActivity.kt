package com.example.bookseller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.bookseller.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_bookmarked_books.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_books.*
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient

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
        val toggle = ActionBarDrawerToggle(this,drawerLayoutProfile,R.string.open,R.string.close);
        drawerLayoutProfile.addDrawerListener(toggle);
        toggle.syncState();
        navigationViewProfile.setNavigationItemSelectedListener(this);
        navigationViewProfile.setCheckedItem(R.id.profile)

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