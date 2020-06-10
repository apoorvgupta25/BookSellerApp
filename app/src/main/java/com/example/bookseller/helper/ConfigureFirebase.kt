package com.example.bookseller.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference

class ConfigureFirebase {

    companion object {

        //static var
        private var authenticationInstance: FirebaseAuth? = null
        private var storageReference: StorageReference? = null

        //static methods
        fun getFirebaseAuth(): FirebaseAuth? {
            if (authenticationInstance == null) {
                authenticationInstance = Firebase.auth
            }
            return authenticationInstance
        }

        //Email
        fun getUserEmail(): String? {
            return getFirebaseAuth()?.currentUser?.email
        }

        //User Uid
        fun getUserId(): String? {
            return getFirebaseAuth()?.currentUser?.uid
        }

        //User db reference
        fun getUserDbRef(userId: String): DocumentReference {
            return FirebaseFirestore.getInstance().collection("user").document(userId)
        }


    }
}