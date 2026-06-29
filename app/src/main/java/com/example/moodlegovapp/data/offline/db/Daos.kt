package com.example.moodlegovapp.data.offline.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedResponseDao {

    @Query("SELECT * FROM cached_responses WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CachedResponseEntity?

    @Query("SELECT * FROM cached_responses WHERE courseId = :courseId")
    suspend fun getAllForCourse(courseId: Int): List<CachedResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: CachedResponseEntity)

    @Query("DELETE FROM cached_responses WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cached_responses")
    suspend fun clearAll()

    @Query("SELECT MAX(lastSyncedAt) FROM cached_responses")
    suspend fun lastSyncedAtAny(): Long?
}

@Dao
interface DownloadedFileDao {

    @Query("SELECT * FROM downloaded_files WHERE fileUrl = :url LIMIT 1")
    suspend fun get(url: String): DownloadedFileEntity?

    @Query("SELECT * FROM downloaded_files WHERE courseId = :courseId")
    fun observeForCourse(courseId: Int): Flow<List<DownloadedFileEntity>>

    @Query("SELECT * FROM downloaded_files WHERE courseId = :courseId")
    suspend fun getForCourse(courseId: Int): List<DownloadedFileEntity>

    @Query("SELECT * FROM downloaded_files WHERE state = :state")
    suspend fun getByState(state: String): List<DownloadedFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedFileEntity)

    @Update
    suspend fun update(entity: DownloadedFileEntity)

    @Query("DELETE FROM downloaded_files WHERE fileUrl = :url")
    suspend fun delete(url: String)

    @Query("DELETE FROM downloaded_files WHERE courseId = :courseId")
    suspend fun deleteForCourse(courseId: Int)

    @Query("SELECT COALESCE(SUM(remoteSizeBytes), 0) FROM downloaded_files WHERE state = 'DOWNLOADED'")
    suspend fun totalDownloadedBytes(): Long
}

@Dao
interface PendingActionDao {

    @Query("SELECT * FROM pending_actions WHERE syncedAt IS NULL ORDER BY createdAt ASC")
    suspend fun getUnsynced(): List<PendingActionEntity>

    @Query("SELECT * FROM pending_actions WHERE syncedAt IS NULL ORDER BY createdAt ASC")
    fun observeUnsynced(): Flow<List<PendingActionEntity>>

    @Query("SELECT COUNT(*) FROM pending_actions WHERE syncedAt IS NULL")
    fun observePendingCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: PendingActionEntity): Long

    @Update
    suspend fun update(entity: PendingActionEntity)

    @Delete
    suspend fun delete(entity: PendingActionEntity)

    @Query("DELETE FROM pending_actions WHERE syncedAt IS NOT NULL")
    suspend fun clearSynced()
}
