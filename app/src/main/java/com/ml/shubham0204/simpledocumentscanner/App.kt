package com.ml.shubham0204.simpledocumentscanner

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initDebug()) {
            Log.e( "APP" , "OpenCV initialization failed." )
        }
    }

}