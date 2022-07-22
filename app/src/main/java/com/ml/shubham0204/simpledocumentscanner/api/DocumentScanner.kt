package com.ml.shubham0204.simpledocumentscanner.api

import android.graphics.Bitmap
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class DocumentScanner( inferenceCallback: InferenceCallback ) {

    private val API_INPUT_IMAGE_KEY = "image"
    private val API_URL = ""
    private val client = OkHttpClient()

    interface InferenceCallback {
        fun onInference( boundingBox: BoundingBox )
        fun onError( message : String )
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
            inferenceCallback.onInference( BoundingBox( x , y , w , h ))
        }

    }

    // https://stackoverflow.com/questions/23512547/how-to-use-okhttp-to-upload-a-file
    private fun sendRequest( imageFile : File) {
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


}