package io.github.vietdsk6.porta.root

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileImportRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.RuleAssertionRepository
import io.github.vietdsk6.porta.data.repository.RuleDraftRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityStudioDraftCompiler
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferencesImpl
import io.github.vietdsk6.porta.data.source.remote.draft.DemoDraftService
import io.github.vietdsk6.porta.data.source.remote.draft.GeminiDraftService
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ManageLocalPortaDataUseCase
import io.github.vietdsk6.porta.domain.usecase.RoutePlanner
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateViewModel
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioViewModel
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.route.RouteViewModel
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.util.ApplicationScope
import io.github.vietdsk6.porta.util.defaultAsyncDispatcher
import io.github.vietdsk6.porta.util.getPlatformCapabilities
import io.github.vietdsk6.porta.util.logging.Logger
import io.github.vietdsk6.porta.util.logging.NapierLogger
import io.github.vietdsk6.porta.util.platformModule
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/** Koin graph for Porta's local-first planning features. */
private val domainModule = module {
    singleOf(::EligibilityEvaluator)
    singleOf(::ScenarioEvaluator)
    singleOf(::ActionFrontierEngine)
    singleOf(::RoutePlanner)
    singleOf(::ManageLocalPortaDataUseCase)
}

private val dataModule = module {
    singleOf(::ApplicationScope)
    single { CurrentLocalDateProvider() }
    single { getPlatformCapabilities() }
    factory { defaultAsyncDispatcher } bind CoroutineContext::class
    factory { BackgroundExecutor.IO } bind BackgroundExecutor::class

    single { OpportunityPackCodec() }
    singleOf(::BundledOpportunityPackProvider)
    singleOf(::OpportunityPackValidator)
    singleOf(::OpportunityPackParser)
    singleOf(::OpportunityStudioDraftCompiler)
    single { ProfileExportCodec() }
    single { HttpClient() }
    singleOf(::DemoDraftService)

    single {
        UserPreferencesImpl(
            get<PreferencesDataStoreProvider>().providePreferencesDataStore(),
        )
    } bind UserPreferences::class

    singleOf(::OpportunityPackRepository)
    singleOf(::PlanRepository)
    singleOf(::ProfileImportRepository)
    singleOf(::ProfileRepository)
    singleOf(::RuleAssertionRepository)
    singleOf(::SavedOpportunityRepository)
    single {
        val httpClient = get<HttpClient>()
        RuleDraftRepository(
            userPreferences = get(),
            demoDraftService = get<DemoDraftService>(),
            geminiDraftServiceFactory = { key -> GeminiDraftService(httpClient, key) },
            backgroundExecutor = get(),
        )
    }

    factory<Logger> { NapierLogger() }
}

private val presentationModule = module {
    viewModelOf(::OnBoardingViewModel)
    viewModelOf(::ProfileSetupViewModel)
    viewModelOf(::DoorsViewModel)
    viewModel { (opportunityId: String) ->
        OpportunityDetailViewModel(opportunityId, get(), get(), get(), get(), get())
    }
    viewModelOf(::FrontierViewModel)
    viewModelOf(::RouteViewModel)
    viewModel { (actionId: String) ->
        ScenarioPreviewViewModel(actionId, get(), get(), get(), get(), get())
    }
    viewModelOf(::ActivePlanViewModel)
    viewModel {
        StudentProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModelOf(::PackInspectorViewModel)
    viewModel {
        OpportunityStudioViewModel(get(), get(), get(), get(), get(), get())
    }
    viewModelOf(::ActualProfileUpdateViewModel)
    // Add new view models below — generate_screen.sh inserts here.
}

internal val appModules: List<Module>
    get() = platformModule + domainModule + dataModule + presentationModule
