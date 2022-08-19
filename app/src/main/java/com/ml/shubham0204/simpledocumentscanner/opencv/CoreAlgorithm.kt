package com.ml.shubham0204.simpledocumentscanner.opencv

import android.graphics.Bitmap
import android.graphics.Rect
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class CoreAlgorithm( private var openCVResultCallback: OpenCVResultCallback ) {

    interface OpenCVResultCallback {
        fun onDocumentRectResult( rect : Rect )
        fun onBinarizeDocResult( binImage : Bitmap )
    }

    fun getDocumentRect( image: Bitmap ) {
        var x = Mat()
        Utils.bitmapToMat(image, x)
        x = convertColor(x)
        x = adaptiveThreshold(x)
        x = morphClose(x)
        x = erode(x)
        x = cannyEdgeDetection( x )
        val contours = contours(x)
        openCVResultCallback.onDocumentRectResult( getRectFromContour(findContourWithLargestPerimeter(contours)) )
    }

    fun binarize( image : Bitmap ) {
        var x = Mat()
        Utils.bitmapToMat(image, x)
        x = convertColor( x )
        x = threshold( x )
        val output = Bitmap.createBitmap( image.width , image.height , Bitmap.Config.RGB_565 )
        Utils.matToBitmap( x , output )
        openCVResultCallback.onBinarizeDocResult( output )
    }

    private fun convertColor(mat: Mat): Mat {
        return Mat().apply{ Imgproc.cvtColor( mat, this , Imgproc.COLOR_RGB2GRAY) }
    }

    private fun gaussianBlur( mat : Mat ) : Mat {
        return Mat().apply{ Imgproc.GaussianBlur( mat , this , Size( 9.0 , 9.0 ) , 0.0 )}
    }

    private fun cannyEdgeDetection( mat : Mat ) : Mat {
        return Mat().apply{ Imgproc.Canny( mat , this , 75.0 , 200.0 ) }
    }

    private fun adaptiveThreshold( mat : Mat ) : Mat {
        return Mat().apply {
            Imgproc.adaptiveThreshold(
                mat , this ,
                255.0 ,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C ,
                Imgproc.THRESH_BINARY ,
                25 ,
                5.0
            )
        }
    }

    private fun threshold( mat : Mat ) : Mat {
        return Mat().apply {
            Imgproc.threshold( mat , this , 150.0 , 255.0 , Imgproc.THRESH_BINARY )
        }
    }

    private fun erode( mat : Mat ) : Mat {
        val kernel = Mat.ones( Size( 11.0 , 11.0 ) , CvType.CV_8U )
        return Mat().apply {
            Imgproc.erode(
                mat , this ,
                kernel
            )
        }
    }

    private fun morphClose( mat : Mat ) : Mat {
        val kernel = Mat.ones( Size( 5.0 , 5.0 ) , CvType.CV_8U )
        return Mat().apply {
            Imgproc.morphologyEx(
                mat , this ,
                Imgproc.MORPH_CLOSE ,
                kernel
            )
        }
    }

    private fun contours( mat : Mat ) : List<MatOfPoint> {
        val contours : List<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours( mat , contours , hierarchy , Imgproc.RETR_TREE , Imgproc.CHAIN_APPROX_SIMPLE )
        return contours
    }

    private fun findContourWithLargestPerimeter( contours : List<MatOfPoint> ) : MatOfPoint {
        val arcLengths = contours.map{
            // Refer to this SO answer ->
            // https://stackoverflow.com/questions/11273588/how-to-convert-matofpoint-to-matofpoint2f-in-opencv-java-api
            val contour = MatOfPoint2f( *(it.toArray()) )
            Imgproc.arcLength( contour , true )
        }
        return contours[ arcLengths.indexOf( arcLengths.maxOrNull()!! ) ]
    }

    private fun getRectFromContour( contour : MatOfPoint ) : Rect {
        val rect = Imgproc.boundingRect( contour )
        return Rect( rect.x , rect.y , rect.x + rect.width , rect.y + rect.height )
    }





}