package com.ml.shubham0204.simpledocumentscanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CropOverlayTransformations(
    private var viewWidth : Int ,
    private var viewHeight : Int
) {

    private val boxTransformation = Matrix()

    suspend fun getBoundedImage( image : Bitmap ) : Bitmap = withContext( Dispatchers.Main ) {
        var rotationFixedImage : Bitmap? = null
        if (image.width > image.height) {
            rotationFixedImage = image.rotate(90f)
            boxTransformation.postRotate( 90f )
        }
        else {
            rotationFixedImage = image
        }
        val aspectRatio = rotationFixedImage.width.toFloat() / rotationFixedImage.height.toFloat()
        val requiredHeight = ( viewWidth * (1f/aspectRatio) ).toInt()
        val scaleFactor = requiredHeight / rotationFixedImage.height.toFloat()
        boxTransformation.postScale( scaleFactor , scaleFactor )
        return@withContext Bitmap.createScaledBitmap( rotationFixedImage , viewWidth, requiredHeight, false)
    }


    suspend fun getScaledBoundingBox( boundingBox: BoundingBox ) : BoundingBox = withContext( Dispatchers.Main ) {
        val destRect = RectF()
        boxTransformation.mapRect( destRect , boundingBox.toRectF() )
        return@withContext BoundingBox.createFromRect( destRect )
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply{ postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true )
    }

}