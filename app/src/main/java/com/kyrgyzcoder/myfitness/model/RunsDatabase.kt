package com.kyrgyzcoder.myfitness.model

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kyrgyzcoder.myfitness.utils.DateConverter

@Database(entities = [Runs::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class RunsDatabase : RoomDatabase() {

    abstract fun RunsDAO(): RunsDAO

    companion object {
        private var instance: RunsDatabase? = null

        fun getInstance(context: Context): RunsDatabase? {
            if (instance == null) {
                synchronized(RunsDatabase::class) {
                    instance = Room.databaseBuilder(
                        context,
                        RunsDatabase::class.java,
                        "runs_database"
                    ).fallbackToDestructiveMigration()
                        .addCallback(RoomCallback)
                        .build()
                }
            }
            return instance
        }

        private val RoomCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CreateDbAsyncTask(instance).execute()
            }
        }

        class CreateDbAsyncTask(db: RunsDatabase?) : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                /*In case you want to add some data to database at the beginning
                        val RunsDAO = db?.RunsDAO()
                        RunsDAO?.insert(Runs("" parameters))
                     */
            }
        }
    }

}