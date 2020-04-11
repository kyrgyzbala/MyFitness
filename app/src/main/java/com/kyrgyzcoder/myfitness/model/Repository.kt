package com.kyrgyzcoder.myfitness.model

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

class Repository(app: Application) {

    private var runsDAO: RunsDAO
    private var allRuns: LiveData<List<Runs>>

    init {
        val db: RunsDatabase = RunsDatabase.getInstance(app.applicationContext)!!

        runsDAO = db.RunsDAO()
        allRuns = runsDAO.getAllRuns()
    }

    fun getAllRuns(): LiveData<List<Runs>> {
        return allRuns
    }

    fun insertRun(run: Runs) {
        InsertRunAsyncTask(runsDAO).execute(run)
    }

    fun deleteRun(run: Runs) {
        DeleteRunAsyncTask(runsDAO).execute(run)
    }

    fun deleteAllRuns() {
        DeleteAllRunsAsyncTask(runsDAO).execute()
    }

    companion object {

        class InsertRunAsyncTask(private var runsDAO: RunsDAO) : AsyncTask<Runs, Unit, Unit>() {
            override fun doInBackground(vararg params: Runs?) {
                runsDAO.insertRun(params[0]!!)
            }
        }

        class DeleteRunAsyncTask(private var runsDAO: RunsDAO) : AsyncTask<Runs, Unit, Unit>() {
            override fun doInBackground(vararg params: Runs?) {
                runsDAO.deleteRun(params[0]!!)
            }
        }

        class DeleteAllRunsAsyncTask(private var runsDAO: RunsDAO) : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                runsDAO.deleteAllRuns()
            }
        }
    }
}