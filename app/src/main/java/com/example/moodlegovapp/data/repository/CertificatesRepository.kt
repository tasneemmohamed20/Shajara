package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.repositoryinterface.CertificatesRepositoryProtocol
import com.example.moodlegovapp.domain.models.Certificate

class CertificatesRepository(
    private val api: ApiServiceProtocol
) : CertificatesRepositoryProtocol {

    override suspend fun getCertificates(): AppResult<List<Certificate>> = api.getCertificates()
    override suspend fun getDownloadUrl(certificateId: Int): AppResult<String> = api.getCertificateDownloadUrl(certificateId)
}