package com.ml.shubham0204.simpledocumentscanner

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ml.shubham0204.simpledocumentscanner.api.BoundingBox
import com.ml.shubham0204.simpledocumentscanner.databinding.FragmentCropImageBinding

class CropImageFragment : Fragment() {

    private lateinit var cropImageBinding: FragmentCropImageBinding
    private lateinit var cropAreaDrawingOverlay: CropAreaDrawingOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        cropImageBinding = FragmentCropImageBinding.inflate( inflater )
        cropAreaDrawingOverlay = cropImageBinding.cropAreaDrawingOverlay
        return cropImageBinding.root
    }

    fun drawImageWithBox( image : Bitmap , box : BoundingBox ) {
        cropAreaDrawingOverlay.setImage( image )
        cropAreaDrawingOverlay.drawQuad( box )
    }


}