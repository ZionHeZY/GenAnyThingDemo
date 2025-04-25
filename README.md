# GenAnyThingDemo

一个基于 Clean 架构的 Android AI 网页生成演示项目，支持 WebView 与原生能力的 JS 桥接。

## 目录结构

```
app/
 └─ src/main/java/tech/hezy/genanythingdemo/
      ├─ domain/
      │    └─ usecase/
      │         └─ JsBridgeUseCase.kt
      ├─ data/
      │    └─ remote/
      │         └─ JsBridgeImpl.kt
      ├─ presentation/
      │    ├─ JsBridge.kt
      │    └─ viewmodel/
      │         └─ HtmlGenViewModel.kt
      ├─ HtmlGenActivity.kt
      └─ data/remote/GeminiApi.kt
```

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

## 接口说明

- **domain/usecase/JsBridgeUseCase.kt**：定义所有可供 WebView JS 调用的原生能力接口。
- **data/remote/JsBridgeImpl.kt**：实现 JsBridgeUseCase，封装所有原生 API 调用。
- **presentation/JsBridge.kt**：通过 @JavascriptInterface 暴露所有能力给 WebView。
- **HtmlGenActivity.kt**：负责依赖注入、WebView 配置、UI 交互。

---
如需扩展原生能力，只需在 UseCase、Impl、JsBridge 各加一行即可。
