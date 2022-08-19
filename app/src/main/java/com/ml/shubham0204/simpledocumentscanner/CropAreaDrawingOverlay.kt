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

    // These variables hold the current image on which the box is being drawn as
    // well as the box ( along with its path )
    private var currentBox : BoundingBox? = null
    private lateinit var currentImage : Bitmap
    private var currentBoxPath : Path = Path()

    // These variables determine the look of the box and its vertices
    private val quadPaint = Paint().apply {
        color = Color.parseColor( "#4D90caf9" )
        style = Paint.Style.FILL
        strokeWidth = 6f
    }
    private val vertexPaint = Paint().apply {
        color = Color.parseColor( "#fdd835" )
        style = Paint.Style.FILL
    }
    private val vertexRadius = 20f
    lateinit var cropOverlayTransformations : CropOverlayTransformations

    // These variables are used to store the position and id of the vertex that
    // is being moved by the user
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
                // When the user presses the finger against the screen,
                // we determine if any vertex has to be moved.
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

    // This method
    // 1. Computes the distances of point ( given by x and y ) with the vertices of `currentBox`.
    // 2. If the distance from the nearest vertex is smaller than a threshold ( selectVertexDistanceThreshold ),
    //    return the ID of that vertex. Else, return null that indicates no vertex has to be moved
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

        // Draw `currentImage` on the canvas
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

    // Refresh the view to apply the changes
    private fun refresh() {
        customDraw = true
        invalidate()
    }


    fun getCurrentRectF() : RectF {
        val inverse = Matrix()
        cropOverlayTransformations.boxTransformation.invert( inverse )
        val bbox = currentBox?.toRectF()!!
        inverse.mapRect( bbox )
        return bbox
    }

    // Draw the image and box on the canvas.
    // We need to scale the image and the box so that it can fit within the device's screen
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