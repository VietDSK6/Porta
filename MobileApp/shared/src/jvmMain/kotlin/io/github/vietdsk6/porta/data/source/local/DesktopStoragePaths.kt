package io.github.vietdsk6.porta.data.source.local

import java.io.File

internal object DesktopStoragePaths {
    private const val APP_DIRECTORY_NAME = ".io.github.vietdsk6.porta"

    fun appDataDirectory(userHome: File = File(System.getProperty("user.home"))): File = File(userHome, APP_DIRECTORY_NAME).apply { mkdirs() }

    fun databaseFile(
        fileName: String,
        userHome: File = File(System.getProperty("user.home")),
        temporaryDirectory: File = File(System.getProperty("java.io.tmpdir")),
    ): File {
        val persistentFile = File(appDataDirectory(userHome), fileName)
        val legacyDirectory = File(temporaryDirectory, "io.github.vietdsk6.porta")
        val legacyFile = File(legacyDirectory, fileName)
        if (persistentFile.exists() || !legacyFile.exists()) return persistentFile

        return runCatching {
            legacyFile.copyTo(persistentFile, overwrite = false)
            listOf("-wal", "-shm").forEach { suffix ->
                val legacyCompanion = File(legacyFile.absolutePath + suffix)
                val persistentCompanion = File(persistentFile.absolutePath + suffix)
                if (legacyCompanion.exists() && !persistentCompanion.exists()) {
                    legacyCompanion.copyTo(persistentCompanion, overwrite = false)
                }
            }
            persistentFile
        }.getOrElse {
            // Preserve access to an existing database if the one-time copy cannot be completed.
            legacyFile
        }
    }
}
