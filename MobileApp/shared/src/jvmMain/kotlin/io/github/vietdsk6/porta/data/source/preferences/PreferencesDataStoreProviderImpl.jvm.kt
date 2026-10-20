package io.github.vietdsk6.porta.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.vietdsk6.porta.util.Constants
import okio.Path.Companion.toPath
import java.io.File

class PreferencesDataStoreProviderImpl : PreferencesDataStoreProvider {
    override fun providePreferencesDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            // Not java.io.tmpdir — the OS may wipe it, silently resetting user preferences.
            // Named after the app id so it's unique per app and refactor_package.sh renames it too.
            val appDataDir = File(System.getProperty("user.home"), ".io.github.vietdsk6.porta")
            appDataDir.mkdirs()
            File(appDataDir, Constants.PREFERENCES_STORAGE_NAME)
                .absolutePath
                .toPath()
        },
    )
}
