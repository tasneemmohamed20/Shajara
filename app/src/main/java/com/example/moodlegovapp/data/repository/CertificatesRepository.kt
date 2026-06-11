package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.CertificatesDataSource
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.repositoryinterface.CertificatesRepositoryProtocol

/**
 * Certificates Repository that coordinates data sources for certificate operations.
 * Future: Add certificate caching and download progress tracking.
 */
class CertificatesRepository(
    private val certificatesDataSource: CertificatesDataSource
) : CertificatesRepositoryProtocol {

    override suspend fun getCertificates(): AppResult<List<Certificate>> {
        return certificatesDataSource.getCertificates()
    }

    override suspend fun getDownloadUrl(certificateId: Int): AppResult<String> {
        return certificatesDataSource.getCertificateDownloadUrl(certificateId)
    }
}