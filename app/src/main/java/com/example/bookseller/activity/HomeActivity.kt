package com.example.bookseller.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.toolbarHome

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //recycler
    private var booksList: ArrayList<Book> = ArrayList()
    private lateinit var bookAdapter: BookAdapter

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance().collection("user")

    private var dialogHomeActivity: AlertDialog? = null

    private var selectedSemester = "Semester"

    private var bookUidList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //toolbar
        setSupportActionBar(toolbarHome)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

        //navigation drawer
        val toggle = ActionBarDrawerToggle(this,drawerLayoutHome,R.string.open,R.string.close);
        drawerLayoutHome.addDrawerListener(toggle);
        toggle.syncState();
        navigationViewHome.setNavigationItemSelectedListener(this);
        navigationViewHome.setCheckedItem(R.id.homeActivity2)

//        saveUserInDB()
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        signInClient = GoogleSignIn.getClient(this, gso)

        addBookFAB.setOnClickListener {
            startActivity(Intent(this, RegisterBookActivity::class.java))
        }

        setUpRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            getAllBooks()
        },0)
        setUpRecyclerView()
    }

    //filter semester wise
    fun filterSemesterwise(view: View){
        val dialogSemester = AlertDialog.Builder(this)
        dialogSemester.setTitle("Select Semester")

        //spinner
        val spinnerView = layoutInflater.inflate(R.layout.dialog_spinner, null)

        val semesterHomeSpinner = spinnerView.findViewById<Spinner>(R.id.spinnerFilter)
        val semester = resources.getStringArray(R.array.semester_home)
        val semesterAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, semester)

        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        semesterHomeSpinner.adapter = semesterAdapter

        dialogSemester.setView(spinnerView)

        dialogSemester
            .setPositiveButton("OK"){_, _ ->
                selectedSemester = semesterHomeSpinner.selectedItem.toString()
                if(selectedSemester == "All") getAllBooks()
                else getBooksBySemester(selectedSemester)
            }
            .setNegativeButton("Cancel"){_,_ -> }

        dialogHomeActivity = dialogSemester.create()
        dialogHomeActivity!!.show()
    }

    // filter semester wise
    fun filterSubjectwise(view: View){
        if(selectedSemester != "Semester"){
            val dialogSubject = AlertDialog.Builder(this)
            dialogSubject.setTitle("Select Subject")

            //spinner
            val spinnerView = layoutInflater.inflate(R.layout.dialog_spinner, null)

            val subjectHomeSpinner = spinnerView.findViewById<Spinner>(R.id.spinnerFilter)
            var subject: Array<String> = resources.getStringArray(R.array.sub_sem_1)
            when(selectedSemester){
                "1" -> subject = resources.getStringArray(R.array.sub_sem_1_home)
                "2" -> subject = resources.getStringArray(R.array.sub_sem_2_home)
                "3" -> subject = resources.getStringArray(R.array.sub_sem_3_home)
                "4" -> subject = resources.getStringArray(R.array.sub_sem_4_home)
                "5" -> subject = resources.getStringArray(R.array.sub_sem_5_home)
                "6" -> subject = resources.getStringArray(R.array.sub_sem_6_home)
                "7" -> subject = resources.getStringArray(R.array.sub_sem_7_home)
                "8" -> subject = resources.getStringArray(R.array.sub_sem_8_home)
            }
            val subjectAdapter: ArrayAdapter<String> = ArrayAdapter(this@HomeActivity, android.R.layout.simple_spinner_item, subject)

            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subjectHomeSpinner.adapter = subjectAdapter

            dialogSubject.setView(spinnerView)

            dialogSubject
                .setPositiveButton("OK"){_, _ ->
                    val selectedSubject = subjectHomeSpinner.selectedItem.toString()
                    if(selectedSubject == "All") getBooksBySemester(selectedSemester)
                    else getBooksBySubject(selectedSubject, selectedSemester)
                }
                .setNegativeButton("Cancel"){_,_ -> }

            dialogHomeActivity = dialogSubject.create()
            dialogHomeActivity!!.show()
        }
        else Toast.makeText(this@HomeActivity, "Select Semester First", Toast.LENGTH_SHORT).show()
    }

    // get all books
    private fun getAllBooks(){
//        dialogHomeActivity = SpotsDialog.Builder()
//            .setContext(this)
//            .setMessage("Getting Books")
//            .setCancelable(false)
//            .build()
//        dialogHomeActivity!!.show()


        // Sub-Collection Query
        /*db.addSnapshotListener(this) { querySnapshot, e ->
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
         */

        //Top-Level Collection Query
        ConfigureFirebase.getBookDbRef()
            .addSnapshotListener(this){querySnapshot,e->
                if(e != null){
                    return@addSnapshotListener;
                }
                booksList.clear()
                bookUidList.clear()
                for (documentSnapshot in querySnapshot!!){
                    booksList.add(documentSnapshot.toObject(Book::class.java))
                    bookUidList.add(documentSnapshot.id)
                }
                bookAdapter.notifyDataSetChanged()
                bookAdapter.isShimmer = false
//                dialogHomeActivity!!.dismiss()
            }
    }

    // get books by semester
    private fun getBooksBySemester(selectedSemester: String){
//        dialogHomeActivity = SpotsDialog.Builder()
//            .setContext(this)
//            .setMessage("Getting Books By Semster")
//            .setCancelable(false)
//            .build()
//        dialogHomeActivity!!.show()

        ConfigureFirebase.getBookDbRef()
            .whereEqualTo("semester",selectedSemester)
            .addSnapshotListener(this){querySnapshot,e->
                if(e != null){
                    return@addSnapshotListener;
                }
                booksList.clear()
                bookUidList.clear()
                for (documentSnapshot in querySnapshot!!){
                    booksList.add(documentSnapshot.toObject(Book::class.java))
                    bookUidList.add(documentSnapshot.id)
                }
                bookAdapter.notifyDataSetChanged()
                bookAdapter.isShimmer = false
//                dialogHomeActivity!!.dismiss()
            }
    }

    // get book by subject
    private fun getBooksBySubject(selectedSubject: String, selectedSemester: String){
//        dialogHomeActivity = SpotsDialog.Builder()
//            .setContext(this)
//            .setMessage("Getting Books By Subject")
//            .setCancelable(false)
//            .build()
//        dialogHomeActivity!!.show()

        ConfigureFirebase.getBookDbRef()
            .whereEqualTo("subject",selectedSubject)
            .addSnapshotListener(this){querySnapshot,e->
                if(e != null){
                    Log.i("query error 2",e.toString())
                    return@addSnapshotListener;
                }
                booksList.clear()
                bookUidList.clear()
                for (documentSnapshot in querySnapshot!!){
                    val book = documentSnapshot.toObject(Book::class.java)
                    Log.i("query",book.toString())
                    booksList.add(documentSnapshot.toObject(Book::class.java))
                    bookUidList.add(documentSnapshot.id)
                }
                bookAdapter.notifyDataSetChanged()
                bookAdapter.isShimmer = false
//                dialogHomeActivity!!.dismiss()
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

    // set up recycler view
    private fun setUpRecyclerView() {
        bookAdapter = BookAdapter(booksList)
        bookRecyclerView.layoutManager = LinearLayoutManager(this)
        bookRecyclerView.adapter = bookAdapter
        bookAdapter.notifyDataSetChanged()

        bookAdapter.setOnBookClickListener(object : BookAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val selectedBook = booksList[position]
                val selectedBookUid = bookUidList[position]
                val intent = Intent(this@HomeActivity, ViewBookActivity::class.java)
                intent.putExtra("selectedBook",selectedBook)
                intent.putExtra("selectedBookUid",selectedBookUid)
                startActivity(intent)
            }
        })
    }

    // Navigation Drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.homeActivity2 -> {
                drawerLayoutHome.closeDrawers()
                true
            }
            R.id.myBooksActivity2 ->{
                startActivity(Intent(this, MyBooksActivity::class.java))
                drawerLayoutHome.closeDrawer(GravityCompat.START)
                true
            }
            R.id.logout -> {
                logout()
                true
            }
            R.id.profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                drawerLayoutHome.closeDrawers()
                true
            }
            else -> false
        }
    }

    private fun logout(){
        mAuth.signOut()
        signInClient.signOut()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    // saves state
    override fun onBackPressed() {

        if(drawerLayoutHome.isDrawerOpen(GravityCompat.START)){
            drawerLayoutHome.closeDrawer(GravityCompat.START)
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}