package tech.hezy.genanythingdemo.domain.usecase

interface JsBridgeUseCase {
    fun getDeviceInfo(): String
    fun vibrate(millis: Long)
    fun showToast(message: String)
    fun openSettings()
    fun dialPhone(number: String)
    fun ring()
    fun toggleFlashlight(on: Boolean)
}
