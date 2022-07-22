package com.ml.shubham0204.simpledocumentscanner.api

import android.graphics.Rect

class BoundingBox( var x : Int , y : Int , w : Int , h : Int ) {

    private var x1 = 0
    private var x2 = 0
    private var y1 = 0
    private var y2 = 0

    init {
        x1 = x
        y1 = y
        x2 = x1 + w
        y2 = y1 + h
    }

    fun getRect() : Rect {
        return Rect( x1 , y1 , x2 , y2 )
    }

}