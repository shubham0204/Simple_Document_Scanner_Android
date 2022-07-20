package com.ml.shubham0204.simpledocumentscanner

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityMainBinding

// This app uses the API to crop documents
class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate( layoutInflater )
        setContentView( activityMainBinding.root )

        activityMainBinding.takePictureButton.setOnClickListener {
            // dispatchSelectPictureIntent()
            val cropImageFragment = CropImageFragment()
            supportFragmentManager
                .beginTransaction()
                .add( R.id.edit_pic_fragment_container , cropImageFragment )
                .commit()
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
                inputStream.close()
            }
    }


}