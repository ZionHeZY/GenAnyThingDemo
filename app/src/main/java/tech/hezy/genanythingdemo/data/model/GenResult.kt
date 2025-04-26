package tech.hezy.genanythingdemo.data.model

data class GenResult(
    val id: Int = 0,
    val prompt: String,
    val resultHtml: String,
    val platformType: ApiType,
    val params: GenParams,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String>? = null
)
