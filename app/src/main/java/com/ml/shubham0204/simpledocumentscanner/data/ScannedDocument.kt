package com.ml.shubham0204.simpledocumentscanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScannedDocument(

    // Name of the scanned document
    @ColumnInfo( name = "name" )
    var name : String? ,

    // The file Uri from where the saved document can be retreived
    @ColumnInfo( name="uri" )
    var uri : String? ,

    // Creation date of the document
    @ColumnInfo( name = "creation_date" )
    var creationDate : Long? ,

    // Auto-incremented ID
    @PrimaryKey( autoGenerate = true )
    var id : Int = 0

)