package tech.hezy.genanythingdemo.data.remote.api

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import tech.hezy.genanythingdemo.data.remote.SystemPrompts

class GeminiApi(private val apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )
    private val systemPrompt = SystemPrompts.HTML_GEN

    suspend fun requestHtml(userPrompt: String): String {
        Log.d("GeminiApi", "AI请求内容：系统提示词：$systemPrompt\n用户需求：$userPrompt")
        return try {
            val response = generativeModel.generateContent(
                content {
                    text(systemPrompt)
                    text(userPrompt)
                }
            )
            Log.d("GeminiApi", "AI输出内容：" + "Result===========" + response.text)
            response.text.toString()
        } catch (e: Exception) {
            Log.e("GeminiApi", "AI生成报错：${e.message}", e)
            // 详细错误内容直接返回，便于 WebView 展示
            """
            <html><body style='color:#c00;font-size:15px;white-space:pre-wrap;'>
            <b>Gemini 生成异常：</b><br/>${e.localizedMessage?.replace("\n", "<br/>") ?: e}<br/><br/>
            <pre>${e.stackTraceToString().replace("\n", "<br/>")}</pre>
            </body></html>
            """.trimIndent()
        }
    }
}