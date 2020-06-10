package com.example.bookseller.model

import java.io.Serializable

data class Book(
    
    val title: String?,
    val description: String?,
    val price: Int?,
    val semester: String?,
    val subject: String?,
    val phone: Int?
    ) : Serializable

//val photo: List<String?>? = null
