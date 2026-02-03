package io.linkio.android

data class DeepLinkData(
    val url: String,
    val params: Map<String, String>,
    val isDeferred: Boolean
)
