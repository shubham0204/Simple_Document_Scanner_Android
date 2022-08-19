package com.ml.shubham0204.simpledocumentscanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.ml.shubham0204.simpledocumentscanner.opencv.BoundingBox

// Performs transformations on the image and box in CropAreaDrawingOverlay
// Here's a nice blog to understand matrix transformations in Android ->
// https://medium.com/a-problem-like-maria/understanding-android-matrix-transformations-25e028f56dc7
class CropOverlayTransformations(
    private var viewWidth : Int ,
    private var viewHeight : Int
) {

    lateinit var boxTransformation : Matrix

    // Scale the image
    fun getBoundedImage( image : Bitmap ) : Bitmap {
        boxTransformation = Matrix()
        val scaleFactor = viewWidth.toFloat() / image.width.toFloat()
        val requiredHeight = ( image.height.toFloat() * scaleFactor ).toInt()
        boxTransformation.preScale( scaleFactor , scaleFactor )
        return Bitmap.createScaledBitmap( image , viewWidth , requiredHeight, false)
    }

    // Use the same matrix ( boxTransformation ) to scale the box
    fun getScaledBoundingBox( boundingBox: BoundingBox ) : BoundingBox {
        val destRect = RectF()
        boxTransformation.mapRect( destRect , boundingBox.toRectF() )
        Log.e( "APP" , "TRANSFORMED RECT ${destRect.toShortString()}")
        return BoundingBox.createFromRect( destRect )
    }


}