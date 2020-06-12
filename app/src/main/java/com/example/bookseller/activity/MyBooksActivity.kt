package com.example.bookseller.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookseller.R
import com.example.bookseller.adapter.BookAdapter
import com.example.bookseller.helper.ConfigureFirebase
import com.example.bookseller.model.Book
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_books.*
import kotlinx.android.synthetic.main.activity_my_books.navigationViewMy
import kotlinx.android.synthetic.main.activity_profile.*

class MyBooksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //recycler
    private var myBooksList: ArrayList<Book> = ArrayList()
    private lateinit var myBooksAdapter: BookAdapter

    private var dialog: AlertDialog? = null

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_books)

//        document + get() -> documentSnapshot
//        collection + get -> querySnapshot

        //initialize user
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)

        //toolbar
        setSupportActionBar(toolbarMy)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

        //navigation drawer
        val toggle = ActionBarDrawerToggle(this,drawerLayoutMy,R.string.open,R.string.close);
        drawerLayoutMy.addDrawerListener(toggle);
        toggle.syncState();
        navigationViewMy.setNavigationItemSelectedListener(this);
        navigationViewMy.setCheckedItem(R.id.myBooksActivity2)




        setUpRecyclerView()

    }

    override fun onStart() {
        super.onStart()

//        dialog = SpotsDialog.Builder()
//            .setContext(this)
//            .setMessage("Getting your Books")
//            .setCancelable(false)
//            .build()
//        dialog!!.show()

//        ConfigureFirebase.getUserDbRef(ConfigureFirebase.getUserId()!!).collection("books")       //Sub-collection Query
        ConfigureFirebase.getBookDbRef().whereEqualTo("userId",ConfigureFirebase.getUserId())
            .addSnapshotListener(this){querySnapshot,e->
                if(e != null){
                    return@addSnapshotListener;
                }
                myBooksList.clear()
                for (documentSnapshot in querySnapshot!!){
                    myBooksList.add(documentSnapshot.toObject(Book::class.java))
                }
                myBooksAdapter.isShimmer = false
                myBooksAdapter.notifyDataSetChanged()
//                dialog!!.dismiss()
            }
    }

    // set up recycler view
    private fun setUpRecyclerView() {
        myBooksAdapter = BookAdapter(myBooksList)
        myBooksRecyclerView.layoutManager = LinearLayoutManager(this)
        myBooksRecyclerView.adapter = myBooksAdapter
        myBooksAdapter.notifyDataSetChanged()

    }

    // Navigation Drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.homeActivity2 -> {
                startActivity(Intent(this, HomeActivity::class.java))
                true
            }
            R.id.myBooksActivity2 ->{
                drawerLayoutMy.closeDrawers()
                true
            }
            R.id.logout -> {
                logout()
                true
            }
            R.id.profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> false
        }
    }

    // logout
    private fun logout(){
        mAuth.signOut()
        signInClient.signOut()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onBackPressed() {
        if(drawerLayoutMy.isDrawerOpen(GravityCompat.START)){
            drawerLayoutMy.closeDrawer(GravityCompat.START)
        }
        else super.onBackPressed()

    }
}