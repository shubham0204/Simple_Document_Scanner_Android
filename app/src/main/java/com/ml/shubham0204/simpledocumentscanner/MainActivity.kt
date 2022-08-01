package com.ml.shubham0204.simpledocumentscanner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.api.DocumentScanner
import com.ml.shubham0204.simpledocumentscanner.databinding.ContentMainBinding

// This app uses the API to crop documents
class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding : ContentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ContentMainBinding.inflate( layoutInflater )
        setContentView( activityMainBinding.root )


        activityMainBinding.fab.setOnClickListener {
            dispatchSelectPictureIntent()
        }

    }


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
            finish()
        }
    }


}