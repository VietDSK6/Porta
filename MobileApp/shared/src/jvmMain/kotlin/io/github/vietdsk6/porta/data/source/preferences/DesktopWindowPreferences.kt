package io.github.vietdsk6.porta.data.source.preferences

import org.koin.core.context.GlobalContext

/** Last known Desktop window bounds. Position is absent until the OS has placed the first window. */
data class DesktopWindowGeometry(
    val width: Int = 1200,
    val height: Int = 800,
    val x: Int? = null,
    val y: Int? = null,
    val maximized: Boolean = false,
)

/** Uses Porta's existing DataStore instance, so window settings follow the same local-data controls. */
object DesktopWindowPreferences {
    private const val WIDTH = "desktop.window.width"
    private const val HEIGHT = "desktop.window.height"
    private const val X = "desktop.window.x"
    private const val Y = "desktop.window.y"
    private const val MAXIMIZED = "desktop.window.maximized"

    suspend fun load(): DesktopWindowGeometry {
        val preferences = GlobalContext.get().get<UserPreferences>()
        return DesktopWindowGeometry(
            width = (preferences.getInt(WIDTH) ?: 1200).coerceAtLeast(640),
            height = (preferences.getInt(HEIGHT) ?: 800).coerceAtLeast(480),
            x = preferences.getInt(X),
            y = preferences.getInt(Y),
            maximized = preferences.getBoolean(MAXIMIZED),
        )
    }

    suspend fun save(geometry: DesktopWindowGeometry) {
        val preferences = GlobalContext.get().get<UserPreferences>()
        preferences.putInt(WIDTH, geometry.width.coerceAtLeast(640))
        preferences.putInt(HEIGHT, geometry.height.coerceAtLeast(480))
        geometry.x?.let { preferences.putInt(X, it) }
        geometry.y?.let { preferences.putInt(Y, it) }
        preferences.putBoolean(MAXIMIZED, geometry.maximized)
    }
}
