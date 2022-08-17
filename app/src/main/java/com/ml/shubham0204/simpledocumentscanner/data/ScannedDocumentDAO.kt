package com.ml.shubham0204.simpledocumentscanner.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// DAO ( Data Access Object ) for the `ScannedDocument`
@Dao
interface ScannedDocumentDAO {

    @Query( "SELECT * FROM scanneddocument")
    fun getAllDocuments() : LiveData<List<ScannedDocument>>

    @Insert
    suspend fun insertDocument( doc : ScannedDocument )

    @Delete
    suspend fun deleteDocument( doc : ScannedDocument )

}