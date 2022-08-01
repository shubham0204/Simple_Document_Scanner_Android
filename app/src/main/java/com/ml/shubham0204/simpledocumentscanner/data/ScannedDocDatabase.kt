package com.ml.shubham0204.simpledocumentscanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database( entities = [ScannedDocument::class] , version = 1 , exportSchema = false )
abstract class ScannedDocDatabase : RoomDatabase() {

    abstract fun getDAO() : ScannedDocumentDAO

    companion object {

        @Volatile
        private var INSTANCE : ScannedDocDatabase? = null

        fun getDatabase(context: Context): ScannedDocDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder( context, ScannedDocDatabase::class.java, "scanned_doc_database")
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }

}