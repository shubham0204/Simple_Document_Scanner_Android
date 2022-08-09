package com.ml.shubham0204.simpledocumentscanner.file

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import com.ml.shubham0204.simpledocumentscanner.R
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class FileOps {

    companion object {

        private val dateFormat = SimpleDateFormat( "yyyy_MM_dd_HHmm" )

        fun getBitmapFromFileName( context : Context , name : String ) : Bitmap {
            val imageFile = getFileFromName( context , name )
            return getBitmapFromStream( FileInputStream( imageFile ) )
        }

        fun getBitmapFromStream( inputStream: InputStream ) : Bitmap {
            val bitmap = BitmapFactory.decodeStream( inputStream )
            inputStream.close()
            return bitmap
        }


        private fun getFileFromName( context: Context , name : String ) : File {
            return File( Environment.DIRECTORY_PICTURES + "/${context.getString(R.string.app_name)}/${name}")
        }

        fun saveFile( filename : String , launcher : ActivityResultLauncher<Intent?> ) {
            val createFileIntent = Intent( Intent.ACTION_CREATE_DOCUMENT ).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/png"
                putExtra(Intent.EXTRA_TITLE, filename )
            }
            launcher.launch( createFileIntent )
        }

        fun getFilenameForCurrentTime() : String {
            return dateFormat.format( Date() )
        }


    }

}