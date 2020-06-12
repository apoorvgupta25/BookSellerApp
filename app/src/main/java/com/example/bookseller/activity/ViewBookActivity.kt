package com.example.bookseller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.example.bookseller.model.Book
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_view_book.*

class ViewBookActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_book)


        val selectedBook = intent.getSerializableExtra("selectedBook") as? Book

        if(selectedBook != null){
            bookTitleTextView.text = selectedBook.title
            bookDescriptionTextView.text = selectedBook.description
            bookPriceTextView.text = selectedBook.price
            bookSemesterTextView.text = selectedBook.semester
            bookSubjectTextView.text = selectedBook.subject

            //Carousel
            val imageListener = ImageListener{ position, imageView ->
                val urlString = selectedBook.getPhoto()[position]
                Picasso.get().load(urlString).into(imageView)
            }

            carouselView.pageCount = selectedBook.getPhoto().size
            carouselView.setImageListener(imageListener)
        }
        //open phone
        callButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",selectedBook!!.phone, null)))
        }

    }

    //reportBook
    private fun reportBook(){

        ConfigureFirebase.getBookDbRef()
            .document(intent.getStringExtra("selectedBookUid")!!)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getBoolean("reported")!!){
                        Toast.makeText(this, "This Book has been already Reported", Toast.LENGTH_SHORT).show()
                    } else {
                        ConfigureFirebase.getBookDbRef()
                            .document(intent.getStringExtra("selectedBookUid")!!)
                            .update("reported", true)
                        Toast.makeText(this, "Book Reported", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    // Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_book_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.reportBook)
            reportBook()
        return super.onOptionsItemSelected(item)
    }
}