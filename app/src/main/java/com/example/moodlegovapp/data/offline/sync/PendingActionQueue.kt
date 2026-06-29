package com.example.moodlegovapp.data.offline.sync

import com.example.moodlegovapp.data.offline.db.PendingActionDao
import com.example.moodlegovapp.data.offline.db.PendingActionEntity
import com.example.moodlegovapp.data.offline.db.PendingActionType
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

/** Payload queued when a file upload happens offline (bytes stored as base64). */
data class PendingFileUpload(
    val assignmentId: Int,
    val fileName: String,
    val fileBytesBase64: String
)

/**
 * Queues mutating calls made while offline so they can be replayed once the
 * device reconnects. Per the Moodle doc: "Actions performed offline are logged
 * and stored into the system with the synchronisation time, not with the time
 * when they happened" — [PendingActionEntity.createdAt] keeps the original local
 * time for the user's own reference (e.g. "saved offline at 10:03"), while
 * [PendingActionEntity.syncedAt] is stamped by [SyncEngine] only once the action
 * has actually gone out to the server, matching that documented behaviour.
 */
class PendingActionQueue(
    private val dao: PendingActionDao,
    private val gson: Gson = Gson()
) {
    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    suspend fun <T> enqueue(type: PendingActionType, payload: T) {
        dao.insert(
            PendingActionEntity(
                type = type.name,
                payloadJson = gson.toJson(payload),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getUnsynced(): List<PendingActionEntity> = dao.getUnsynced()

    fun <T> decode(entity: PendingActionEntity, clazz: Class<T>): T = gson.fromJson(entity.payloadJson, clazz)

    suspend fun markSynced(entity: PendingActionEntity) {
        dao.update(entity.copy(syncedAt = System.currentTimeMillis()))
    }

    suspend fun markFailed(entity: PendingActionEntity, error: String) {
        dao.update(entity.copy(attemptCount = entity.attemptCount + 1, lastError = error))
    }

    suspend fun clearSynced() = dao.clearSynced()
}
