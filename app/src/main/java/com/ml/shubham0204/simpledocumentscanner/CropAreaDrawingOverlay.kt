package com.ml.shubham0204.simpledocumentscanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

// Custom view to draw the crop selection box over the image
class CropAreaDrawingOverlay(context: Context? , attributeSet: AttributeSet ) : View(context , attributeSet ) {

    private var currentBox : BoundingBox? = null
    private lateinit var currentImage : Bitmap
    lateinit var cropOverlayTransformations : CropOverlayTransformations
    private var currentBoxPath : Path = Path()
    private val quadPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val vertexPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }
    private val vertexRadius = 20f

    private var customDraw = false
    private var moveQuadVertex = false
    private var moveQuadVertexID = 0
    private val selectVertexDistanceThreshold = 50


    init {
        // This call is necessary in order to override onDraw
        // See this SO thread -> https://stackoverflow.com/questions/12261435/canvas-does-not-draw-in-custom-view
        setWillNotDraw( false )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if ( currentBox == null ){
            return false
        }
        when( event!!.action ) {
            MotionEvent.ACTION_DOWN -> {
                Log.e( "APP" , "DOWN : ${event.x} ${event.y}")
                val id = inferBoxVertex( event.x , event.y )
                if ( id != null ) {
                    moveQuadVertex = true
                    moveQuadVertexID = id
                }
            }
            MotionEvent.ACTION_UP -> {
                Log.e( "APP" , "UP : ${event.x} ${event.y}")
                moveQuadVertex = false
                moveQuadVertexID = 0
            }
            MotionEvent.ACTION_MOVE -> {
                Log.e( "APP" , "MOVE : ${event.x} ${event.y}")
                if ( moveQuadVertex ) {
                    Log.e( "APP" , "MOVING ...")
                    currentBox!!.vertices[ moveQuadVertexID ] = PointF( event.x , event.y )
                    refresh()
                }
            }
        }
        return true
    }

    private fun inferBoxVertex(x : Float, y : Float ) : Int? {
        var id = 0
        for ( point in currentBox!!.vertices) {
            val d = sqrt( ( point.x - x ).pow(2) + ( point.y - y ).pow(2) )
            Log.e( "APP" , "DISTANCE $d")
            if ( d < selectVertexDistanceThreshold ) {
                return id
            }
            id += 1
        }
        return null
    }


    override fun onDraw(canvas: Canvas?) {
        if ( !customDraw ) {
            super.onDraw(canvas)
            return
        }

        Log.e( "APP" , "DRAWING ...")
        canvas?.drawBitmap( currentImage ,
            ( width / 2f ) - ( currentImage.width / 2f ) ,
            0f ,
            null )

        // Draw the boundaries of the quadrilateral
        // Refer to this SO thread -> https://stackoverflow.com/questions/2047573/how-to-draw-filled-polygon
        if ( currentBox != null ) {
            val path = currentBoxPath.apply {
                reset()
                moveTo( currentBox!!.vertices[0].x , currentBox!!.vertices[0].y )
                lineTo( currentBox!!.vertices[1].x , currentBox!!.vertices[1].y )
                lineTo( currentBox!!.vertices[2].x , currentBox!!.vertices[2].y )
                lineTo( currentBox!!.vertices[3].x , currentBox!!.vertices[3].y )
                close()
            }
            canvas?.drawPath( path , quadPaint )

            // Drawing small filled circle at the vertices of the quadrilateral
            for ( point in currentBox!!.vertices ) {
                canvas?.drawCircle( point.x, point.y , vertexRadius , vertexPaint )
            }
        }

        customDraw = false

    }


    private fun refresh() {
        customDraw = true
        invalidate()
    }


    fun setImageAndBox(image : Bitmap, box : BoundingBox ) {
        CoroutineScope( Dispatchers.Default ).launch {
            currentImage = cropOverlayTransformations.getBoundedImage( image )
            currentBox = cropOverlayTransformations.getScaledBoundingBox( box )
            CoroutineScope( Dispatchers.Main ).launch {
                refresh()
            }
        }
    }


}