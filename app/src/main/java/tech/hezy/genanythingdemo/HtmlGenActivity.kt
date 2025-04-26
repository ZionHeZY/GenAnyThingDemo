package tech.hezy.genanythingdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.hezy.genanythingdemo.data.remote.ArkRemoteDataSource
import tech.hezy.genanythingdemo.data.remote.DeepSeekRemoteDataSource
import tech.hezy.genanythingdemo.data.remote.api.GeminiApi
import tech.hezy.genanythingdemo.data.remote.JsBridgeImpl
import tech.hezy.genanythingdemo.data.model.ApiType
import tech.hezy.genanythingdemo.data.model.GenResult
import tech.hezy.genanythingdemo.presentation.JsBridge
import tech.hezy.genanythingdemo.presentation.viewmodel.GenModelType
import tech.hezy.genanythingdemo.presentation.viewmodel.HtmlGenUiState
import tech.hezy.genanythingdemo.presentation.viewmodel.HtmlGenViewModel

class HtmlGenActivity : ComponentActivity() {
    private lateinit var viewModel: HtmlGenViewModel
    private lateinit var spinnerModel: Spinner
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_gen)

        val geminiApiKey = BuildConfig.apiKey
        val deepSeekApiKey = BuildConfig.deepseekApiKey
        val arkApiKey = BuildConfig.arkApiKey
        val geminiApi = GeminiApi(geminiApiKey)
        val deepSeekApi = DeepSeekRemoteDataSource(deepSeekApiKey)
        val arkApi = ArkRemoteDataSource(arkApiKey)
        viewModel = HtmlGenViewModel(geminiApi, deepSeekApi, arkApi)

        val etPrompt = findViewById<EditText>(R.id.et_prompt)
        val btnGenerate = findViewById<Button>(R.id.btn_generate)
        val tvInfo = findViewById<TextView>(R.id.tv_info)
        val scrollInfo = findViewById<ScrollView>(R.id.scroll_info)
        spinnerModel = findViewById(R.id.spinner_model)
        webView = findViewById(R.id.webview_card_webview)

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

        // 下拉选择模型
        val modelAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Gemini", "DeepSeek", "ARK")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerModel.adapter = modelAdapter
        spinnerModel.setSelection(0)
        spinnerModel.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                viewModel.setModel(
                    when (position) {
                        0 -> GenModelType.GEMINI
                        1 -> GenModelType.DEEPSEEK
                        2 -> GenModelType.ARK
                        else -> GenModelType.GEMINI
                    }
                )
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        btnGenerate.setOnClickListener {
            val prompt = etPrompt.text.toString()
            viewModel.generateHtmlSinglePlatform(prompt)
        }

        etPrompt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                btnGenerate.performClick()
                true
            } else {
                false
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HtmlGenUiState.Success -> {
                        loadHtmlToWebView(state.result.resultHtml)
                    }
                    is HtmlGenUiState.Loading -> {
                        webView.loadDataWithBaseURL(null, "<html><body>加载中...</body></html>", "text/html", "utf-8", null)
                    }
                    is HtmlGenUiState.Error -> {
                        webView.loadDataWithBaseURL(null, "<html><body style='color:#c00;'>${state.message}</body></html>", "text/html", "utf-8", null)
                    }
                    is HtmlGenUiState.Initial -> {
                        tvInfo.text = "请描述你想要的页面，点击生成按钮"
                        clearWebView()
                    }
                }
                scrollInfo.post { scrollInfo.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    // 自动适配HTML内容，插入viewport和自适应样式
    private fun adaptHtmlForWebView(html: String): String {
        val hasViewport = html.contains("name=\"viewport\"")
        val viewport = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\">"
        val style = """
            <style>
            html,body{max-width:100vw;overflow-x:hidden;box-sizing:border-box;}
            *{box-sizing:border-box!important;}
            img,table,pre,code,.container,.content{max-width:100%!important;width:auto!important;box-sizing:border-box!important;}
            </style>
        """.trimIndent()
        return if (html.contains("<head>")) {
            html.replace("<head>", "<head>\n${if (!hasViewport) viewport else ""}\n$style\n")
        } else {
            "<head>$viewport$style</head>$html"
        }
    }

    // WebView 加载内容时调用适配方法
    private fun loadHtmlToWebView(rawHtml: String) {
        val html = adaptHtmlForWebView(rawHtml)
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }

    private fun clearWebView() {
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
    }
}
