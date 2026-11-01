package io.github.vietdsk6.porta.util.file

/** File export operations used by Porta's profile and opportunity-pack tools. */
interface FileManager {
    suspend fun saveTextByPickingUpLocation(
        suggestedName: String,
        extension: String,
        content: String,
    ): Result<Boolean>
}
