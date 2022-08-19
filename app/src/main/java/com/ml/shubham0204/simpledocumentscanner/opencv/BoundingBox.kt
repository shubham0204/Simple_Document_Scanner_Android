package com.ml.shubham0204.simpledocumentscanner.opencv

import android.graphics.PointF
import android.graphics.RectF

// BoundingBox class that holds the RectF for the predicted document
class BoundingBox(
    private var p1 : PointF ,
    p2 : PointF ,
    private var p3 : PointF ,
    p4 : PointF ) {

    var vertices = arrayOf( p1 , p2 , p3 , p4 )

    companion object {

        // Creates a new BoundingBox instance from given x, y, width and height.
        fun createFromXYWH( x : Int  , y : Int , w : Int , h : Int ) : BoundingBox {
            return BoundingBox(
                PointF( x.toFloat() , y.toFloat() ) ,
                PointF( x.toFloat() , (y+h).toFloat() ) ,
                PointF( (x+w).toFloat() , (y+h).toFloat() ) ,
                PointF( (x+w).toFloat() , y.toFloat() )
            )
        }

        fun createFromRect( rect : RectF ) : BoundingBox {
            return BoundingBox(
                PointF(rect.left, rect.top ) ,
                PointF( rect.left , rect.top + rect.height() ) ,
                PointF( rect.left + rect.width() , rect.top + rect.height() ) ,
                PointF( rect.left + rect.width() , rect.top )
            )
        }

    }

    // With the current `vertices`, return a new RectF
    fun toRectF() : RectF {
        return RectF( vertices[0].x , vertices[0].y , vertices[2].x , vertices[2].y )
    }


    override fun toString(): String {
        return "${p1.x} ${p1.y} ${p3.x} ${p3.y}"
    }

}