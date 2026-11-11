package io.github.vietdsk6.porta.data.source.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.vietdsk6.porta.data.source.local.DesktopStoragePaths
import io.github.vietdsk6.porta.util.Constants
import okio.Path.Companion.toPath

class PreferencesDataStoreProviderImpl : PreferencesDataStoreProvider {
    override fun providePreferencesDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            DesktopStoragePaths.preferencesFile(Constants.PREFERENCES_STORAGE_NAME)
                .absolutePath
                .toPath()
        },
    )
}
