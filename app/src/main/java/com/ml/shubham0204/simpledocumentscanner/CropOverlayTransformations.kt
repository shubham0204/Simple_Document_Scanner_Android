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

    private lateinit var boxTransformation : Matrix

    suspend fun getBoundedImage( image : Bitmap ) : Bitmap = withContext( Dispatchers.Main ) {
        boxTransformation = Matrix()
        val scaleFactor = viewHeight.toFloat() / image.height.toFloat()
        val requiredWidth = ( image.width.toFloat() * scaleFactor ).toInt()
        boxTransformation.preScale( scaleFactor , scaleFactor )
        boxTransformation.postTranslate( ( viewWidth / 2f ) - ( requiredWidth / 2f ) , 0f )
        return@withContext Bitmap.createScaledBitmap( image , requiredWidth, viewHeight, false)
    }


    suspend fun getScaledBoundingBox( boundingBox: BoundingBox ) : BoundingBox = withContext( Dispatchers.Main ) {
        val destRect = RectF()
        boxTransformation.mapRect( destRect , boundingBox.toRectF() )
        Log.e( "APP" , "TRANSFORMED RECT ${destRect.toShortString()}")
        return@withContext BoundingBox.createFromRect( destRect )
    }


}