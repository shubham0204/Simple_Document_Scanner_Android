package com.ml.shubham0204.simpledocumentscanner

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.api.DocumentScanner
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocRepository
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocument
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityCropImageBinding
import com.ml.shubham0204.simpledocumentscanner.utils.BitmapUtils
import com.ml.shubham0204.simpledocumentscanner.utils.FileOps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CropImageActivity : AppCompatActivity() {

    private lateinit var documentScanner : DocumentScanner
    private lateinit var cropImageActivityBinding: ActivityCropImageBinding
    private lateinit var cropImageOverlay : CropAreaDrawingOverlay
    private lateinit var scannedDocRepository: ScannedDocRepository

    private lateinit var inputImage : Bitmap
    private lateinit var imageRotated : Bitmap
    private lateinit var bboxOnScaledImage : BoundingBox
    private var filename = ""

    private lateinit var progressDialog : MaterialDialog

    private val ioScope = CoroutineScope( Dispatchers.IO )
    private val mainScope = CoroutineScope( Dispatchers.Main )
    private val defaultScope = CoroutineScope( Dispatchers.Default )

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
                ioScope.launch{
                    val imageUri = Uri.parse( intent.extras?.getString( "image_uri" ) )
                    inputImage = FileOps.getBitmapFromStream( contentResolver.openInputStream( imageUri )!! )
                    defaultScope.launch {
                        documentScanner.cropDocument( inputImage )
                    }
                }
            }
        })

        cropImageActivityBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            if ( menuItem.itemId == R.id.crop_image_menu_save ) {
                askFileName()
            }
            true
        }

    }

    @SuppressLint("CheckResult")
    private fun askFileName() {
        MaterialDialog( this ).show {
            input( "✍️Enter filename" )
            positiveButton( text = "Done" ) {
                filename = getInputField().text.toString()
                binarizeImage( inputImage )
                progressDialog = MaterialDialog( this@CropImageActivity ).apply {
                    customView( R.layout.progress_dialog_layout )
                    cancelable( false )
                }
                progressDialog.show()
            }
            negativeButton( text = "Cancel" ) {
                dismiss()
            }
            cancelable( false )
        }
    }

    private fun binarizeImage( image : Bitmap ) {
        val croppedImage = BitmapUtils.cropImage( image , cropImageOverlay.getCurrentRectF() )
        ioScope.launch{
            documentScanner.binarizeDocument( croppedImage )
        }
    }

    private fun saveFile( image : Bitmap ) {
        val filename = "$filename.png"
        val fileUri = FileOps.saveImage( this , image , filename )

        scannedDocRepository.addDoc( ScannedDocument( filename , fileUri.toString() , System.currentTimeMillis() ))

        mainScope.launch {
            progressDialog.dismiss()
            MaterialDialog( this@CropImageActivity ).show {
                title( R.string.alert_dialog_title_crop_image_success )
                message( R.string.message_crop_img_saved )
                positiveButton( text = "Close" ){
                    finish()
                }
                cancelable( false )
            }
        }
    }


    private val inferenceCallback = object : DocumentScanner.InferenceCallback {

        override fun onCropDocumentInference(image: Bitmap, boundingBox: BoundingBox, isRotated : Boolean ) {
            mainScope.launch{
                Log.e( "APP" , "imagea size ${image.width} ${image.height}")
                this@CropImageActivity.imageRotated = image
                this@CropImageActivity.inputImage = image
                this@CropImageActivity.bboxOnScaledImage = boundingBox
                cropImageOverlay.setImageAndBox( image , boundingBox )
            }
        }

        override fun onBinarizeDocumentInference(image: Bitmap) {
            saveFile( image )
        }

        override fun onError(message: String) {
            Log.e( "APP" , message )
        }

    }


}