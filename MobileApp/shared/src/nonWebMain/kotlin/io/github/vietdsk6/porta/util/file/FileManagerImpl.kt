package io.github.vietdsk6.porta.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.writeString

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
}
