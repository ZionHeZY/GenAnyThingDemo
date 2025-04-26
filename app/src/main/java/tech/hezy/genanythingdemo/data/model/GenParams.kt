package tech.hezy.genanythingdemo.data.model

data class GenParams(
    val model: String?,
    val temperature: Float?,
    val topP: Float?,
    val systemPrompt: String?
)
