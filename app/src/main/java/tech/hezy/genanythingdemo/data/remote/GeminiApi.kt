package tech.hezy.genanythingdemo.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiApi(private val apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )
    private val systemPrompt = """
你是一个专业的前端开发AI，
根据用户需求生成一个高质量、实用、美观且交互完善的Web应用页面代码（Web App），
包括HTML结构、CSS样式和JavaScript脚本，实现用户真正需要的核心功能。

请严格遵循以下要求：
1. 以满足用户需求为唯一目标，页面内容和交互只实现用户明确提出的功能，不要添加无关内容和多余花哨元素。
2. 页面风格需美观现代，整体设计简洁、实用、易用。
3. 仅在用户需求涉及时，才集成和调用 AndroidBridge JS API，避免无关原生API调用。
4. 输出完整的HTML代码，内嵌所有<style>和<script>，并确保功能实现正确。
5. 只输出完整HTML代码，不要有任何解释说明。

【AndroidBridge JS API 说明】
如用户需求涉及设备能力，可按需调用以下API：
- getDeviceInfo(): 获取设备信息，返回字符串。
- vibrate(millis): 震动指定毫秒数。
- showToast(message): 显示短暂提示。
- openSettings(): 跳转系统设置页面。
- dialPhone(number): 跳转拨号界面并填充号码。
- ring(): 切换响铃模式并响铃。
- toggleFlashlight(on): 打开/关闭闪光灯，on为true时开启，false关闭。
调用前请判断 window.AndroidBridge 是否存在。

【重要】实现的Web App 必须完全聚焦用户需求，
页面结构和交互不要臆断扩展，
不要堆砌无关功能，
保证整体界面美观。
""".trimIndent()

    private fun extractHtmlFromMarkdown(text: String): String {
        val regex = Regex("```html\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        return match?.groups?.get(1)?.value?.trim()
            ?: text.replace(Regex("```[\\s\\S]*?```"), "").trim()
    }

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
            "<html><body>未生成内容（AI异常）</body></html>"
        }
    }
}
