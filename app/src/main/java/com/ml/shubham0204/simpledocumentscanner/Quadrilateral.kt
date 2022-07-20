package com.ml.shubham0204.simpledocumentscanner

import android.graphics.Point
import android.graphics.PointF

class Quadrilateral() {

    var p1 = PointF()
    var p2 = PointF()
    var p3 = PointF()
    var p4 = PointF()

    constructor(
        p1x : Int , p1y : Int ,
        p2x : Int , p2y : Int ,
        p3x : Int , p3y : Int ,
        p4x : Int , p4y : Int  ) : this() {
        p1 = PointF( p1x.toFloat() , p1y.toFloat() )
        p2 = PointF( p2x.toFloat() , p2y.toFloat() )
        p3 = PointF( p3x.toFloat() , p3y.toFloat() )
        p4 = PointF( p4x.toFloat() , p4y.toFloat() )
    }

    fun points() : Array<PointF> {
        return arrayOf( p1 , p2 , p3 , p4 )
    }

}