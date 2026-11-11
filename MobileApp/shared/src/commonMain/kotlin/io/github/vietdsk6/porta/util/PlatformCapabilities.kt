package io.github.vietdsk6.porta.util

/** Capabilities used by shared UI instead of checking a platform name at runtime. */
data class PlatformCapabilities(
    val supportsStudio: Boolean,
    val supportsEvidenceFiles: Boolean,
    val supportsDragDrop: Boolean,
)

expect fun getPlatformCapabilities(): PlatformCapabilities
