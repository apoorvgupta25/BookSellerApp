package com.example.bookseller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookseller.R
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
}