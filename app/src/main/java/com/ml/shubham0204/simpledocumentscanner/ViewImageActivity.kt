package com.ml.shubham0204.simpledocumentscanner

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityViewImageBinding
import com.ml.shubham0204.simpledocumentscanner.utils.FileOps

class ViewImageActivity : AppCompatActivity() {

    private lateinit var viewImageBinding : ActivityViewImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewImageBinding = ActivityViewImageBinding.inflate( layoutInflater )
        setContentView( viewImageBinding.root )

        setSupportActionBar( viewImageBinding.viewDocToolbar )

        // Get the Uri of the image from the incoming intent
        val imageUri = Uri.parse( intent.extras?.getString( "image_uri" )!! )
        val imageName = intent.extras?.getString( "image_name" )
        supportActionBar?.title = imageName

        val image = FileOps.loadImageFromUri( this , imageUri )
        viewImageBinding.viewDocImageview.setImageBitmap( image )
        viewImageBinding.viewDocToolbar.setNavigationOnClickListener{
            finish()
        }

    }


}