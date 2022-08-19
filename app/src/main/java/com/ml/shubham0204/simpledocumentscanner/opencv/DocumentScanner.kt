package com.ml.shubham0204.simpledocumentscanner.opencv

import android.graphics.Bitmap
import android.graphics.Rect
import com.ml.shubham0204.simpledocumentscanner.utils.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.opencv.core.Core
import java.io.File



// This class handles the API calls and their responses.
class DocumentScanner( private var inferenceCallback: InferenceCallback) {

    // These variables preserve the aspect ratio and input image respectively.
    private var scaleFactor = 1f
    private var currentImage : Bitmap? = null
    private var isRotated = false

    private val mainScope = CoroutineScope( Dispatchers.Main )

    private val openCVResultCallback = object : CoreAlgorithm.OpenCVResultCallback {

        override fun onDocumentRectResult(rect: Rect) {
            val x = (rect.left * scaleFactor).toInt()
            val y = (rect.top * scaleFactor).toInt()
            val w = (rect.width() * scaleFactor).toInt()
            val h = (rect.height() * scaleFactor).toInt()
            mainScope.launch {
                inferenceCallback.onCropDocumentInference( currentImage!! , BoundingBox.createFromXYWH( x , y , w , h )  )
            }
        }

        override fun onBinarizeDocResult(binImage: Bitmap) {
            mainScope.launch {
                inferenceCallback.onBinarizeDocumentInference( binImage )
            }
        }

    }
    private val coreAlgorithm = CoreAlgorithm( openCVResultCallback )

    interface InferenceCallback {
        // Supplies the predicted results ( boundingBox ) along with the image on which the
        // predictions were made.
        fun onCropDocumentInference(image : Bitmap, boundingBox: BoundingBox)

        fun onBinarizeDocumentInference( image : Bitmap )

        // Pass the error message if any error occurs
        fun onError( message : String )
    }

    fun cropDocument( image : Bitmap )  {
        val resizedImage = processImage( image )
        coreAlgorithm.getDocumentRect( resizedImage )
    }

    fun binarizeDocument( image : Bitmap ) {
        coreAlgorithm.binarize( image )
    }

    // Process the input image before uploading it to the server
    private fun processImage(image : Bitmap ) : Bitmap {
        // Change the orientation of the image. We wish to have a portrait orientation.
        val rotatedImage = if ( image.width > image.height ) {
            isRotated = true
            BitmapUtils.rotate( image , 90f )
        } else {
            isRotated = false
            image
        }
        val aspectRatio = rotatedImage.width.toFloat() / rotatedImage.height.toFloat()
        val requiredWidth = ( aspectRatio * 480 ).toInt()
        // Store the rotated image and its aspect ratio, as the image will be scaled down in the next step
        scaleFactor = rotatedImage.width.toFloat() / requiredWidth
        currentImage = rotatedImage
        return Bitmap.createScaledBitmap( rotatedImage , requiredWidth , 480 , false )
    }




}