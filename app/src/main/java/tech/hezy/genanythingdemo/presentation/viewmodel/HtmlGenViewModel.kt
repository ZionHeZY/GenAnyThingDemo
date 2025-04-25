package tech.hezy.genanythingdemo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.hezy.genanythingdemo.data.remote.GeminiApi

sealed interface HtmlGenUiState {
    object Initial : HtmlGenUiState
    object Loading : HtmlGenUiState
    data class Success(val html: String, val analysis: String) : HtmlGenUiState
    data class Error(val message: String) : HtmlGenUiState
}

class HtmlGenViewModel(private val geminiApi: GeminiApi) : ViewModel() {
    private val _uiState = MutableStateFlow<HtmlGenUiState>(HtmlGenUiState.Initial)
    val uiState: StateFlow<HtmlGenUiState> = _uiState.asStateFlow()

    fun generateHtml(userPrompt: String) {
        _uiState.value = HtmlGenUiState.Loading
        viewModelScope.launch {
            try {
                val text = geminiApi.requestHtml(userPrompt)
                val html = extractHtmlFromMarkdown(text)
                _uiState.value = HtmlGenUiState.Success(html, "")
            } catch (e: Exception) {
                _uiState.value = HtmlGenUiState.Error(e.localizedMessage ?: e.toString())
            }
        }
    }

    private fun extractHtmlFromMarkdown(text: String): String {
        val regex = Regex("```html\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        return match?.groups?.get(1)?.value?.trim()
            ?: text.replace(Regex("```[\\s\\S]*?```"), "").trim().ifBlank { text.trim() }
    }
}
