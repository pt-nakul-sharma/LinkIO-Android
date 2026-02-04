package io.linkio.android

data class LinkIOConfig(
    val domain: String,
    val backendURL: String,
    val appScheme: String? = null, // Custom URL scheme (e.g., "rokart" for rokart://)
    val autoCheckPendingLinks: Boolean = true
)
