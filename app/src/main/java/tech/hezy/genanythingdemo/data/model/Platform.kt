package tech.hezy.genanythingdemo.data.model

enum class ApiType {
    GEMINI,
    DEEPSEEK,
    OPENAI,
    ANTHROPIC,
    GROQ,
    OLLAMA,
    ARK; 
    val displayName: String
        get() = when (this) {
            GEMINI -> "Gemini"
            DEEPSEEK -> "DeepSeek"
            OPENAI -> "OpenAI"
            ANTHROPIC -> "Anthropic"
            GROQ -> "Groq"
            OLLAMA -> "Ollama"
            ARK -> "ARK"
        }
}

data class Platform(
    val name: ApiType,
    val apiUrl: String,
    val token: String?,
    val model: String?,
    val temperature: Float?,
    val topP: Float?,
    val systemPrompt: String?,
    val enabled: Boolean = true,
    val selected: Boolean = false
)
