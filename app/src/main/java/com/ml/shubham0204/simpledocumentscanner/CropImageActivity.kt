package com.ml.shubham0204.simpledocumentscanner

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.api.DocumentScanner
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocRepository
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocument
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityCropImageBinding
import com.ml.shubham0204.simpledocumentscanner.file.FileOps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CropImageActivity : AppCompatActivity() {

    private lateinit var documentScanner : DocumentScanner
    private lateinit var cropImageActivityBinding: ActivityCropImageBinding
    private lateinit var cropImageOverlay : CropAreaDrawingOverlay
    private lateinit var scannedDocRepository: ScannedDocRepository

    private lateinit var inputImage : Bitmap
    private lateinit var scaledImage : Bitmap
    private lateinit var bboxOnInputImage : BoundingBox
    private lateinit var bboxOnScaledImage : BoundingBox

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
                    inputImage = FileOps.getBitmapFromStream( contentResolver.openInputStream( imageUri )!! )
                    documentScanner.cropDocument( inputImage )
                }
            }
        })

        cropImageActivityBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            if ( menuItem.itemId == R.id.crop_image_menu_save ) {
                saveFile()
            }
            true
        }

    }

    private fun saveFile() {
        // Android Filesystem oh!
        // Refer to this blog -> https://www.simplifiedcoding.net/android-save-bitmap-to-gallery/
        val fileUri : Uri?
        val filename = "${FileOps.getFilenameForCurrentTime()}.png"
        val contentValues = ContentValues().apply {
            put( MediaStore.Images.ImageColumns.DISPLAY_NAME , filename )
            put( MediaStore.Images.ImageColumns.DATE_TAKEN , System.currentTimeMillis() )
            put( MediaStore.Images.ImageColumns.MIME_TYPE , "image/png" )
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
                put( MediaStore.Images.ImageColumns.RELATIVE_PATH , Environment.DIRECTORY_PICTURES + "/${getString(R.string.app_name)}" )
            }
        }
        fileUri = contentResolver.insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI , contentValues )

        val fileOutputStream = contentResolver.openOutputStream( fileUri!! )!!
        fileOutputStream.use{
            scaledImage.compress( Bitmap.CompressFormat.PNG , 100 , it )
            it.flush()
        }
        scannedDocRepository.addDoc( ScannedDocument( filename , fileUri.toString() , System.currentTimeMillis() ))

    }


    private fun transformBBoxForInputImage( boundingBox: BoundingBox ) : BoundingBox {
        val scaleFactor = inputImage.width.toFloat() / scaledImage.width.toFloat()
        val matrix = Matrix().apply { postScale( scaleFactor , scaleFactor ) }
        boundingBox.apply{
            matrix.mapRect( toRectF() )
        }
        return boundingBox
    }


    private val inferenceCallback = object : DocumentScanner.InferenceCallback {

        override fun onInference(image: Bitmap, boundingBox: BoundingBox) {
            this@CropImageActivity.scaledImage = image
            this@CropImageActivity.bboxOnScaledImage = boundingBox
            cropImageOverlay.setImageAndBox( image , boundingBox )
        }

        override fun onError(message: String) {
            Log.e( "APP" , message )
        }

    }


}