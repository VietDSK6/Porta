package io.github.vietdsk6.porta.data.source.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.util.Constants
class DatabaseProviderImpl : DatabaseProvider {
    override fun provideAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val dbFilePath = DesktopStoragePaths.databaseFile(Constants.LOCAL_DB_STORAGE_NAME)
        return Room.databaseBuilder<AppDatabase>(
            name = dbFilePath.absolutePath,
        ).setDriver(BundledSQLiteDriver())
    }
}
