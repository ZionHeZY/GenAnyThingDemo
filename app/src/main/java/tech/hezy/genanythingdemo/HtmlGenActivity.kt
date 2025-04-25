package tech.hezy.genanythingdemo

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.hezy.genanythingdemo.data.remote.GeminiApi
import tech.hezy.genanythingdemo.data.remote.JsBridgeImpl
import tech.hezy.genanythingdemo.presentation.JsBridge
import tech.hezy.genanythingdemo.presentation.viewmodel.HtmlGenUiState
import tech.hezy.genanythingdemo.presentation.viewmodel.HtmlGenViewModel

class HtmlGenActivity : ComponentActivity() {
    private lateinit var viewModel: HtmlGenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_gen)

        // 直接在Activity中创建ViewModel和GeminiApi（极简依赖链）
        val apiKey = BuildConfig.apiKey // 或直接写死你的key
        val geminiApi = GeminiApi(apiKey)
        viewModel = HtmlGenViewModel(geminiApi)

        val etPrompt = findViewById<EditText>(R.id.et_prompt)
        val btnGenerate = findViewById<Button>(R.id.btn_generate)
        val tvInfo = findViewById<TextView>(R.id.tv_info)
        val webView = findViewById<WebView>(R.id.webview)
        val scrollInfo = findViewById<ScrollView>(R.id.scroll_info)

        // 注入JS桥接
        val jsBridgeUseCase = JsBridgeImpl(this)
        val jsBridge = JsBridge(jsBridgeUseCase)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webView.addJavascriptInterface(jsBridge, "AndroidBridge")

        webView.webViewClient = WebViewClient()

        btnGenerate.setOnClickListener {
            val prompt = etPrompt.text.toString()
            viewModel.generateHtml(prompt)
        }

        etPrompt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnGenerate.performClick()
                true
            } else {
                false
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HtmlGenUiState.Loading -> {
                        tvInfo.text = "AI 正在生成页面，请稍候..."
                    }
                    is HtmlGenUiState.Error -> {
                        tvInfo.text = "错误：${state.message}"
                    }
                    is HtmlGenUiState.Success -> {
                        tvInfo.text = state.analysis
                        webView.loadDataWithBaseURL(null, state.html, "text/html", "utf-8", null)
                    }
                    is HtmlGenUiState.Initial -> {
                        tvInfo.text = "请描述你想要的页面，点击生成按钮"
                    }
                }
                scrollInfo.post { scrollInfo.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }
}
