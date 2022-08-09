package com.ml.shubham0204.simpledocumentscanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScannedDocument(

    @ColumnInfo( name = "name" )
    var name : String? ,

    @ColumnInfo( name="uri" )
    var uri : String? ,

    @ColumnInfo( name = "creation_date" )
    var creationDate : Long? ,

    @PrimaryKey( autoGenerate = true )
    var id : Int = 0

)