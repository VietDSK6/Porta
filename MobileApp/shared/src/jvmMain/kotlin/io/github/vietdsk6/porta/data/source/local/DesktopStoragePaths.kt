package io.github.vietdsk6.porta.data.source.local

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Places Desktop data in the operating system's user-data directory and preserves older installs. */
internal object DesktopStoragePaths {
    private const val LEGACY_APP_DIRECTORY_NAME = ".io.github.vietdsk6.porta"
    private const val LEGACY_TEMP_DIRECTORY_NAME = "io.github.vietdsk6.porta"

    fun appDataDirectory(
        userHome: File = File(System.getProperty("user.home")),
        osName: String = System.getProperty("os.name"),
        environment: Map<String, String> = System.getenv(),
    ): File {
        val directory = when {
            osName.startsWith("Mac", ignoreCase = true) ->
                File(userHome, "Library/Application Support/Porta")

            osName.startsWith("Windows", ignoreCase = true) ->
                File(environment["APPDATA"].takeUnless { it.isNullOrBlank() } ?: File(userHome, "AppData/Roaming").path, "Porta")

            else -> {
                val xdgDataHome = environment["XDG_DATA_HOME"]
                    ?.takeIf(String::isNotBlank)
                    ?.let(::File)
                    ?.takeIf(File::isAbsolute)
                File(xdgDataHome ?: File(userHome, ".local/share"), "porta")
            }
        }
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create Porta data directory: $directory" }
        return directory
    }

    fun databaseFile(
        fileName: String,
        userHome: File = File(System.getProperty("user.home")),
        temporaryDirectory: File = File(System.getProperty("java.io.tmpdir")),
        osName: String = System.getProperty("os.name"),
        environment: Map<String, String> = System.getenv(),
    ): File {
        val destination = File(appDataDirectory(userHome, osName, environment), fileName)
        if (destination.exists()) return destination

        val legacyHome = File(File(userHome, LEGACY_APP_DIRECTORY_NAME), fileName)
        val legacyTemporary = File(File(temporaryDirectory, LEGACY_TEMP_DIRECTORY_NAME), fileName)
        val source = listOf(legacyHome, legacyTemporary).firstOrNull(File::exists) ?: return destination
        return migrateFile(source, destination, companions = listOf("-wal", "-shm"))
    }

    fun preferencesFile(
        fileName: String,
        userHome: File = File(System.getProperty("user.home")),
        osName: String = System.getProperty("os.name"),
        environment: Map<String, String> = System.getenv(),
    ): File {
        val destination = File(appDataDirectory(userHome, osName, environment), fileName)
        if (destination.exists()) return destination
        val legacy = File(File(userHome, LEGACY_APP_DIRECTORY_NAME), fileName)
        return if (legacy.exists()) migrateFile(legacy, destination) else destination
    }

    private fun migrateFile(source: File, destination: File, companions: List<String> = emptyList()): File {
        val staged = mutableListOf<Pair<File, File>>()
        return try {
            (companions.filter { suffix -> File(source.absolutePath + suffix).exists() } + "").forEach { suffix ->
                val destinationFile = File(destination.absolutePath + suffix)
                val stageFile = File(destinationFile.absolutePath + ".porta-migrating")
                File(source.absolutePath + suffix).copyTo(stageFile, overwrite = true)
                staged += stageFile to destinationFile
            }
            // The main file is moved last: its presence marks a complete migration on the next launch.
            staged.forEach { (stageFile, destinationFile) ->
                Files.move(stageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            destination
        } catch (_: Exception) {
            // Keep the old installation readable if any part of the copy fails.
            source
        } finally {
            staged.forEach { (stageFile, _) -> stageFile.delete() }
        }
    }
}
