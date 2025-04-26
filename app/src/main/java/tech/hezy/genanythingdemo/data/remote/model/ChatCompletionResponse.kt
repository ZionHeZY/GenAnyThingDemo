package tech.hezy.genanythingdemo.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatCompletionResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("object") val obj: String?,
    @SerializedName("created") val created: Long?,
    @SerializedName("model") val model: String?,
    @SerializedName("choices") val choices: List<Choice>?
) {
    data class Choice(
        @SerializedName("index") val index: Int?,
        @SerializedName("message") val message: Message?,
        @SerializedName("finish_reason") val finishReason: String?
    )
}
