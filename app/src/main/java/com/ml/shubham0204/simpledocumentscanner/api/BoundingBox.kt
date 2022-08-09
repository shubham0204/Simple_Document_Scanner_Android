package com.ml.shubham0204.simpledocumentscanner.api

import android.graphics.PointF
import android.graphics.RectF

class BoundingBox(
    private var p1 : PointF ,
    private var p2 : PointF ,
    private var p3 : PointF ,
    private var p4 : PointF ) {

    var vertices = arrayOf( p1 , p2 , p3 , p4 )

    companion object {

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

    fun toRectF() : RectF {
        return RectF( p1.x , p1.y , p3.x , p3.y )
    }


    override fun toString(): String {
        return "${p1.x} ${p1.y} ${p3.x} ${p3.y}"
    }

}