package tech.hezy.genanythingdemo.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hezy.genanythingdemo.data.model.*

sealed class ApiState<out T> {
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Error(val throwable: Throwable) : ApiState<Nothing>()
}

interface WebAppGenRepository {
    suspend fun generateWebApp(
        platform: Platform,
        prompt: String,
        params: GenParams
    ): Flow<ApiState<GenResult>>
    suspend fun fetchGenHistory(): List<GenResult>
    suspend fun saveGenResult(result: GenResult)
}
