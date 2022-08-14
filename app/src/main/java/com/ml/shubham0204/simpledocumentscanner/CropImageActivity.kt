package com.ml.shubham0204.simpledocumentscanner

import android.content.ContentValues
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.api.DocumentScanner
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocRepository
import com.ml.shubham0204.simpledocumentscanner.data.ScannedDocument
import com.ml.shubham0204.simpledocumentscanner.databinding.ActivityCropImageBinding
import com.ml.shubham0204.simpledocumentscanner.file.FileOps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CropImageActivity : AppCompatActivity() {

    private lateinit var documentScanner : DocumentScanner
    private lateinit var cropImageActivityBinding: ActivityCropImageBinding
    private lateinit var cropImageOverlay : CropAreaDrawingOverlay
    private lateinit var scannedDocRepository: ScannedDocRepository

    private lateinit var inputImage : Bitmap
    private lateinit var scaledImage : Bitmap
    private lateinit var bboxOnScaledImage : BoundingBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropImageActivityBinding = ActivityCropImageBinding.inflate( layoutInflater )
        setContentView( cropImageActivityBinding.root )

        scannedDocRepository = ScannedDocRepository( this )

        cropImageOverlay = cropImageActivityBinding.cropAreaDrawingOverlay
        documentScanner = DocumentScanner( inferenceCallback )

        cropImageOverlay.viewTreeObserver.addOnGlobalLayoutListener( object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                cropImageOverlay.viewTreeObserver.removeOnGlobalLayoutListener( this )
                cropImageOverlay.cropOverlayTransformations = CropOverlayTransformations( cropImageOverlay.width , cropImageOverlay.height )
                CoroutineScope( Dispatchers.IO ).launch{
                    val imageUri = Uri.parse( intent.extras?.getString( "image_uri" ) )
                    inputImage = FileOps.getBitmapFromStream( contentResolver.openInputStream( imageUri )!! )
                    documentScanner.cropDocument( inputImage )
                }
            }
        })

        cropImageActivityBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            if ( menuItem.itemId == R.id.crop_image_menu_save ) {
                saveFile()
            }
            true
        }

    }

    private fun saveFile() {
        // Android Filesystem oh!
        // Refer to this blog -> https://www.simplifiedcoding.net/android-save-bitmap-to-gallery/
        val fileUri : Uri?
        val filename = "${FileOps.getFilenameForCurrentTime()}.png"
        val contentValues = ContentValues().apply {
            put( MediaStore.Images.ImageColumns.DISPLAY_NAME , filename )
            put( MediaStore.Images.ImageColumns.DATE_TAKEN , System.currentTimeMillis() )
            put( MediaStore.Images.ImageColumns.MIME_TYPE , "image/png" )
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
                put( MediaStore.Images.ImageColumns.RELATIVE_PATH , Environment.DIRECTORY_PICTURES + "/${getString(R.string.app_name)}" )
            }
        }
        fileUri = contentResolver.insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI , contentValues )

        val fileOutputStream = contentResolver.openOutputStream( fileUri!! )!!
        fileOutputStream.use{
            val croppedImage = cropImage2( inputImage , transformBBoxForInputImage( bboxOnScaledImage ) )
            croppedImage.compress( Bitmap.CompressFormat.PNG , 100 , it )
            it.flush()
        }
        scannedDocRepository.addDoc( ScannedDocument( filename , fileUri.toString() , System.currentTimeMillis() ))

        MaterialDialog( this ).show {
            title( R.string.alert_dialog_title_crop_image_success )
            message( R.string.message_crop_img_saved )
            positiveButton( text = "Close" ){
                finish()
            }
            cancelable( false )
        }

    }

    // Crops image from the given bounding box ( RectF )
    // Refer to this SO answer -> https://stackoverflow.com/a/62876721/13546426
    private fun cropImage( image : Bitmap , bbox : BoundingBox ) : Bitmap {
        val original = image.copy(Bitmap.Config.ARGB_8888, true)
        val mask = createMask( image.width , image.height , bbox )
        val result = Bitmap.createBitmap( mask.width, mask.height, Bitmap.Config.ARGB_8888 )
        val mCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mCanvas.drawBitmap(original, 0f, 0f, null)
        mCanvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        return result
    }

    private fun cropImage2( image : Bitmap , bbox : BoundingBox ) : Bitmap {
        val mutableBitmap: Bitmap = image.copy(Bitmap.Config.ARGB_8888, true)

        val bitmap2 = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val polyCanvas = Canvas(bitmap2)

        val paint = Paint()
        paint.strokeWidth = 9f
        val rect = bbox.toRectF()
        val path = Path().apply {
            moveTo( rect.left , rect.top )
            lineTo( rect.left , rect.top + rect.height() )
            lineTo( rect.left + rect.width() , rect.top + rect.height() )
            lineTo( rect.left + rect.width() , rect.top )
            close()
        }
        polyCanvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        polyCanvas.drawBitmap(mutableBitmap, 0f, 0f, paint)

        return bitmap2

    }

    private fun createMask( width : Int , height : Int , boundingBox: BoundingBox ) : Bitmap {
        val mask = Bitmap.createBitmap( width , height , Bitmap.Config.RGB_565 )
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val rect = boundingBox.toRectF()
        val path = Path().apply {
            moveTo( rect.left , rect.top )
            lineTo( rect.left , rect.top + rect.height() )
            lineTo( rect.left + rect.width() , rect.top + rect.height() )
            lineTo( rect.left + rect.width() , rect.top )
            close()
        }
        val canvas = Canvas( mask )
        canvas.drawPath( path , paint )
        return mask
    }


    private fun transformBBoxForInputImage( boundingBox: BoundingBox ) : BoundingBox {
        val scaleFactor = inputImage.width.toFloat() / scaledImage.width.toFloat()
        val matrix = Matrix().apply { postScale( scaleFactor , scaleFactor ) }
        boundingBox.apply{
            matrix.mapRect( toRectF() )
        }
        return boundingBox
    }


    private val inferenceCallback = object : DocumentScanner.InferenceCallback {

        override fun onInference(image: Bitmap, boundingBox: BoundingBox) {
            this@CropImageActivity.scaledImage = image
            this@CropImageActivity.bboxOnScaledImage = boundingBox
            cropImageOverlay.setImageAndBox( image , boundingBox )
        }

        override fun onError(message: String) {
            Log.e( "APP" , message )
        }

    }


}