package com.example.moodlegovapp.core.data.repository

import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.domain.repositoryinterface.CertificatesRepositoryProtocol
import com.example.moodlegovapp.core.domain.models.Certificate
class CertificatesRepository(
    private val api: ApiServiceProtocol
) : CertificatesRepositoryProtocol {

    override suspend fun getCertificates(): AppResult<List<Certificate>> = api.getCertificates()
    override suspend fun getDownloadUrl(certificateId: Int): AppResult<String>    = api.getCertificateDownloadUrl(certificateId)
}