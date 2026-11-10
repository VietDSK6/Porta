package io.github.vietdsk6.porta.util.file

import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download

class FileManagerImpl : FileManager {
    override suspend fun saveTextByPickingUpLocation(
        suggestedName: String,
        extension: String,
        content: String,
    ): Result<Boolean> = runCatching {
        FileKit.download(
            bytes = content.encodeToByteArray(),
            fileName = "$suggestedName.$extension",
        )
        true
    }

    override suspend fun storeEvidenceAttachment(
        profileId: String,
        selectedFile: SelectedEvidenceFile,
    ): Result<EvidenceAttachmentMetadata> = Result.failure(
        UnsupportedOperationException("Local evidence attachments are available on Android and Desktop."),
    )

    override suspend fun deleteEvidenceAttachment(attachment: EvidenceAttachmentMetadata): Result<Unit> = Result.success(Unit)

    override suspend fun openEvidenceAttachment(attachment: EvidenceAttachmentMetadata): Result<Boolean> = Result.success(false)

    override suspend fun clearEvidenceAttachments(profileId: String?): Result<Unit> = Result.success(Unit)
}
