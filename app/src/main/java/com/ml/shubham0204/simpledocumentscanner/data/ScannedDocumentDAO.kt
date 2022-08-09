package com.ml.shubham0204.simpledocumentscanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScannedDocumentDAO {

    @Query( "SELECT * FROM scanneddocument")
    suspend fun getAllDocuments() : List<ScannedDocument>

    @Insert
    suspend fun insertDocument( doc : ScannedDocument )

    @Delete
    suspend fun deleteDocument( doc : ScannedDocument )

}