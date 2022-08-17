package com.ml.shubham0204.simpledocumentscanner.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.ml.shubham0204.simpledocumentscanner.R
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


        fun saveImage( context: Context , image : Bitmap , filename : String ) : Uri {
            // Android Filesystem oh!
            // Refer to this blog -> https://www.simplifiedcoding.net/android-save-bitmap-to-gallery/
            val contentValues = ContentValues().apply {
                put( MediaStore.Images.ImageColumns.DISPLAY_NAME , filename )
                put( MediaStore.Images.ImageColumns.DATE_TAKEN , System.currentTimeMillis() )
                put( MediaStore.Images.ImageColumns.MIME_TYPE , "image/png" )
                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
                    put( MediaStore.Images.ImageColumns.RELATIVE_PATH , Environment.DIRECTORY_PICTURES
                            + "/${context.getString(R.string.app_name)}" )
                }
            }
            val fileUri = context.contentResolver.insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI , contentValues )
            val fileOutputStream =  context.contentResolver.openOutputStream( fileUri!! )!!
            fileOutputStream.use{
                image.compress( Bitmap.CompressFormat.PNG , 100 , it )
                it.flush()
            }
            return fileUri
        }


    }

}