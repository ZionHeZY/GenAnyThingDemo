package tech.hezy.genanythingdemo.presentation

import android.webkit.JavascriptInterface
import tech.hezy.genanythingdemo.domain.usecase.JsBridgeUseCase

class JsBridge(private val useCase: JsBridgeUseCase) {
    @JavascriptInterface fun getDeviceInfo(): String = useCase.getDeviceInfo()
    @JavascriptInterface fun vibrate(millis: Long) = useCase.vibrate(millis)
    @JavascriptInterface fun showToast(message: String) = useCase.showToast(message)
    @JavascriptInterface fun openSettings() = useCase.openSettings()
    @JavascriptInterface fun dialPhone(number: String) = useCase.dialPhone(number)
    @JavascriptInterface fun ring() = useCase.ring()
    @JavascriptInterface fun toggleFlashlight(on: Boolean) = useCase.toggleFlashlight(on)
}
