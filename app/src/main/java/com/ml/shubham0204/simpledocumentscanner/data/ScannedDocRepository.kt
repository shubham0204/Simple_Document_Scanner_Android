package com.ml.shubham0204.simpledocumentscanner.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScannedDocRepository( context: Context ) {

    private val scannedDocDatabase = ScannedDocDatabase.getDatabase( context )
    private val coroutineScope = CoroutineScope( Dispatchers.IO )

    fun addDoc( doc : ScannedDocument ) {
        coroutineScope.launch {
            scannedDocDatabase.getDAO().insertDocument( doc )
        }
    }

    fun removeDoc( doc : ScannedDocument ) {
        coroutineScope.launch {
            scannedDocDatabase.getDAO().deleteDocument( doc )
        }
    }

    suspend fun getAllDocs() : List<ScannedDocument> {
        return scannedDocDatabase.getDAO().getAllDocuments()
    }


}