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
class DocumentScanner( inferenceCallback: InferenceCallback ) {

    private val API_INPUT_IMAGE_KEY = "image"
    private val API_URL = "http://192.168.43.154:8080/get_rect"
    private val client = OkHttpClient()
    private lateinit var tempImageFile : File
    private var scaleFactor = 1f
    private var currentImage : Bitmap? = null


    interface InferenceCallback {
        fun onInference( image : Bitmap , boundingBox: BoundingBox )
        fun onError( message : String )
    }

    fun cropDocument( image : Bitmap )  {
        createAndSendRequest( image )
    }

    private fun createAndSendRequest(image : Bitmap ) {
        tempImageFile = File.createTempFile( "image" , "png" )
        val outputStream = FileOutputStream( tempImageFile )
        val resizedImage = processImage( image )
        resizedImage.compress( Bitmap.CompressFormat.PNG , 100 , outputStream )
        outputStream.close()
        sendRequest( tempImageFile )
    }

    // https://stackoverflow.com/questions/23512547/how-to-use-okhttp-to-upload-a-file
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

    private fun processImage(image : Bitmap ) : Bitmap {
        val rotatedImage = if ( image.width > image.height ) { image.rotate( 90f ) } else { image }
        currentImage = rotatedImage
        val aspectRatio = rotatedImage.width.toFloat() / rotatedImage.height.toFloat()
        val requiredWidth = ( aspectRatio * 480 ).toInt()
        scaleFactor = rotatedImage.width.toFloat() / requiredWidth
        return Bitmap.createScaledBitmap( rotatedImage , requiredWidth , 480 , false )
    }

    // Rotating a Bitmap
    // SO -> https://stackoverflow.com/a/48715217/13546426
    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply{ postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true )
    }



}