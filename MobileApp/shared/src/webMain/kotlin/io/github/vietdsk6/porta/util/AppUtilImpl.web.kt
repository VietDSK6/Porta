package io.github.vietdsk6.porta.util

class AppUtilImpl : AppUtil {

    override fun shareApp() {
        println("This is not supported on Web platform")
    }

    override fun openFeedbackMail() {
        println("This is not supported on Web platform")
    }

    override fun getAppName(): String {
        return "Porta" // TODO Update name
    }

    override fun getAppVersionInfo(): String {
        return "1.0.0" // TODO Update version
    }
}
