package com.ml.shubham0204.simpledocumentscanner

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.api.DocumentScanner
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocDatabase
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocRepository
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocument
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityCropImageBinding
import com.ml.shubham0204.simpledocumentscanner.file.FileOps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CropImageActivity : AppCompatActivity() {

    private lateinit var documentScanner : DocumentScanner
    private lateinit var cropImageActivityBinding: ActivityCropImageBinding
    private lateinit var cropImageOverlay : CropAreaDrawingOverlay
    private lateinit var progressDialog: ProgressDialog
    private lateinit var scannedDocRepository: ScannedDocRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropImageActivityBinding = ActivityCropImageBinding.inflate( layoutInflater )
        setContentView( cropImageActivityBinding.root )

        scannedDocRepository = ScannedDocRepository( this )

        cropImageOverlay = cropImageActivityBinding.cropAreaDrawingOverlay
        documentScanner = DocumentScanner( inferenceCallback )

        cropImageOverlay.viewTreeObserver.addOnGlobalLayoutListener( object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                cropImageOverlay.viewTreeObserver.removeOnGlobalLayoutListener( this )

                cropImageOverlay.cropOverlayTransformations = CropOverlayTransformations( cropImageOverlay.width , cropImageOverlay.height )
                CoroutineScope( Dispatchers.IO ).launch{
                    val imageUri = Uri.parse( intent.extras?.getString( "image_uri" ) )
                    val inputImage = FileOps.getBitmapFromStream( contentResolver.openInputStream( imageUri )!! )
                    documentScanner.cropDocument( inputImage )
                }
            }
        })

        cropImageActivityBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            if ( menuItem.itemId == R.id.crop_image_menu_save ) {
                FileOps.saveFile( "image.png" , saveImageFileIntentLauncher )
            }
            true
        }

    }

    private val saveImageFileIntentLauncher = registerForActivityResult( ActivityResultContracts.StartActivityForResult() ) {
        it ->
        val fileUri = it.data?.data
        if ( fileUri != null ) {
            val outputStream = contentResolver.openOutputStream( fileUri )
            // outputStream.write()
            scannedDocRepository.addDoc( ScannedDocument( "hello" , System.currentTimeMillis() ))
        }
    }


    private val inferenceCallback = object : DocumentScanner.InferenceCallback {

        override fun onInference(image: Bitmap, boundingBox: BoundingBox) {
            cropImageOverlay.setImageAndBox( image , boundingBox )
        }

        override fun onError(message: String) {
            Log.e( "APP" , message )
        }

    }


}