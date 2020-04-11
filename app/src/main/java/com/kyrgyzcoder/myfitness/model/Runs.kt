package com.kyrgyzcoder.myfitness.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "all_runs_table")
data class Runs(
    var runDate: Date,
    var runDistanceMeters: Int,
    var runTimeSecs: Int,
    var locStartLat: String,
    var locStartLon: String,
    var locEndLat: String,
    var locEndLon: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}