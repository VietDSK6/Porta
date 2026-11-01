package io.github.vietdsk6.porta.util.file

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
}
