package io.linkio.android

data class LinkIOConfig(
    val domain: String,
    val backendURL: String,
    val autoCheckPendingLinks: Boolean = true
)
