package com.ml.shubham0204.simpledocumentscanner.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// DAO ( Data Access Object ) for the `ScannedDocument`
@Dao
interface ScannedDocumentDAO {

    // This function returns an instance of LiveData, so we can listen to changes in the database
    @Query( "SELECT * FROM scanneddocument")
    fun getAllDocuments() : LiveData<List<ScannedDocument>>

    // The functions below suspend functions, and are used with the IO CoroutineScope in ScannedDocRepository
    // Their execution can be paused or resumed in different threads.

    @Insert
    suspend fun insertDocument( doc : ScannedDocument )

    @Delete
    suspend fun deleteDocument( doc : ScannedDocument )

}