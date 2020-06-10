package com.example.bookseller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookseller.R
import kotlinx.android.synthetic.main.activity_my_books.*

class MyBooksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_books)

        addBookFAB.setOnClickListener {
            startActivity(Intent(this, RegisterBookActivity::class.java))
        }


    }
}