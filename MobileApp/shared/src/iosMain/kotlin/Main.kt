import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import io.github.vietdsk6.porta.root.App
import io.github.vietdsk6.porta.util.LocalNativeViewFactory
import io.github.vietdsk6.porta.util.NativeViewFactory
import io.github.vietdsk6.porta.util.SwiftLibDependencyFactory
import io.github.vietdsk6.porta.util.swiftLibDependenciesModule
import org.koin.core.KoinApplication
import platform.UIKit.UIViewController

fun MainViewController(nativeViewFactory: NativeViewFactory): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
        App()
    }
}

// This is called on application started on Swift side
fun KoinApplication.provideSwiftLibDependencyFactory(factory: SwiftLibDependencyFactory) = run { modules(swiftLibDependenciesModule(factory)) }
