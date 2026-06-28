package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.CertificatesDataSource
import com.example.moodlegovapp.data.offline.OfflineCache
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.repositoryinterface.CertificatesRepositoryProtocol
import com.google.gson.reflect.TypeToken

/**
 * Certificates Repository — cache-first reads ("Badges"/achievement-adjacent
 * content stays available offline per the Moodle doc's offline feature list).
 * Download URLs are not cached since they're short-lived and only meaningful
 * once online to actually fetch the file.
 */
class CertificatesRepository(
    private val certificatesDataSource: CertificatesDataSource,
    private val offlineCache: OfflineCache,
    private val dataStoreManager: DataStoreManager
) : CertificatesRepositoryProtocol {

    private fun userId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    override suspend fun getCertificates(): AppResult<List<Certificate>> =
        offlineCache.fetch(
            key = OfflineCache.certificatesKey(userId()),
            typeToken = object : TypeToken<List<Certificate>>() {},
            networkCall = { certificatesDataSource.getCertificates() }
        )

    override suspend fun getDownloadUrl(certificateId: Int): AppResult<String> =
        certificatesDataSource.getCertificateDownloadUrl(certificateId)

    override suspend fun viewCertificate(cmid: Int) = certificatesDataSource.viewCertificate(cmid)
}
