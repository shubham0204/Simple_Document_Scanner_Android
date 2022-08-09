package com.ml.shubham0204.simpledocumentscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocAdapter
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocRepository
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocument
import com.ml.shubham0204.simpledocumentscanner.databinding.ContentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// The first activity to open in the app
// The other two activities, namely, `ViewImageActivity` and `CropImageActivity` are opened from this activity
// to perform their respective tasks.
class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding : ContentMainBinding
    private lateinit var scannedDocAdapter: ScannedDocAdapter
    private lateinit var scannedDocRepository: ScannedDocRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ContentMainBinding.inflate( layoutInflater )
        setContentView( activityMainBinding.root )

        setSupportActionBar( activityMainBinding.contentMainToolbar )

        activityMainBinding.fab.setOnClickListener {
            dispatchSelectPictureIntent()
        }

        val recyclerView = activityMainBinding.scannedDocsRecyclerview
        recyclerView.layoutManager = LinearLayoutManager( this )
        scannedDocRepository = ScannedDocRepository( this )
        scannedDocAdapter = ScannedDocAdapter( this , itemClickListener )

        CoroutineScope( Dispatchers.IO ).launch {
            val docs = scannedDocRepository.getAllDocs()
            CoroutineScope( Dispatchers.Main ).launch {
                scannedDocAdapter.addDocs( docs as ArrayList<ScannedDocument> )
            }
        }
        recyclerView.adapter = scannedDocAdapter

    }



    private val itemClickListener = object : ScannedDocAdapter.onItemClickListener {

        override fun onItemClick(doc: ScannedDocument, position: Int) {
            startViewImageActivity( doc )
        }

        override fun onItemLongClick(doc: ScannedDocument, position: Int) {

        }

    }


    // Dispatch an `Intent` by which the user can select an image to detect a document.
    // Refer to these docs: https://developer.android.com/training/data-storage/shared/documents-files#open-file
    private fun dispatchSelectPictureIntent() {
        val selectPictureIntent = Intent( Intent.ACTION_OPEN_DOCUMENT ).apply {
            type = "image/*"
            addCategory( Intent.CATEGORY_OPENABLE )
        }
        selectPictureIntentLauncher.launch( selectPictureIntent )
    }

    private val selectPictureIntentLauncher =
        registerForActivityResult( ActivityResultContracts.StartActivityForResult() ) { result ->
            if ( result.data!!.data != null ) {
                startCropImageActivity( result.data!!.data!! )
            }
    }


    private fun startCropImageActivity( imageUri : Uri ) {
        Intent( this , CropImageActivity::class.java ).apply {
            putExtra( "image_uri" , imageUri.toString() )
            startActivity( this )
        }
    }

    private fun startViewImageActivity( doc : ScannedDocument ) {
        Intent( this , ViewImageActivity::class.java ).apply {
            putExtra( "image_uri" , doc.uri )
            startActivity( this )
        }
    }


}