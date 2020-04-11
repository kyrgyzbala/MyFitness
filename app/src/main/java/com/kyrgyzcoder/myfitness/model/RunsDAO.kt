package com.kyrgyzcoder.myfitness.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RunsDAO {

    @Insert
    fun insertRun(run: Runs)

    @Delete
    fun deleteRun(run: Runs)

    @Query("DELETE FROM all_runs_table")
    fun deleteAllRuns()

    @Query("SELECT * FROM all_runs_table ORDER BY id DESC")
    fun getAllRuns(): LiveData<List<Runs>>
}