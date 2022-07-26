package com.ml.shubham0204.simpledocumentscanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox

class CropAreaDrawingOverlay(context: Context? , attributeSet: AttributeSet ) : View(context , attributeSet ) {

    private lateinit var currentQuad : BoundingBox
    private lateinit var currentImage : Bitmap
    private var currentQuadPath : Path = Path()
    private val quadPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val vertexPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val vertexRadius = 24f
    private var customDraw = false

    init {
        // This call is necessary in order to override onDraw
        // See this SO thread -> https://stackoverflow.com/questions/12261435/canvas-does-not-draw-in-custom-view
        setWillNotDraw( false )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when( event!!.action ) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }


    override fun onDraw(canvas: Canvas?) {
        if ( !customDraw ) {
            super.onDraw(canvas)
            return
        }

        canvas?.drawBitmap( currentImage , 0f , 0f , null )

        // Draw the boundaries of the quadrilateral
        // Refer to this SO thread -> https://stackoverflow.com/questions/2047573/how-to-draw-filled-polygon
        val path = currentQuadPath.apply {
            reset()
            moveTo(currentQuad.x1, currentQuad.y1)
            lineTo(currentQuad.x1, currentQuad.y2)
            lineTo(currentQuad.x2, currentQuad.y2)
            lineTo(currentQuad.x2, currentQuad.y1)
            close()
        }
        canvas?.drawPath( path , quadPaint )

        // Drawing small filled circle at the vertices of the quadrilateral
        for ( point in currentQuad.points() ) {
            canvas?.drawCircle( point.x, point.y , vertexRadius , vertexPaint )
        }
        customDraw = false

    }

    fun drawQuad( quad : BoundingBox ) {
        customDraw = true
        currentQuad = quad
        invalidate()
    }

    fun setImage( image : Bitmap ) {
        currentImage = image
    }

}