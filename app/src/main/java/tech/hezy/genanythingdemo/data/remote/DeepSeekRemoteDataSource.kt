package tech.hezy.genanythingdemo.data.remote

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.hezy.genanythingdemo.data.model.*
import tech.hezy.genanythingdemo.data.remote.api.DeepSeekApiService
import tech.hezy.genanythingdemo.data.remote.model.ChatCompletionRequest
import tech.hezy.genanythingdemo.data.remote.model.ChatCompletionResponse
import tech.hezy.genanythingdemo.data.remote.SystemPrompts
import tech.hezy.genanythingdemo.domain.repository.ApiState
import tech.hezy.genanythingdemo.domain.repository.WebAppGenRepository
import java.util.concurrent.TimeUnit

class DeepSeekRemoteDataSource(private val apiKey: String) : WebAppGenRepository {
    private val baseUrl = "https://api.deepseek.com"

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
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(DeepSeekApiService::class.java)

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
            model = params.model ?: "deepseek-chat",
            messages = messages,
            temperature = params.temperature ?: 0.7f,
            top_p = params.topP ?: 0.95f,
            stream = false,
            max_tokens = 2048
        )
        Log.d("DeepSeekRemoteDataSource", "[REQUEST] url=$baseUrl/v1/chat/completions body=${request}")
        try {
            val response = apiService.createChatCompletion(request)
            Log.d("DeepSeekRemoteDataSource", "[RESPONSE] code=${response.code()} message=${response.message()} body=${response.body()}")
            if (response.isSuccessful) {
                val result = response.body()?.choices?.firstOrNull()?.message?.content
                Log.d("DeepSeekRemoteDataSource", "AI输出内容：Result===========${result}")
                emit(ApiState.Success(
                    GenResult(
                        prompt = prompt,
                        resultHtml = result ?: "<html><body>未生成内容（AI异常）</body></html>",
                        platformType = platform.name,
                        params = params
                    )
                ))
            } else {
                Log.e("DeepSeekRemoteDataSource", "API错误: ${response.code()} ${response.message()} ${response.errorBody()?.string()}")
                emit(ApiState.Error(Exception("API错误: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e("DeepSeekRemoteDataSource", "AI生成报错：${e.message}", e)
            emit(ApiState.Error(e))
        }
    }

    // 历史管理可用本地数据库实现，这里仅作占位
    override suspend fun fetchGenHistory(): List<GenResult> = emptyList()
    override suspend fun saveGenResult(result: GenResult) { }
}
