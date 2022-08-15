package com.ml.shubham0204.simpledocumentscanner

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityViewImageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewImageActivity : AppCompatActivity() {

    private lateinit var viewImageBinding : ActivityViewImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewImageBinding = ActivityViewImageBinding.inflate( layoutInflater )
        setContentView( viewImageBinding.root )

        // Get the Uri of the image from the incoming intent
        val imageUri = Uri.parse( intent.extras?.getString( "image_uri" )!! )

        // To read the Bitmap from the Uri, there's a different method for Android Q+ devices
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            // Use the ImageDecoder class as `MediaStore.Images.Media.getBitmap` was deprecated
            val source = ImageDecoder.createSource( contentResolver , imageUri )
            CoroutineScope( Dispatchers.Default ).launch {
                val image = ImageDecoder.decodeBitmap( source )
                CoroutineScope( Dispatchers.Main ).launch {
                    viewImageBinding.viewDocImageview.setImageBitmap( image )
                }
            }
        }
        else {
            // On older devices, we use can use the `MediaStore.Images.Media.getBitmap` method
            val image = MediaStore.Images.Media.getBitmap( contentResolver , imageUri )
            viewImageBinding.viewDocImageview.setImageBitmap( image )
        }
        viewImageBinding.viewDocToolbar.setNavigationOnClickListener{
            finish()
        }

    }


}