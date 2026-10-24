package io.github.vietdsk6.porta.data.source.local

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopStoragePathsTest {
    @Test
    fun `legacy temporary database is copied to persistent app data directory`() {
        val root = Files.createTempDirectory("porta-storage-test").toFile()
        try {
            val userHome = root.resolve("home").apply { mkdirs() }
            val temporaryDirectory = root.resolve("tmp").apply { mkdirs() }
            val legacyDirectory = temporaryDirectory.resolve("io.github.vietdsk6.porta").apply { mkdirs() }
            legacyDirectory.resolve("local_storage.db").writeText("legacy-db")
            legacyDirectory.resolve("local_storage.db-wal").writeText("legacy-wal")

            val result = DesktopStoragePaths.databaseFile(
                fileName = "local_storage.db",
                userHome = userHome,
                temporaryDirectory = temporaryDirectory,
            )

            assertEquals(userHome.resolve(".io.github.vietdsk6.porta/local_storage.db"), result)
            assertEquals("legacy-db", result.readText())
            assertEquals("legacy-wal", result.resolveSibling("local_storage.db-wal").readText())
            assertTrue(legacyDirectory.resolve("local_storage.db").exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `existing persistent database is never overwritten by legacy copy`() {
        val root = Files.createTempDirectory("porta-storage-test").toFile()
        try {
            val userHome = root.resolve("home").apply { mkdirs() }
            val temporaryDirectory = root.resolve("tmp").apply { mkdirs() }
            val persistentFile = userHome
                .resolve(".io.github.vietdsk6.porta")
                .apply { mkdirs() }
                .resolve("local_storage.db")
                .apply { writeText("persistent-db") }
            temporaryDirectory
                .resolve("io.github.vietdsk6.porta")
                .apply { mkdirs() }
                .resolve("local_storage.db")
                .writeText("legacy-db")

            val result = DesktopStoragePaths.databaseFile(
                fileName = "local_storage.db",
                userHome = userHome,
                temporaryDirectory = temporaryDirectory,
            )

            assertEquals(persistentFile, result)
            assertEquals("persistent-db", result.readText())
        } finally {
            root.deleteRecursively()
        }
    }

    private fun java.io.File.resolveSibling(name: String): java.io.File = parentFile.resolve(name)
}
