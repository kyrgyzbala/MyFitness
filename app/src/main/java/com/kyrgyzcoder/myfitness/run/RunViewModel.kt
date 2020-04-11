package com.kyrgyzcoder.myfitness.run

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kyrgyzcoder.myfitness.model.Repository
import com.kyrgyzcoder.myfitness.model.Runs

class RunViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = Repository(application)
    private val allRuns: LiveData<List<Runs>> = repo.getAllRuns()

    fun getAllRuns(): LiveData<List<Runs>> {
        return allRuns
    }

    fun deleteRun(runs: Runs) {
        repo.deleteRun(runs)
    }

    fun insertRun(runs: Runs) {
        repo.insertRun(runs)
    }

    fun deleteAllRuns() {
        repo.deleteAllRuns()
    }
}
