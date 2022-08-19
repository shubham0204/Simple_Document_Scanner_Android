package com.ml.shubham0204.simpledocumentscanner

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocAdapter
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocRepository
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocument
import com.ml.shubham0204.simpledocumentscanner.databinding.ContentMainBinding
import com.ml.shubham0204.simpledocumentscanner.utils.PermissionUtils


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
            // Manage Permissions
            if ( PermissionUtils.hasStoragePermission( this ) ) {
                dispatchSelectPictureIntent()
            }
            else {
                PermissionUtils.requestStoragePermission( this , storagePermissionRequest )
            }
        }

        val recyclerView = activityMainBinding.scannedDocsRecyclerview
        recyclerView.layoutManager = LinearLayoutManager( this )

        scannedDocRepository = ScannedDocRepository( this )
        scannedDocAdapter = ScannedDocAdapter( this , itemClickListener )

        scannedDocRepository.getAllDocs().observe( this@MainActivity ) {
            scannedDocAdapter.addDocs( it as ArrayList<ScannedDocument> )
        }
        recyclerView.adapter = scannedDocAdapter

    }

    private val itemClickListener = object : ScannedDocAdapter.onItemClickListener {

        override fun onItemClick(doc: ScannedDocument, position: Int) {
            startViewImageActivity( doc )
        }

        @SuppressLint("CheckResult")
        override fun onItemLongClick(doc: ScannedDocument, position: Int) {
            MaterialDialog( this@MainActivity ).show{
                listItems( items = listOf( "🗑️ Delete" , "🌐 Share") ){ dialog, index, text ->
                    when( index ) {
                        0 -> {
                            scannedDocAdapter.removeDoc( doc )
                            scannedDocRepository.removeDoc( doc )
                        }
                        1 -> { shareDoc( doc ) }
                    }
                }
            }
        }

    }

    private fun shareDoc( doc : ScannedDocument ) {
        val shareIntent = Intent( Intent.ACTION_SEND ).apply {
            type = "image/png"
            putExtra( Intent.EXTRA_STREAM , doc.uri )
        }
        startActivity( Intent.createChooser( shareIntent , "Share document via ..." ) )
    }

    private val storagePermissionRequest = registerForActivityResult( ActivityResultContracts.RequestPermission() ) { isGranted ->
        if ( isGranted ) {
            dispatchSelectPictureIntent()
        }
        else {
            MaterialDialog( this@MainActivity ).show {
                title( R.string.alert_dialog_title_storage_permission )
                message( text = "The app could not function without the permission" )
                positiveButton( text = "Close" ){
                    it.dismiss()
                }
            }
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
            if ( result.data != null ) {
                startCropImageActivity( result.data!!.data!! )
            }
        }

    // Start CropActivity once an image is selected, and pass the Uri of the image
    private fun startCropImageActivity( imageUri : Uri ) {
        Intent( this , CropImageActivity::class.java ).apply {
            putExtra( "image_uri" , imageUri.toString() )
            startActivity( this )
        }
    }

    // Start CropActivity once an scanned doc is selected, and pass the Uri of the image with its name
    private fun startViewImageActivity( doc : ScannedDocument ) {
        Intent( this , ViewImageActivity::class.java ).apply {
            putExtra( "image_uri" , doc.uri )
            putExtra( "image_name" , doc.name )
            startActivity( this )
        }
    }



}