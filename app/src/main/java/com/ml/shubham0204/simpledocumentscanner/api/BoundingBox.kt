package com.ml.shubham0204.simpledocumentscanner.api

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF

class BoundingBox( var x : Int , var y : Int , var w : Int , var h : Int ) {

    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f

    init {
        x1 = x.toFloat()
        y1 = y.toFloat()
        x2 = x1 + w
        y2 = y1 + h
    }

    fun getRect() : RectF {
        return RectF( x1 , y1 , x2 , y2 )
    }

    fun points() : Array<PointF> {
        return arrayOf( PointF( x1 , y1 ) , PointF( x1 , y2 ) , PointF( x2 , y2 ) , PointF( x2 , y1 ) )
    }

    override fun toString(): String {
        return "$x $y $w $h"
    }

}