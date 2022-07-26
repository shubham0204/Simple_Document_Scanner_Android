package com.ml.shubham0204.simpledocumentscanner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.ml.shubham0204.simpledocumentscanner.R

// Helper methods to request permissions
class PermissionUtils {

    companion object {

        // As we're only writing/reading images that were created by the app itself, we don't need
        // the storage permission on devices > API 29
        // On older devices, we'd need the WRITE_EXTERNAL_STORAGE permission
        // See -> https://developer.android.com/training/data-storage/shared/media#storage-permission
        fun hasStoragePermission( context: Context ) : Boolean {
            return if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ) {
                ActivityCompat.checkSelfPermission( context , Manifest.permission.WRITE_EXTERNAL_STORAGE ) ==
                        PackageManager.PERMISSION_GRANTED }
            else {
                true
            }
        }

        fun requestStoragePermission( context : Context , launcher : ActivityResultLauncher<String> ) {
            MaterialDialog( context ).show {
                title( R.string.alert_dialog_title_storage_permission )
                message( R.string.message_storage_permission_request )
                positiveButton( text = "Allow" ){
                    it.dismiss()
                    launcher.launch( Manifest.permission.WRITE_EXTERNAL_STORAGE )
                }
                negativeButton( text = "Deny" ){ dialog ->
                    dialog.dismiss()
                    MaterialDialog( context ).show {
                        title( R.string.alert_dialog_title_storage_permission )
                        message( text = "The app could not function without the permission" )
                        positiveButton( text = "Close" ){
                            it.dismiss()
                        }
                    }
                }
            }
        }

    }

}