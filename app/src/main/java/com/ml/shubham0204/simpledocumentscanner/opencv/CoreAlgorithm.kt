package com.ml.shubham0204.simpledocumentscanner.opencv

import android.graphics.Bitmap
import android.graphics.Rect
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class CoreAlgorithm {

    companion object {

        fun getDocumentRect(image: Bitmap): Rect {
            var x = Mat()
            Utils.bitmapToMat(image, x)
            x = convertColor(x)
            x = gaussianBlur(x)
            x = cannyEdgeDetection(x)
            val contours = contours(x)
            return getRectFromContour(findContourWithLargestPerimeter(contours))
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

        private fun contours( mat : Mat ) : List<MatOfPoint> {
            val contours : List<MatOfPoint> = ArrayList()
            val hierarchy = Mat()
            Imgproc.findContours( mat , contours , hierarchy , Imgproc.RETR_TREE , Imgproc.CHAIN_APPROX_SIMPLE )
            return contours
        }

        private fun findContourWithLargestPerimeter( contours : List<MatOfPoint> ) : MatOfPoint {
            val arcLengths = contours.map{
                Imgproc.arcLength( MatOfPoint2f( it ) , true )
            }
            return contours[ arcLengths.indexOf( arcLengths.maxOrNull()!! ) ]
        }

        private fun getRectFromContour( contour : MatOfPoint ) : Rect {
            val rect = Imgproc.boundingRect( contour )
            return Rect( rect.x , rect.y , rect.x + rect.width , rect.y + rect.height )
        }



    }


}