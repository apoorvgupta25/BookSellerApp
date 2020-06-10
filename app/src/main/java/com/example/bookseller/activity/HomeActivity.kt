package com.example.bookseller.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookseller.R
import com.example.bookseller.adapter.BookAdapter
import com.example.bookseller.helper.ConfigureFirebase
import com.example.bookseller.model.Book
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    //recycler
    private var booksList: ArrayList<Book> = ArrayList()
    private lateinit var bookAdapter: BookAdapter

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance().collection("user")

    private var dialogHomeActivity: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        saveUserInDB()


        setUpRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        dialogHomeActivity = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Getting Books")
            .setCancelable(false)
            .build()
        dialogHomeActivity!!.show()



        db.addSnapshotListener(this) { querySnapshot, e ->
            if (e != null) return@addSnapshotListener
            booksList.clear()
            for (documentSnapshot in querySnapshot!!) {
                db.document(documentSnapshot.id)
                    .collection("books")
                    .addSnapshotListener(this) { querySnapshotBook, eBook ->
                        if (eBook != null) return@addSnapshotListener

//                        booksList.clear()
                        for (documentSnapshotBook in querySnapshotBook!!) {
                            booksList.add(documentSnapshotBook.toObject(Book::class.java))
                        }
                        bookAdapter.notifyDataSetChanged()
                    }
            }
            bookAdapter.notifyDataSetChanged()
            dialogHomeActivity!!.dismiss()
        }


    }

    // Save new User
    private fun saveUserInDB() {
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)

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


    private fun setUpRecyclerView() {
        bookAdapter = BookAdapter(booksList)
        bookRecyclerView.layoutManager = LinearLayoutManager(this)
        bookRecyclerView.adapter = bookAdapter
        bookAdapter.notifyDataSetChanged()

//        bookAdapter.setOnBookClickListener(object : BookAdapter.OnItemClickListener {
//            override fun onItemClick(position: Int) {
//                Toast.makeText(this@HomeActivity, "Clicked ${booksList.get(position).title}", Toast.LENGTH_SHORT).show()
//                TODO("Move to new Activity Remaining")
//            }
//        })
    }

    //Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                mAuth.signOut()
                signInClient.signOut()
                finish()
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.myBooks -> {
                startActivity(Intent(this@HomeActivity, MyBooksActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // saves state
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}