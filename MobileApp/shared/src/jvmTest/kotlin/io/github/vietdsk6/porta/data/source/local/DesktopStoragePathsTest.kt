package io.github.vietdsk6.porta.data.source.local

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopStoragePathsTest {
    @Test
    fun `legacy temporary database is copied to macOS app data directory`() {
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
                osName = "Mac OS X",
                environment = emptyMap(),
            )

            assertEquals(userHome.resolve("Library/Application Support/Porta/local_storage.db"), result)
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
                .resolve("Library/Application Support/Porta")
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
                osName = "Mac OS X",
                environment = emptyMap(),
            )

            assertEquals(persistentFile, result)
            assertEquals("persistent-db", result.readText())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `home directory database and preferences migrate once without deleting old data`() {
        val root = Files.createTempDirectory("porta-storage-test").toFile()
        try {
            val userHome = root.resolve("home").apply { mkdirs() }
            val legacyDirectory = userHome.resolve(".io.github.vietdsk6.porta").apply { mkdirs() }
            legacyDirectory.resolve("local_storage.db").writeText("old-db")
            legacyDirectory.resolve("local_storage.db-wal").writeText("old-wal")
            legacyDirectory.resolve("user.preferences_pb").writeText("old-prefs")

            val db = DesktopStoragePaths.databaseFile(
                fileName = "local_storage.db",
                userHome = userHome,
                osName = "Mac OS X",
                environment = emptyMap(),
            )
            val preferences = DesktopStoragePaths.preferencesFile(
                fileName = "user.preferences_pb",
                userHome = userHome,
                osName = "Mac OS X",
                environment = emptyMap(),
            )

            assertEquals("old-db", db.readText())
            assertEquals("old-wal", db.resolveSibling("local_storage.db-wal").readText())
            assertEquals("old-prefs", preferences.readText())
            assertTrue(legacyDirectory.resolve("local_storage.db").exists())
            assertTrue(legacyDirectory.resolve("user.preferences_pb").exists())

            db.writeText("new-db")
            preferences.writeText("new-prefs")
            assertEquals(db, DesktopStoragePaths.databaseFile("local_storage.db", userHome, osName = "Mac OS X", environment = emptyMap()))
            assertEquals(preferences, DesktopStoragePaths.preferencesFile("user.preferences_pb", userHome, "Mac OS X", emptyMap()))
            assertEquals("new-db", db.readText())
            assertEquals("new-prefs", preferences.readText())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `Windows and Linux paths honor user data environment`() {
        val root = Files.createTempDirectory("porta-storage-test").toFile()
        try {
            val userHome = root.resolve("home").apply { mkdirs() }
            val appData = root.resolve("AppData/Roaming")
            val xdgData = root.resolve("xdg-data")

            assertEquals(
                appData.resolve("Porta"),
                DesktopStoragePaths.appDataDirectory(
                    userHome,
                    osName = "Windows 11",
                    environment = mapOf("APPDATA" to appData.absolutePath),
                ),
            )
            assertEquals(
                xdgData.resolve("porta"),
                DesktopStoragePaths.appDataDirectory(
                    userHome,
                    osName = "Linux",
                    environment = mapOf("XDG_DATA_HOME" to xdgData.absolutePath),
                ),
            )
            assertEquals(
                userHome.resolve(".local/share/porta"),
                DesktopStoragePaths.appDataDirectory(userHome, osName = "Linux", environment = emptyMap()),
            )
        } finally {
            root.deleteRecursively()
        }
    }

    private fun java.io.File.resolveSibling(name: String): java.io.File = parentFile.resolve(name)
}
