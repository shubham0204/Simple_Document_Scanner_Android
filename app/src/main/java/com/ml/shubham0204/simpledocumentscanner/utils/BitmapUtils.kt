package com.ml.shubham0204.simpledocumentscanner.utils

import android.graphics.*

// Helper class to perform operations on Bitmap
class BitmapUtils {

    companion object {

        // Extension function for rotating a Bitmap
        // Refer to this SO answer -> https://stackoverflow.com/a/48715217/13546426
        fun rotate(image : Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix().apply{ postRotate(degrees) }
            return Bitmap.createBitmap( image , 0, 0, image.width, image.height, matrix, true )
        }

        // Crops image from the given bounding box ( RectF )
        // Refer to this SO answer -> https://stackoverflow.com/a/62876721/13546426
        // To see different PorterDuff modes, see -> https://developer.android.com/reference/android/graphics/PorterDuff.Mode
        fun cropImage( image : Bitmap , bbox : RectF) : Bitmap {
            val original = image.copy( Bitmap.Config.ARGB_8888, true )
            val result = Bitmap.createBitmap( original.width , original.height , Bitmap.Config.ARGB_8888 )
            val mCanvas = Canvas( result )
            val paint = Paint( Paint.ANTI_ALIAS_FLAG )
            val path = Path().apply {
                moveTo(bbox.left, bbox.top)
                lineTo(bbox.left, bbox.top + bbox.height())
                lineTo(bbox.left + bbox.width() , bbox.top + bbox.height() )
                lineTo(bbox.left + bbox.width(), bbox.top)
                close()
            }
            mCanvas.drawPath( path , paint )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN )
            mCanvas.drawBitmap( original , 0f , 0f , paint )
            return result
        }


    }

}