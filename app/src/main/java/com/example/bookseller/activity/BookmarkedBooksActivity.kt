package com.example.bookseller.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_bookmarked_books.*
import kotlinx.android.synthetic.main.activity_home.*

class BookmarkedBooksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //recycler
    private var bookmarksList: ArrayList<Book> = ArrayList()
    private var bookmarksUidList: ArrayList<String> = ArrayList()
    private lateinit var bookmarksAdapter: BookAdapter

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarked_books)

        //user
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)

        //toolbar
        setSupportActionBar(toolbarBookmark)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //navigation drawer
        val toggle = ActionBarDrawerToggle(this, drawerLayoutBookmarks, R.string.open, R.string.close)
        drawerLayoutBookmarks.addDrawerListener(toggle)
        toggle.syncState()
        navigationViewBookmark.setNavigationItemSelectedListener(this)
        navigationViewBookmark.setCheckedItem(R.id.bookmarksActivity)

        //Drawer header
        val source = Source.CACHE
        ConfigureFirebase.getUserDocRef().get(source).addOnSuccessListener { documentSnapshot ->
            val viewHome = navigationViewBookmark.getHeaderView(0)
            val textView = viewHome.findViewById<TextView>(R.id.navDrawerName)
            textView.text = documentSnapshot.get("name").toString()
        }

        getBookmarkedBooks()

        setUpRecyclerView()

//        swipeToDelete()
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                ConfigureFirebase.getUserDocRef()
                    .update("bookmarks", FieldValue.arrayRemove(bookmarksUidList[position]))

                bookmarksUidList.removeAt(position)
                bookmarksList.removeAt(position)
                bookmarksAdapter.notifyItemRemoved(position)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {return false}

        }).attachToRecyclerView(bookmarksRecyclerView)
    }

    // getBookmarked Books
    private fun getBookmarkedBooks(){

        ConfigureFirebase.getUserDocRef()
        .get()
        .addOnSuccessListener { documentSnapshot ->

            val bookmarks: List<String> = documentSnapshot.get("bookmarks") as List<String>

            bookmarksList.clear()
            bookmarksUidList.clear()

            if(bookmarks.isNotEmpty()) {
                for (bookmark in bookmarks) {
                    ConfigureFirebase.getBookColRef()
                        .whereEqualTo(FieldPath.documentId(), bookmark)
                        .addSnapshotListener(this) { querySnapshot, _ ->
                            for (documentSnapshotBook in querySnapshot!!) {
                                bookmarksList.add(documentSnapshotBook.toObject(Book::class.java))
                                bookmarksUidList.add(documentSnapshotBook.id)
                            }
                            bookmarksAdapter.notifyDataSetChanged()
                            bookmarksAdapter.isShimmer = false
                        }
                }
            }
            else {
                bookmarksAdapter.notifyDataSetChanged()
                bookmarksAdapter.isShimmer = false
            }
        }
    }

    // set up recycler view
    private fun setUpRecyclerView() {
        bookmarksAdapter = BookAdapter(bookmarksList)
        bookmarksRecyclerView.layoutManager = LinearLayoutManager(this)
        bookmarksRecyclerView.adapter = bookmarksAdapter
        bookmarksAdapter.notifyDataSetChanged()

        bookmarksAdapter.setOnBookClickListener(object : BookAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val selectedBook = bookmarksList[position]
                val selectedBookUid = bookmarksUidList[position]
                val intent = Intent(this@BookmarkedBooksActivity, ViewBookActivity::class.java)
                intent.putExtra("selectedBook",selectedBook)
                intent.putExtra("selectedBookUid",selectedBookUid)
                startActivity(intent)
            }
        })
    }


    //Navigation Drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Handler().postDelayed({
            when (item.itemId) {
                R.id.homeActivity2 -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.myBooksActivity2 -> startActivity(Intent(this, MyBooksActivity::class.java))
                R.id.logout -> logout()
                R.id.profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.bookmarksActivity -> {}
            }
        },200)
        drawerLayoutBookmarks.closeDrawer(GravityCompat.START)
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
        if(drawerLayoutBookmarks.isDrawerOpen(GravityCompat.START)){
            drawerLayoutBookmarks.closeDrawer(GravityCompat.START)
        }
        else super.onBackPressed()
    }
}