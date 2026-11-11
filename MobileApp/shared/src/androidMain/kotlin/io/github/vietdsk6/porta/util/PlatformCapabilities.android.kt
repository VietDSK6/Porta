package io.github.vietdsk6.porta.util

actual fun getPlatformCapabilities(): PlatformCapabilities = PlatformCapabilities(
    supportsStudio = false,
    supportsEvidenceFiles = true,
    supportsDragDrop = false,
)
