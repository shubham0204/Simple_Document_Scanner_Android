package com.ml.shubham0204.simpledocumentscanner.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedDocumentDAO {

    @Query( "SELECT * FROM scanneddocument")
    fun getAllDocuments() : Flow<List<ScannedDocument>>

    @Insert
    fun insertDocument( doc : ScannedDocument )

    @Delete
    fun deleteDocument( doc : ScannedDocument )

}