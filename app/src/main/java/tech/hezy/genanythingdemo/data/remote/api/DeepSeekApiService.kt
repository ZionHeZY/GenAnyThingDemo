package tech.hezy.genanythingdemo.data.remote.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Response
import tech.hezy.genanythingdemo.data.remote.model.ChatCompletionRequest
import tech.hezy.genanythingdemo.data.remote.model.ChatCompletionResponse

interface DeepSeekApiService {
    @Headers("Content-Type: application/json")
    @POST("/v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}
