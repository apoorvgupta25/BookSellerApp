package com.example.bookseller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookseller.R
import com.example.bookseller.adapter.BookAdapter.BookViewHolder
import com.example.bookseller.model.Book
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register_book.view.*
import kotlinx.android.synthetic.main.book_item.view.*


class BookAdapter(private val booksList: ArrayList<Book>) : RecyclerView.Adapter<BookViewHolder>() {

// TODO: use DiffUtil rather than notifydatasetchanges
// TODO: Coding in flow upcoming tutorial on Click

    var isShimmer = true

    //interface and custom Click
    private var listener: OnItemClickListener? = null

    public interface OnItemClickListener {
//        fun onItemClick(bookItem: Book)
        fun onItemClick(position: Int)
    }

    //custom setOnItemClickListener
    fun setOnBookClickListener(listener: OnItemClickListener){
        this.listener = listener
    }


    //Implementing Adapter Methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
//        TODO("Remaining")
        if(isShimmer){
            holder.itemView.shimmer_layout.startShimmer()
        } else {
            holder.itemView.shimmer_layout.stopShimmer()
            holder.itemView.shimmer_layout.setShimmer(null)
            val currentBook: Book = booksList[position]

            holder.itemView.bookTitleTextView.background = null
            holder.itemView.bookPriceTextView.background = null
            holder.imageView.background = null

            holder.itemView.bookTitleTextView.text = currentBook.title
            holder.itemView.bookPriceTextView.text = currentBook.price.toString()


//        val imageView: ImageView = itemV

            val urlPicture = currentBook.getPhoto()
            val urlCover = urlPicture[0]
            Picasso.get().load(urlCover).into(holder.imageView);
        }

        //In One Line //todo: Example of apply
//        holder.itemView.apply {
//            titleTextView.text = "Book @"
//            priceTextView.text = "$$$"
//        }

    }

    override fun getItemCount(): Int {
        return if(isShimmer) 5 else booksList.size      //5 items show while loading
    }

    // View Holder Class
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imageView: ImageView = itemView.findViewById(R.id.imageView)
        init {
            itemView.setOnClickListener {
                if(listener != null){
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener!!.onItemClick(position)
                    }
                }
            }
        }
    }
}