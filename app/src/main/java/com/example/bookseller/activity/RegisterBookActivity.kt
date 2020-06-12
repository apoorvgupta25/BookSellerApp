package com.example.bookseller.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookseller.R
import com.example.bookseller.helper.ConfigureFirebase
import com.example.bookseller.helper.Permissions
import com.example.bookseller.model.Book
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register_book.*
import java.util.*
import kotlin.collections.ArrayList


class RegisterBookActivity : AppCompatActivity(), View.OnClickListener {

    private val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private var storageReference: StorageReference? = null

    private var photoList: ArrayList<String> = ArrayList()
    private var photoUrlList: ArrayList<String> = ArrayList()

    private var book: Book? = null
    private var dialog: AlertDialog? = null

    private val imageName = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_book)

//        photoList =
//        photoUrlList = ArrayList()

        Permissions.validatePermssion(permission, this, 1)

        loadSpinnerData()

        storageReference = ConfigureFirebase.getStorageReference()


    }


    //2.1.On Post
    fun validateData(v: View) {
        book = createBook()

        //validate phone number
        val price = priceEditText.rawValue.toString()

        if (photoList.size != 0) {
            if (book!!.semester!!.isNotEmpty() && book!!.semester != "Semester") {
                if (book!!.subject!!.isNotEmpty() && book!!.subject != "Subject") {
                    if (book!!.price!!.isNotEmpty() && price != "0") {
                        if (book!!.phone!!.isNotEmpty()) {
                            if (book!!.description!!.isNotEmpty()) {
                                if (book!!.title!!.isNotEmpty()) {
                                    saveBook()
                                } else showToastMsg("Fill Title")
                            } else showToastMsg("Fill Description")
                        } else showToastMsg("Fill Phone number field")
                    } else showToastMsg("Enter valid Price")
                } else showToastMsg("Select subject")
            } else showToastMsg("Select Semester")
        } else showToastMsg("Select at least one photo")
    }

    //2.2.Create Book Object
    private fun createBook(): Book {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val phone = phoneNumberEditText.text.toString()
        val price = priceEditText.text.toString()
        val semester = semesterSpinner.selectedItem.toString()
        val subject = subjectSpinner.selectedItem.toString()
        val userId = ConfigureFirebase.getUserId()

        return Book(
            title = title,
            description = description,
            phone = phone,
            semester = semester,
            subject = subject,
            price = price,
            userId = userId,
            reported = false
        )
    }

    //2.3.Save Book
    private fun saveBook() {
        dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Uploading Your Book")
            .setCancelable(false)
            .build()

        dialog!!.show()

        for (i in photoList.indices) {
            val imageUrl = photoList[i]
            val listSize = photoList.size
            savePhotoStorage(imageUrl, listSize, i)
        }
    }

    //2.4. save photo
    private fun savePhotoStorage(imageUrl: String, listSize: Int, i: Int) {
        val userId = ConfigureFirebase.getUserId()!!

        val imageBook: StorageReference = storageReference!!
            .child("images")
            .child("books")
            .child(userId)
            .child(imageName)

        val uploadTask = imageBook.putFile(Uri.parse(imageUrl))
        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnCompleteListener { task ->
                val convertUrl = task.result.toString()

                photoUrlList.add(convertUrl)

                if (listSize == photoUrlList.size) {
                    book!!.setPhoto(photoUrlList)
                    ConfigureFirebase.getBookDbRef().add(book!!).addOnCompleteListener {
                        dialog!!.dismiss()
                        finish()
                    }
                }
            }
        }
    }


    private fun showToastMsg(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    //1.1.On Image Click
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.bookImageView1 -> chooseImage(1)
            R.id.bookImageView2 -> chooseImage(2)
            R.id.bookImageView3 -> chooseImage(3)
        }
    }

    //1.2.Open Gallery
    private fun chooseImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    //1.3.2.Activity Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val image = data!!.data
            val imagePath = image.toString()

            when (requestCode) {
                1 -> bookImageView1.setImageURI(image)
                2 -> bookImageView2.setImageURI(image)
                3 -> bookImageView3.setImageURI(image)
            }
            photoList.add(imagePath)
        }
    }

    //1.3.1.Request Permission Again
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (permissionResult in grantResults) {
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                permissionAlert()
            }
        }
    }

    //1.3.1.1.Permission Grant alert
    private fun permissionAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissions Denied")
        builder.setMessage("Please grant the required permission to use the app")
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm ") { _, _ ->
            finish()
        }

        // setPositiveButton takes two parameter text and instance of interface(lambda expression)
        // lambda expression can be taken out of parenthesis
        // the overriding method of interface also takes two parameter

        val aDialog = builder.create()
        aDialog.show()
    }

    //Spinner
    private fun loadSpinnerData() {

        // semester
        val semester = resources.getStringArray(R.array.semester)
        val semesterAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, semester)

        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        semesterSpinner.adapter = semesterAdapter

        // subject- update subject based semester
        var subject = resources.getStringArray(R.array.sub_sem_1)

        semesterSpinner.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                when (position) {
                    1 -> subject = resources.getStringArray(R.array.sub_sem_1)
                    2 -> subject = resources.getStringArray(R.array.sub_sem_2)
                    3 -> subject = resources.getStringArray(R.array.sub_sem_3)
                    4 -> subject = resources.getStringArray(R.array.sub_sem_4)
                    5 -> subject = resources.getStringArray(R.array.sub_sem_5)
                    6 -> subject = resources.getStringArray(R.array.sub_sem_6)
                    7 -> subject = resources.getStringArray(R.array.sub_sem_7)
                    8 -> subject = resources.getStringArray(R.array.sub_sem_8)
                }
                val subjectAdapter: ArrayAdapter<String> = ArrayAdapter(this@RegisterBookActivity, android.R.layout.simple_spinner_item, subject)
                subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                subjectSpinner.adapter = subjectAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}