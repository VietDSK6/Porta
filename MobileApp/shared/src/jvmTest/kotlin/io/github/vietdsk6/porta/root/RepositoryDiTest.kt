package io.github.vietdsk6.porta.root

import io.github.vietdsk6.porta.data.repository.ProfileImportRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.RuleAssertionRepository
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingViewModel
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class RepositoryDiTest {
    @Test
    fun jvmGraphResolvesProfileRepositoriesAndOnboarding() {
        val application = koinApplication {
            modules(appModules)
        }

        try {
            assertNotNull(application.koin.get<ProfileRepository>())
            assertNotNull(application.koin.get<RuleAssertionRepository>())
            assertNotNull(application.koin.get<ProfileImportRepository>())
            assertNotNull(application.koin.get<OnBoardingViewModel>())
        } finally {
            application.close()
        }
    }
}
