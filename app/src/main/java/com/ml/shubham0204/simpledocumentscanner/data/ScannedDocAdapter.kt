package com.ml.shubham0204.simpledocumentscanner.data

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ml.shubham0204.simpledocumentscanner.R
import java.text.SimpleDateFormat

// Adapter for RecyclerView that holds data for scanned documents.
// See content_main.xml
class ScannedDocAdapter( private var context: Context , private var itemClickListener : onItemClickListener )
    : RecyclerView.Adapter<ScannedDocAdapter.ScannedDocViewHolder>(){

    // ArrayList to hold the ScannedDocument(s)
    var scannedDocsList = ArrayList<ScannedDocument>()
    private val dateToTextFormat = SimpleDateFormat( "yyyy MM dd HH:mm" )

    // An interface to listen for onClick and onLongClick events on items of recyclerView
    interface onItemClickListener {
        fun onItemClick( doc : ScannedDocument , position: Int )
        fun onItemLongClick( doc : ScannedDocument , position: Int )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedDocViewHolder {
        return ScannedDocViewHolder( ( context as Activity ).layoutInflater.inflate( R.layout.scanned_doc_item_layout , parent , false ))
    }

    override fun onBindViewHolder(holder: ScannedDocViewHolder, position: Int) {
        val doc = scannedDocsList[ position ]
        holder.docName.text = doc.name
        holder.docDate.text = dateToTextFormat.format( doc.creationDate )
        holder.docImage.load( doc.uri!! ) {
            crossfade( true )
            placeholder( R.drawable.ic_select_image_dark_24 )
            error( R.drawable.ic_image_not_exist_dark_24 )
        }
    }

    override fun getItemCount(): Int {
        return scannedDocsList.size
    }

    fun addDocs( data : ArrayList<ScannedDocument> ) {
        data.reverse()
        scannedDocsList = ArrayList()
        scannedDocsList = data
        notifyDataSetChanged()
    }

    fun removeDoc( doc : ScannedDocument ) {
        val position = scannedDocsList.indexOf( doc )
        scannedDocsList.removeAt( position )
        notifyItemRemoved( position )
    }

    inner class ScannedDocViewHolder( itemView : View) : RecyclerView.ViewHolder( itemView ) {

        val docName : TextView = itemView.findViewById( R.id.scanned_doc_name )
        val docDate : TextView = itemView.findViewById( R.id.scanned_doc_date )
        val docImage : ImageView = itemView.findViewById( R.id.scanned_doc_image )
        private val root : ConstraintLayout = itemView.findViewById( R.id.scanned_doc_root )

        init {
            root.setOnClickListener {
                itemClickListener.onItemClick( scannedDocsList[adapterPosition] , adapterPosition )
            }
            root.setOnLongClickListener {
                itemClickListener.onItemLongClick( scannedDocsList[adapterPosition] , adapterPosition )
                true
            }
        }

    }

}