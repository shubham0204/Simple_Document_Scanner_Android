package com.ml.shubham0204.simpledocumentscanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CropOverlayTransformations(
    private var viewWidth : Int ,
    private var viewHeight : Int
) {

    lateinit var boxTransformation : Matrix

    fun getBoundedImage( image : Bitmap ) : Bitmap {
        boxTransformation = Matrix()
        val scaleFactor = viewWidth.toFloat() / image.width.toFloat()
        val requiredHeight = ( image.height.toFloat() * scaleFactor ).toInt()
        boxTransformation.preScale( scaleFactor , scaleFactor )
        return Bitmap.createScaledBitmap( image , viewWidth , requiredHeight, false)
    }


    fun getScaledBoundingBox( boundingBox: BoundingBox ) : BoundingBox {
        val destRect = RectF()
        boxTransformation.mapRect( destRect , boundingBox.toRectF() )
        Log.e( "APP" , "TRANSFORMED RECT ${destRect.toShortString()}")
        return BoundingBox.createFromRect( destRect )
    }


}