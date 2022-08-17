package com.ml.shubham0204.simpledocumentscanner.data

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Data repository from where rest of the application interacts with the
// database.
// See ScannedDocument.kt -> ScannedDocumentDAO.kt -> ScannedDocDatabase
class ScannedDocRepository( context: Context ) {

    private val scannedDocDatabase = ScannedDocDatabase.getDatabase( context )
    private val ioScope = CoroutineScope( Dispatchers.IO )

    fun addDoc( doc : ScannedDocument ) {
        ioScope.launch {
            scannedDocDatabase.getDAO().insertDocument( doc )
        }
    }

    fun removeDoc( doc : ScannedDocument ) {
        ioScope.launch {
            scannedDocDatabase.getDAO().deleteDocument( doc )
        }
    }

    fun getAllDocs() : LiveData<List<ScannedDocument>> {
        return scannedDocDatabase.getDAO().getAllDocuments()
    }


}