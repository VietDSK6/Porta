package io.github.vietdsk6.porta.util

actual fun getPlatformCapabilities(): PlatformCapabilities = PlatformCapabilities(
    supportsStudio = true,
    supportsEvidenceFiles = true,
    supportsDragDrop = true,
)
