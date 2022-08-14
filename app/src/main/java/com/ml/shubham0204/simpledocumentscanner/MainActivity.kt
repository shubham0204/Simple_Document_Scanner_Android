package com.ml.shubham0204.simpledocumentscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
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
            MaterialDialog( this@MainActivity ).show{
                listItems( R.array.long_press_menu_options ){ dialog, index, text ->
                    when( index ) {
                        0 -> {
                            scannedDocAdapter.removeDoc( doc )
                            scannedDocRepository.removeDoc( doc )
                        }
                        1 -> {
                            shareDoc( doc )
                        }
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

    private fun hasStoragePermission() : Boolean {
        return if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ) {
            ActivityCompat.checkSelfPermission( this , Manifest.permission.WRITE_EXTERNAL_STORAGE ) ==
                    PackageManager.PERMISSION_GRANTED
        }
        else {
            true
        }
    }

    private fun requestStoragePermission() {
        MaterialDialog( this ).show {
            title( R.string.alert_dialog_title_storage_permission )
            message( R.string.message_storage_permission_request )
            positiveButton( text = "Allow" ){
                storagePermissionRequest.launch( Manifest.permission.WRITE_EXTERNAL_STORAGE )
            }
            negativeButton( text = "Deny" ){

            }
            cornerRadius( res = R.dimen.material_dialog_corner_radius )
        }
    }

    private val storagePermissionRequest = registerForActivityResult( ActivityResultContracts.RequestPermission() ) {
        if ( it ) {
            dispatchSelectPictureIntent()
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