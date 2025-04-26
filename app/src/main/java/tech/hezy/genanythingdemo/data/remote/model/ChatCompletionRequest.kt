package tech.hezy.genanythingdemo.data.remote.model

data class ChatCompletionRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Float? = null,
    val top_p: Float? = null,
    val stream: Boolean = false,
    val max_tokens: Int? = null
)
