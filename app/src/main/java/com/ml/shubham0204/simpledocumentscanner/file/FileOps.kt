package com.ml.shubham0204.simpledocumentscanner.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

// Helper methods for file handling operations
class FileOps {

    companion object {

        private val dateFormat = SimpleDateFormat( "yyyy_MM_dd_HHmm" )

        // Parse the `Bitmap` from `inputStream`
        fun getBitmapFromStream( inputStream: InputStream ) : Bitmap {
            val bitmap = BitmapFactory.decodeStream( inputStream )
            inputStream.close()
            return bitmap
        }

        fun getFilenameForCurrentTime() : String {
            return dateFormat.format( Date() )
        }


    }

}