package tech.hezy.genanythingdemo.data.remote

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.hezy.genanythingdemo.data.model.*
import tech.hezy.genanythingdemo.data.remote.api.ArkApiService
import tech.hezy.genanythingdemo.data.remote.model.ChatCompletionRequest
import tech.hezy.genanythingdemo.data.remote.model.ChatCompletionResponse
import tech.hezy.genanythingdemo.data.remote.SystemPrompts
import tech.hezy.genanythingdemo.domain.repository.ApiState
import tech.hezy.genanythingdemo.domain.repository.WebAppGenRepository
import java.util.concurrent.TimeUnit

class ArkRemoteDataSource(private val apiKey: String) : WebAppGenRepository {
    private val baseUrl = "https://ark.cn-beijing.volces.com"

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val api = retrofit.create(ArkApiService::class.java)

    override suspend fun generateWebApp(
        platform: Platform,
        prompt: String,
        params: GenParams
    ): Flow<ApiState<GenResult>> = flow {
        emit(ApiState.Loading)
        val systemPrompt = params.systemPrompt ?: SystemPrompts.HTML_GEN
        val messages = listOf(
            tech.hezy.genanythingdemo.data.remote.model.Message(role = "system", content = systemPrompt),
            tech.hezy.genanythingdemo.data.remote.model.Message(role = "user", content = prompt)
        )
        val request = ChatCompletionRequest(
            model = params.model ?: "deepseek-v3-250324",
            messages = messages,
            temperature = params.temperature,
            top_p = params.topP,
            stream = false,
            max_tokens = null
        )
        try {
            val response = api.createChatCompletion(request)
            if (response.isSuccessful) {
                val body = response.body()
                val html = body?.choices?.firstOrNull()?.message?.content ?: ""
                emit(ApiState.Success(
                    GenResult(
                        prompt = prompt,
                        resultHtml = html,
                        platformType = platform.name,
                        params = params
                    )
                ))
            } else {
                Log.e("ArkRemoteDataSource", "API错误: ${response.code()} ${response.message()} ${response.errorBody()?.string()}")
                emit(ApiState.Error(Exception("API错误: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e("ArkRemoteDataSource", "AI生成报错：${e.message}", e)
            emit(ApiState.Error(e))
        }
    }

    override suspend fun fetchGenHistory(): List<GenResult> = emptyList()
    override suspend fun saveGenResult(result: GenResult) { }
}
