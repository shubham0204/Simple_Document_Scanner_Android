package com.ml.shubham0204.simpledocumentscanner.file

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.ActivityResultLauncher
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FileOps {

    companion object {

        fun getBitmapFromFileName( name : String ) : Bitmap {
            val imageFile = getFileFromName( name )
            return getBitmapFromStream( FileInputStream( imageFile ) )
        }

        fun getBitmapFromStream( inputStream: InputStream ) : Bitmap {
            val bitmap = BitmapFactory.decodeStream( inputStream )
            inputStream.close()
            return bitmap
        }

        private fun getFileFromName( name : String ) : File? {
            return null
        }

        fun saveFile( filename : String , launcher : ActivityResultLauncher<Intent?> ) {
            val createFileIntent = Intent( Intent.ACTION_CREATE_DOCUMENT ).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/png"
                putExtra(Intent.EXTRA_TITLE, filename )
            }
            launcher.launch( createFileIntent )
        }


    }

}