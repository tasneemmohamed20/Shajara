package com.example.moodlegovapp.data.offline.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedResponseEntity::class,
        DownloadedFileEntity::class,
        PendingActionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun cachedResponseDao(): CachedResponseDao
    abstract fun downloadedFileDao(): DownloadedFileDao
    abstract fun pendingActionDao(): PendingActionDao

    companion object {
        @Volatile private var instance: OfflineDatabase? = null

        fun getInstance(context: Context): OfflineDatabase =
            instance ?: synchronized(this) {
                instance ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "moodle_offline.db"
                )
                    // Cache is fully repopulated from network; safe to rebuild on schema bumps.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
