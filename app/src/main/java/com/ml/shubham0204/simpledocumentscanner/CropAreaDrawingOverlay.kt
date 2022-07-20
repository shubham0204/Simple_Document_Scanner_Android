package com.ml.shubham0204.simpledocumentscanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

class CropAreaDrawingOverlay(context: Context? , attributeSet: AttributeSet ) : View(context , attributeSet ) {

    private var currentQuad : Quadrilateral = Quadrilateral()
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
        // Draw the boundaries of the quadrilateral
        // Refer to this SO thread -> https://stackoverflow.com/questions/2047573/how-to-draw-filled-polygon
        val path = currentQuadPath.apply {
            reset()
            moveTo(currentQuad.p1.x, currentQuad.p1.y)
            lineTo(currentQuad.p2.x, currentQuad.p2.y)
            lineTo(currentQuad.p3.x, currentQuad.p3.y)
            lineTo(currentQuad.p4.x, currentQuad.p4.y)
            close()
        }
        canvas?.drawPath( path , quadPaint )

        // Drawing small filled circle at the vertices of the quadrilateral
        for ( point in currentQuad.points() ) {
            canvas?.drawCircle( point.x, point.y , vertexRadius , vertexPaint )
        }

    }

    fun drawQuad( quad : Quadrilateral ) {
        currentQuad = quad
        invalidate()
    }

}