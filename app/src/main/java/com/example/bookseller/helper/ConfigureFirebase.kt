package com.example.bookseller.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference

class ConfigureFirebase {

    companion object{

        //static var
        private var authenticationInstance: FirebaseAuth? = null
        private var storageReference : StorageReference? = null

        //static methods
        fun getFirebaseAuth(): FirebaseAuth? {
            if(authenticationInstance == null) authenticationInstance = Firebase.auth
            return authenticationInstance
        }

        fun getUserEmail(): String {
            return getFirebaseAuth()!!.currentUser!!.email!!
        }

        fun getUserId(): String {
            return getFirebaseAuth()!!.currentUser!!.uid
        }



    }
}