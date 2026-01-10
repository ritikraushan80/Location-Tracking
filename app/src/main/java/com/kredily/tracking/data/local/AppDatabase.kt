package com.kredily.tracking.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/**
 * Created by Ritik on: 10/01/26
 */

@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        fun create(context: Context) = Room.databaseBuilder(
            context, AppDatabase::class.java, "tracking_db"
        ).build()
    }
}
