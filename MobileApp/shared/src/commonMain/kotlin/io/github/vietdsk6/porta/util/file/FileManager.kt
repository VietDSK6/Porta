package io.github.vietdsk6.porta.util.file

import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata

data class SelectedEvidenceFile(
    val displayName: String,
    val mimeType: String? = null,
    val bytes: ByteArray,
)

object EvidenceAttachmentPolicy {
    const val MAX_BYTE_SIZE: Long = 10L * 1024L * 1024L

    val allowedExtensions: Set<String> = setOf(
        "doc",
        "docx",
        "heic",
        "heif",
        "jpeg",
        "jpg",
        "pdf",
        "png",
        "txt",
        "webp",
    )

    fun validate(file: SelectedEvidenceFile): Result<Unit> = runCatching {
        val displayName = normalizedDisplayName(file.displayName)
        require(displayName.isNotBlank()) { "The evidence file must have a name." }
        require(file.bytes.isNotEmpty()) { "The evidence file is empty." }
        require(file.bytes.size.toLong() <= MAX_BYTE_SIZE) {
            "The evidence file must be 10 MB or smaller."
        }

        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        require(extension in allowedExtensions) {
            "Unsupported evidence file type. Choose an image, PDF, text, or Word document."
        }

        val normalizedMimeType = file.mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            ?.takeIf(String::isNotBlank)
        require(normalizedMimeType == null || isMimeTypeAllowed(normalizedMimeType, extension)) {
            "The evidence file type does not match its file name."
        }
    }

    fun normalizedDisplayName(displayName: String): String = displayName
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .trim()

    private fun isMimeTypeAllowed(mimeType: String, extension: String): Boolean = when (extension) {
        "doc" -> mimeType == "application/msword" || mimeType == GENERIC_BINARY_MIME_TYPE
        "docx" -> mimeType == DOCX_MIME_TYPE || mimeType == GENERIC_BINARY_MIME_TYPE
        "heic" -> mimeType == "image/heic" || mimeType == "image/heif" || mimeType == GENERIC_BINARY_MIME_TYPE
        "heif" -> mimeType == "image/heif" || mimeType == "image/heic" || mimeType == GENERIC_BINARY_MIME_TYPE
        "jpeg", "jpg" -> mimeType == "image/jpeg" || mimeType == GENERIC_BINARY_MIME_TYPE
        "pdf" -> mimeType == "application/pdf" || mimeType == GENERIC_BINARY_MIME_TYPE
        "png" -> mimeType == "image/png" || mimeType == GENERIC_BINARY_MIME_TYPE
        "txt" -> mimeType == "text/plain" || mimeType == GENERIC_BINARY_MIME_TYPE
        "webp" -> mimeType == "image/webp" || mimeType == GENERIC_BINARY_MIME_TYPE
        else -> false
    }

    private const val DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    private const val GENERIC_BINARY_MIME_TYPE = "application/octet-stream"
}

/** File export and app-private evidence attachment operations used by Porta. */
interface FileManager {
    suspend fun saveTextByPickingUpLocation(
        suggestedName: String,
        extension: String,
        content: String,
    ): Result<Boolean>

    suspend fun storeEvidenceAttachment(
        profileId: String,
        selectedFile: SelectedEvidenceFile,
    ): Result<EvidenceAttachmentMetadata> = Result.failure(
        UnsupportedOperationException("Local evidence attachments are not available on this platform."),
    )

    suspend fun deleteEvidenceAttachment(attachment: EvidenceAttachmentMetadata): Result<Unit> = Result.success(Unit)

    suspend fun openEvidenceAttachment(attachment: EvidenceAttachmentMetadata): Result<Boolean> = Result.success(false)

    suspend fun clearEvidenceAttachments(profileId: String? = null): Result<Unit> = Result.success(Unit)
}
