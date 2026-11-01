import androidx.compose.ui.window.ComposeUIViewController
import io.github.vietdsk6.porta.root.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
