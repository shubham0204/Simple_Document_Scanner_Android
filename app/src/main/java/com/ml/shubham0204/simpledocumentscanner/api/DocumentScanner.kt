package com.ml.shubham0204.simpledocumentscanner.api

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Network Security Config
// https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
// https://developer.android.com/training/articles/security-config
// Turn off Windows Firewall

// This class handles the API calls and their responses.
class DocumentScanner( inferenceCallback: InferenceCallback ) {

    // The URL to which the POST call has to be made
    // TODO: Change this URL according to your API hosting
    private val API_URL = "http://192.168.43.154:8080/get_rect"

    // The 'key' with which the image is uploaded as a field in the form body.
    private val API_INPUT_IMAGE_KEY = "image"

    // This temporary file holds the image that will be uploaded the POST call.
    private lateinit var tempImageFile : File

    // OkHttp client to make requests
    private val client = OkHttpClient()

    // These variables preserve the aspect ratio and input image respectively.
    private var scaleFactor = 1f
    private var currentImage : Bitmap? = null


    interface InferenceCallback {
        // Supplies the predicted results ( boundingBox ) along with the image on which the
        // predictions were made.
        fun onInference( image : Bitmap , boundingBox: BoundingBox )

        // Pass the error message if any error occurs
        fun onError( message : String )
    }

    fun cropDocument( image : Bitmap )  {
        createAndSendRequest( image )
    }

    // Given the bitmap, send the request to the API
    private fun createAndSendRequest(image : Bitmap ) {
        // Create a temporary file and write the processed Bitmap to it.
        // Note, the image is scaled down and then sent to the API. See `@processImage` method.
        tempImageFile = File.createTempFile( "image" , "png" )
        FileOutputStream( tempImageFile ).run{
            val resizedImage = processImage( image )
            resizedImage.compress( Bitmap.CompressFormat.PNG , 100 , this )
        }
        sendRequest( tempImageFile )
    }

    // Sends a POST request to the server with OkHttpClient
    // Refer to this SO thread -> https://stackoverflow.com/questions/23512547/how-to-use-okhttp-to-upload-a-file
    private fun sendRequest(imageFile : File ) {
        val requestBody = MultipartBody.Builder().run{
            setType( MultipartBody.FORM )
            addFormDataPart(
                API_INPUT_IMAGE_KEY ,
                imageFile.name ,
                imageFile.asRequestBody( "image/png".toMediaTypeOrNull()!! ) )
            build()
        }
        val request = Request.Builder().run{
            url( API_URL )
            post( requestBody )
            build()
        }
        Log.e( "APP" , "Request sent")
        client.newCall( request ).enqueue( responseCallback )
    }


    private val responseCallback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            inferenceCallback.onError( e.message!! )
        }

        override fun onResponse(call: Call, response: Response) {
            val output = JSONArray( response.body!!.string() )
            val x = (output.getInt( 0 ) * scaleFactor).toInt()
            val y = (output.getInt( 1 ) * scaleFactor).toInt()
            val w = (output.getInt( 2 ) * scaleFactor).toInt()
            val h = (output.getInt( 3 ) * scaleFactor).toInt()
            tempImageFile.delete()
            CoroutineScope( Dispatchers.Main ).launch {
                inferenceCallback.onInference( currentImage!! , BoundingBox.createFromXYWH( x , y , w , h ))
            }
        }

    }

    // Process the input image before uploading it to the server
    private fun processImage(image : Bitmap ) : Bitmap {
        // Change the orientation of the image. We wish to have a portrait orientation.
        val rotatedImage = if ( image.width > image.height ) { image.rotate( 90f ) } else { image }
        val aspectRatio = rotatedImage.width.toFloat() / rotatedImage.height.toFloat()
        val requiredWidth = ( aspectRatio * 480 ).toInt()
        // Store the rotated image and its aspect ratio, as the image will be scaled down in the next step
        scaleFactor = rotatedImage.width.toFloat() / requiredWidth
        currentImage = rotatedImage
        return Bitmap.createScaledBitmap( rotatedImage , requiredWidth , 480 , false )
    }

    // Extension function for rotating a Bitmap
    // SO -> https://stackoverflow.com/a/48715217/13546426
    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply{ postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true )
    }



}