# GenAnyThingDemo

AI 原生网页生成演示项目，支持 Gemini、DeepSeek、ARK 多大模型接入，支持 JS 与原生能力桥接。

## 最新特性
- 支持 Gemini、DeepSeek、ARK 三大 AI 平台一键切换
- 所有平台 API Key 统一通过
- 支持自动适配移动端 WebView，生成网页自适应所有设备
- 支持 JS 调用原生能力（震动、闪光灯、响铃、拨号、Toast、设备信息等）
- UI 卡片风格统一，体验简洁美观
- 错误信息详细展示，便于排查

## 目录结构
```
app/
 └─ src/main/java/tech/hezy/genanythingdemo/
      ├─ domain/
      │    └─ usecase/
      │         └─ JsBridgeUseCase.kt
      ├─ data/
      │    └─ remote/
      │         ├─ GeminiApi.kt
      │         ├─ DeepSeekRemoteDataSource.kt
      │         ├─ ArkRemoteDataSource.kt
      │         ├─ api/
      │         │    ├─ GeminiApi.kt
      │         │    ├─ DeepSeekApiService.kt
      │         │    └─ ArkApiService.kt
      │         └─ JsBridgeImpl.kt
      ├─ presentation/
      │    ├─ JsBridge.kt
      │    └─ viewmodel/
      │         └─ HtmlGenViewModel.kt
      ├─ HtmlGenActivity.kt
      └─ res/layout/
           ├─ activity_html_gen.xml
           ├─ webview_card.xml
           └─ ...
```

## 快速开始

1. **克隆项目**
2. **配置 local.properties**（不要提交到 git）：
   ```
   geminiApiKey=你的-gemini-api-key
   deepseekApiKey=你的-deepseek-api-key
   arkApiKey=你的-ark-api-key
   ```
3. **编译运行即可**

## AI 平台切换
- 下拉选择 Gemini / DeepSeek / ARK，自动切换大模型。
- 所有平台 API Key 均通过 BuildConfig 自动注入，安全可靠。

## 网页自适应说明
- 所有生成页面自动插入 viewport 和响应式样式，移动端无横向滚动。

## JS 原生桥接 API 列表
所有方法均可在 H5 页面通过 `window.AndroidBridge` 调用：

| 方法名 | 说明 | 示例 |
|--------|------|------|
| getDeviceInfo() | 获取设备信息，返回字符串 | `window.AndroidBridge.getDeviceInfo()` |
| vibrate(millis) | 震动指定毫秒数 | `window.AndroidBridge.vibrate(200)` |
| showToast(message) | 显示短暂提示 | `window.AndroidBridge.showToast('Hello!')` |
| openSettings() | 跳转系统设置页面 | `window.AndroidBridge.openSettings()` |
| dialPhone(number) | 跳转拨号界面并填充号码 | `window.AndroidBridge.dialPhone('10086')` |
| ring() | 切换响铃模式并响铃 | `window.AndroidBridge.ring()` |
| toggleFlashlight(on) | 打开/关闭闪光灯 | `window.AndroidBridge.toggleFlashlight(true)` |

调用建议：
```js
if (window.AndroidBridge) {
  window.AndroidBridge.vibrate(200);
  window.AndroidBridge.showToast('Hello!');
}
```

## 架构说明
- **HtmlGenActivity.kt**：主界面，负责 UI 逻辑与 WebView 展示
- **HtmlGenViewModel.kt**：核心业务逻辑，管理平台切换与生成流程
- **GeminiApi.kt / DeepSeekRemoteDataSource.kt / ArkRemoteDataSource.kt**：各平台 API 封装
- **SystemPrompts.kt**：系统提示词，已内置 JS 与原生桥接说明
- **JsBridgeUseCase.kt / JsBridgeImpl.kt / JsBridge.kt**：原生能力暴露与 JS 桥接
- **res/layout/**：所有 UI 卡片风格统一，适配移动端

## 常见问题
- **API Key 配置无效/报错**：请确认 local.properties 配置正确，未提交到 git。
- **网页宽度溢出**：已自动修正为响应式布局，如仍有问题请反馈。
- **AI 生成异常**：请检查网络、API Key、模型配额，或在 Issues 留言。
