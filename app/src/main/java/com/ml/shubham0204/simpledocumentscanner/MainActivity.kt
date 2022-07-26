package com.ml.shubham0204.simpledocumentscanner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.api.DocumentScanner
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityMainBinding

// This app uses the API to crop documents
class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding : ActivityMainBinding
    private lateinit var documentScanner : DocumentScanner
    private lateinit var cropImageFragment: CropImageFragment
    private lateinit var inputImage : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate( layoutInflater )
        setContentView( activityMainBinding.root )

        documentScanner = DocumentScanner( inferenceCallback )

        activityMainBinding.takePictureButton.setOnClickListener {
            dispatchSelectPictureIntent()
            cropImageFragment = CropImageFragment()
            supportFragmentManager
                .beginTransaction()
                .add( R.id.edit_pic_fragment_container , cropImageFragment )
                .commit()
        }

    }

    private val inferenceCallback = object : DocumentScanner.InferenceCallback {

        override fun onInference(boundingBox: BoundingBox) {
            Log.e( "APP" , "$boundingBox" )
            cropImageFragment.drawImageWithBox( inputImage , boundingBox )
        }

        override fun onError(message: String) {
            Log.e( "APP" , message )
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
        registerForActivityResult( ActivityResultContracts.StartActivityForResult() ) { data ->
            if ( data.data != null ) {
                val inputStream = contentResolver.openInputStream( data.data!!.data!! )!!
                val bitmap = BitmapFactory.decodeStream( inputStream )
                inputImage = bitmap
                documentScanner.cropDocument( bitmap )
                inputStream.close()
            }
    }


}