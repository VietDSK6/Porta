package io.github.vietdsk6.porta.util.file

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvidenceAttachmentPolicyTest {
    @Test
    fun acceptsCommonEvidenceFileAndNormalizesDisplayName() {
        val selectedFile = SelectedEvidenceFile(
            displayName = "folder/transcript.PDF",
            mimeType = "application/pdf",
            bytes = byteArrayOf(1),
        )

        assertTrue(EvidenceAttachmentPolicy.validate(selectedFile).isSuccess)
        assertEquals("transcript.PDF", EvidenceAttachmentPolicy.normalizedDisplayName(selectedFile.displayName))
    }

    @Test
    fun rejectsUnsupportedExtension() {
        val result = EvidenceAttachmentPolicy.validate(
            SelectedEvidenceFile(
                displayName = "proof.zip",
                mimeType = "application/zip",
                bytes = byteArrayOf(1),
            ),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Unsupported") == true)
    }

    @Test
    fun rejectsMimeTypeThatDoesNotMatchExtension() {
        val result = EvidenceAttachmentPolicy.validate(
            SelectedEvidenceFile(
                displayName = "proof.pdf",
                mimeType = "image/png",
                bytes = byteArrayOf(1),
            ),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("does not match") == true)
    }

    @Test
    fun rejectsEmptyFile() {
        val result = EvidenceAttachmentPolicy.validate(
            SelectedEvidenceFile(
                displayName = "proof.jpg",
                mimeType = "image/jpeg",
                bytes = byteArrayOf(),
            ),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("empty") == true)
    }

    @Test
    fun rejectsFileLargerThanTenMegabytes() {
        val result = EvidenceAttachmentPolicy.validate(
            SelectedEvidenceFile(
                displayName = "oversized.pdf",
                mimeType = "application/pdf",
                bytes = ByteArray((EvidenceAttachmentPolicy.MAX_BYTE_SIZE + 1L).toInt()),
            ),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("10 MB") == true)
    }
}
