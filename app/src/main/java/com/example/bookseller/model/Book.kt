package com.example.bookseller.model

import java.io.Serializable

data class Book(
    val title: String?,
    val description: String?,
    val price: String?,
    val semester: String?,
    val subject: String?,
    val phone: String?
    ) : Serializable {

    private var photo: List<String>? = null

    // empty no-args constructor for firebase
    constructor() : this("","","","","","")

    fun setPhoto(photo: List<String>?) {
        this.photo = photo
    }

    fun getPhoto(): List<String>{
        return this.photo!!
    }
}

