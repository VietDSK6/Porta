package io.github.vietdsk6.porta.data.source.preferences

/**
 * Simple key/value local storage (backed by DataStore Preferences). For small primitive flags
 * and counters — not for structured data, which belongs in Room. Keys live in [Keys].
 */
interface UserPreferences {

    companion object Keys {
        const val KEY_IS_ONBOARD_SHOWN = "KEY_IS_ONBOARD_SHOWN"
        const val KEY_OPPORTUNITY_GOAL = "KEY_OPPORTUNITY_GOAL"
        const val KEY_FRONTIER_HORIZON_DAYS = "KEY_FRONTIER_HORIZON_DAYS"
        const val KEY_FRONTIER_MAX_EFFORT = "KEY_FRONTIER_MAX_EFFORT"
        const val KEY_FRONTIER_MAX_COST = "KEY_FRONTIER_MAX_COST"
        const val KEY_FRONTIER_PRIORITIZE_SAVED = "KEY_FRONTIER_PRIORITIZE_SAVED"
        const val KEY_AI_DRAFT_API_KEY = "KEY_AI_DRAFT_API_KEY"
    }

    suspend fun getString(key: String, defaultValue: String? = null): String?
    suspend fun getInt(key: String, defaultValue: Int? = null): Int?
    suspend fun getLong(key: String, defaultValue: Long? = null): Long?
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putLong(key: String, value: Long)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun remove(key: String)

    suspend fun clear()
}
