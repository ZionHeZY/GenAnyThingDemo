package tech.hezy.genanythingdemo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.hezy.genanythingdemo.data.model.*
import tech.hezy.genanythingdemo.data.remote.api.GeminiApi
import tech.hezy.genanythingdemo.domain.repository.ApiState
import tech.hezy.genanythingdemo.domain.repository.WebAppGenRepository

sealed interface HtmlGenUiState {
    object Initial : HtmlGenUiState
    object Loading : HtmlGenUiState
    data class Success(val result: GenResult, val platform: ApiType) : HtmlGenUiState
    data class Error(val message: String) : HtmlGenUiState
}

enum class GenModelType { GEMINI, DEEPSEEK, ARK }

class HtmlGenViewModel(
    private val geminiApi: GeminiApi,
    private val deepSeekRepo: WebAppGenRepository,
    private val arkRepo: WebAppGenRepository
) : ViewModel() {
    private val _selectedModel = MutableStateFlow(GenModelType.GEMINI)
    val selectedModel: StateFlow<GenModelType> = _selectedModel.asStateFlow()

    private val _uiState = MutableStateFlow<HtmlGenUiState>(HtmlGenUiState.Initial)
    val uiState: StateFlow<HtmlGenUiState> = _uiState.asStateFlow()

    fun setModel(model: GenModelType) {
        _selectedModel.value = model
    }

    fun generateHtmlSinglePlatform(userPrompt: String) {
        _uiState.value = HtmlGenUiState.Loading
        viewModelScope.launch {
            when (_selectedModel.value) {
                GenModelType.GEMINI -> {
                    try {
                        val text = geminiApi.requestHtml(userPrompt)
                        val html = extractHtmlFromMarkdown(text)
                        val result = GenResult(
                            prompt = userPrompt,
                            resultHtml = html,
                            platformType = ApiType.GEMINI,
                            params = GenParams("gemini-2.0-flash", null, null, null)
                        )
                        _uiState.value = HtmlGenUiState.Success(result, ApiType.GEMINI)
                    } catch (e: Exception) {
                        // 展示详细错误信息到 WebView
                        val errorHtml = """
                            <html><body style='color:#c00;font-size:15px;white-space:pre-wrap;'>
                            <b>Gemini 生成异常：</b><br/>${e.localizedMessage?.replace("\n", "<br/>") ?: e}<br/><br/>
                            <pre>${e.stackTraceToString().replace("\n", "<br/>")}</pre>
                            </body></html>
                        """.trimIndent()
                        val result = GenResult(
                            prompt = userPrompt,
                            resultHtml = errorHtml,
                            platformType = ApiType.GEMINI,
                            params = GenParams("gemini-2.0-flash", null, null, null)
                        )
                        _uiState.value = HtmlGenUiState.Success(result, ApiType.GEMINI)
                    }
                }
                GenModelType.DEEPSEEK -> {
                    val platform = Platform(
                        name = ApiType.DEEPSEEK,
                        apiUrl = "https://api.deepseek.com",
                        token = null,
                        model = "deepseek-chat",
                        temperature = 0.7f,
                        topP = 0.95f,
                        systemPrompt = null
                    )
                    deepSeekRepo.generateWebApp(
                        platform = platform,
                        prompt = userPrompt,
                        params = GenParams(
                            model = platform.model,
                            temperature = platform.temperature,
                            topP = platform.topP,
                            systemPrompt = platform.systemPrompt
                        )
                    ).collect { state ->
                        when (state) {
                            is ApiState.Success -> {
                                // 强制清理 markdown 标签和多余内容
                                val html = extractHtmlFromMarkdown(state.data.resultHtml)
                                val cleanResult = state.data.copy(resultHtml = html)
                                _uiState.value = HtmlGenUiState.Success(cleanResult, ApiType.DEEPSEEK)
                            }
                            is ApiState.Error -> {
                                _uiState.value = HtmlGenUiState.Error("DeepSeek: ${state.throwable.localizedMessage ?: state.throwable}")
                            }
                            else -> {}
                        }
                    }
                }
                GenModelType.ARK -> {
                    val platform = Platform(
                        name = ApiType.ARK,
                        apiUrl = "https://ark.cn-beijing.volces.com",
                        token = null,
                        model = "deepseek-v3-250324",
                        temperature = 0.7f,
                        topP = 0.95f,
                        systemPrompt = null
                    )
                    arkRepo.generateWebApp(
                        platform = platform,
                        prompt = userPrompt,
                        params = GenParams(
                            model = platform.model,
                            temperature = platform.temperature,
                            topP = platform.topP,
                            systemPrompt = platform.systemPrompt
                        )
                    ).collect { state ->
                        when (state) {
                            is ApiState.Success -> {
                                val html = extractHtmlFromMarkdown(state.data.resultHtml)
                                val cleanResult = state.data.copy(resultHtml = html)
                                _uiState.value = HtmlGenUiState.Success(cleanResult, ApiType.ARK)
                            }
                            is ApiState.Error -> {
                                _uiState.value = HtmlGenUiState.Error("ARK: ${state.throwable.localizedMessage ?: state.throwable}")
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun extractHtmlFromMarkdown(text: String): String {
        // 只提取 ```html ... ``` 之间的内容，否则返回原始内容
        val regex = Regex("```html\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        return match?.groups?.get(1)?.value?.trim()
            ?: text.replace(Regex("```[\\s\\S]*?```"), "").trim().ifBlank { text.trim() }
    }
}
