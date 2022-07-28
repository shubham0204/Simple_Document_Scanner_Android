package com.ml.shubham0204.simpledocumentscanner.api

import android.graphics.Bitmap
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

    interface InferenceCallback {
        fun onInference( boundingBox: BoundingBox )
        fun onError( message : String )
    }

    fun cropDocument( image : Bitmap ) {
        tempImageFile = File.createTempFile( "image" , "png" )
        FileOutputStream( tempImageFile ).apply{
            image.compress( Bitmap.CompressFormat.PNG , 100 , this )
            close()
        }
        sendRequest( tempImageFile )
    }


    // https://stackoverflow.com/questions/23512547/how-to-use-okhttp-to-upload-a-file
    private fun sendRequest( imageFile : File ) {
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
        client.newCall( request ).enqueue( responseCallback )
    }

    private val responseCallback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            inferenceCallback.onError( e.message!! )
        }

        override fun onResponse(call: Call, response: Response) {
            val output = JSONArray( response.body!!.string() )
            val x = output.getInt( 0 )
            val y = output.getInt( 1 )
            val w = output.getInt( 2 )
            val h = output.getInt( 3 )
            inferenceCallback.onInference( BoundingBox.createFromXYWH( x , y , w , h ))
            tempImageFile.delete()
        }

    }



}