package io.github.vietdsk6.porta.util.file

import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.writeString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FileManagerImpl : FileManager {
    override suspend fun saveTextByPickingUpLocation(
        suggestedName: String,
        extension: String,
        content: String,
    ): Result<Boolean> = runCatching {
        val destination = FileKit.openFileSaver(
            suggestedName = suggestedName,
            defaultExtension = extension,
            allowedExtensions = setOf(extension),
        ) ?: return@runCatching false
        destination.writeString(content)
        true
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun storeEvidenceAttachment(
        profileId: String,
        selectedFile: SelectedEvidenceFile,
    ): Result<EvidenceAttachmentMetadata> = runCatching {
        require(profileId.isNotBlank()) { "A profile is required to store evidence." }
        require(profileId.length <= MAX_PROFILE_ID_LENGTH) { "The profile identifier is too long." }
        EvidenceAttachmentPolicy.validate(selectedFile).getOrThrow()

        val displayName = EvidenceAttachmentPolicy.normalizedDisplayName(selectedFile.displayName)
        val extension = displayName.substringAfterLast('.').lowercase()
        val profileDirectoryName = profileDirectoryName(profileId)
        val storageKey = "$profileDirectoryName/${Uuid.random()}.$extension"
        val profileDirectory = evidenceRoot().resolve(profileDirectoryName)
        profileDirectory.createDirectories()
        val destination = evidenceRoot().resolve(storageKey)

        try {
            destination.write(selectedFile.bytes)
        } catch (error: Throwable) {
            runCatching { destination.delete(mustExist = false) }
            throw error
        }

        EvidenceAttachmentMetadata(
            displayName = displayName,
            mimeType = selectedFile.mimeType?.substringBefore(';')?.trim()?.takeIf(String::isNotBlank),
            byteSize = selectedFile.bytes.size.toLong(),
            storageKey = storageKey,
        )
    }

    override suspend fun deleteEvidenceAttachment(attachment: EvidenceAttachmentMetadata): Result<Unit> = runCatching {
        val file = resolveStoredAttachment(attachment.storageKey) ?: return@runCatching
        file.delete(mustExist = false)
    }

    override suspend fun openEvidenceAttachment(attachment: EvidenceAttachmentMetadata): Result<Boolean> = runCatching {
        val file = resolveStoredAttachment(attachment.storageKey) ?: return@runCatching false
        if (!file.exists() || !file.isRegularFile()) return@runCatching false
        FileKit.openFileWithDefaultApplication(file)
        true
    }

    override suspend fun clearEvidenceAttachments(profileId: String?): Result<Unit> = runCatching {
        val target = if (profileId == null) {
            evidenceRoot()
        } else {
            require(profileId.isNotBlank()) { "A profile is required to clear evidence." }
            require(profileId.length <= MAX_PROFILE_ID_LENGTH) { "The profile identifier is too long." }
            evidenceRoot().resolve(profileDirectoryName(profileId))
        }
        target.deleteRecursivelyIfPresent()
    }

    private fun evidenceRoot(): PlatformFile = FileKit.filesDir.resolve(EVIDENCE_DIRECTORY)

    private fun resolveStoredAttachment(storageKey: String?): PlatformFile? {
        if (storageKey == null) return null
        require(STORAGE_KEY_PATTERN.matches(storageKey)) { "Invalid evidence storage key." }
        return evidenceRoot().resolve(storageKey)
    }

    private fun profileDirectoryName(profileId: String): String {
        val readablePrefix = profileId
            .lowercase()
            .map { character ->
                if (character in 'a'..'z' || character in '0'..'9' || character == '-' || character == '_') {
                    character
                } else {
                    '_'
                }
            }
            .joinToString(separator = "")
            .trim('_')
            .ifBlank { "profile" }
            .take(MAX_PROFILE_PREFIX_LENGTH)
        val stableSuffix = profileId.hashCode().toUInt().toString(radix = 16)
        return "$readablePrefix-$stableSuffix"
    }

    private suspend fun PlatformFile.deleteRecursivelyIfPresent() {
        if (!exists()) return
        if (isDirectory()) {
            list().forEach { child -> child.deleteRecursivelyIfPresent() }
        }
        delete(mustExist = false)
    }

    private companion object {
        const val EVIDENCE_DIRECTORY = "evidence"
        const val MAX_PROFILE_ID_LENGTH = 128
        const val MAX_PROFILE_PREFIX_LENGTH = 48
        val STORAGE_KEY_PATTERN = Regex("[a-z0-9_-]+/[0-9a-f-]+\\.[a-z0-9]+")
    }
}
