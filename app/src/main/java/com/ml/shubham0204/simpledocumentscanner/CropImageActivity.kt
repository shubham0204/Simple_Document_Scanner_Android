package com.ml.shubham0204.simpledocumentscanner

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.ml.shubham0204.simpledocumentscanner.opencv.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.opencv.DocumentScanner
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

    // The three coroutine scopes used in this activity
    // ioScope -> To perform file-related or networking tasks
    // mainScope -> To update UI on main thread
    // defaultScope -> To perform heavy tasks like bitmap transformation
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

        // The cropOverlayTransformations require the CropAreaDrawingOverlay's width and height to scale the
        // image and bounding boxes.
        // Refer this SO answer -> https://stackoverflow.com/a/24035591/13546426
        cropImageOverlay.viewTreeObserver.addOnGlobalLayoutListener( object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                cropImageOverlay.viewTreeObserver.removeOnGlobalLayoutListener( this )
                cropImageOverlay.cropOverlayTransformations = CropOverlayTransformations(
                    cropImageOverlay.width , cropImageOverlay.height
                )
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
                // Start the pipeline:
                // Ask File Name to save -> binarize image -> Save file to storage -> Add file to ScannedDocRepository
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
        defaultScope.launch{
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

        override fun onCropDocumentInference(image: Bitmap, boundingBox: BoundingBox ) {
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
            MaterialDialog( this@CropImageActivity ).show {
                title( text = "Error" )
                message( text = "We faced an error while cropping the document. Error $message" )
                positiveButton( text = "Close" ){
                    it.dismiss()
                    finish()
                }
            }
        }

    }


}