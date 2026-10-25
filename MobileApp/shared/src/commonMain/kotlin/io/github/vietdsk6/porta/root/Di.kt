package io.github.vietdsk6.porta.root

import io.github.vietdsk6.porta.common.BuildConfig
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.CreditRepository
import io.github.vietdsk6.porta.data.repository.GenerationRepository
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.RuleAssertionRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.repository.SubscriptionRepository
import io.github.vietdsk6.porta.data.repository.UserRepository
import io.github.vietdsk6.porta.data.source.ai.ReplicateGenerationProvider
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferencesImpl
import io.github.vietdsk6.porta.data.source.remote.HttpClientFactory
import io.github.vietdsk6.porta.data.source.remote.apiservices.ApiService
import io.github.vietdsk6.porta.data.source.remote.apiservices.TemporaryFileUploadApiService
import io.github.vietdsk6.porta.data.source.remote.apiservices.ai.AiTransport
import io.github.vietdsk6.porta.data.source.remote.apiservices.ai.OpenAiApiService
import io.github.vietdsk6.porta.data.source.remote.apiservices.ai.ReplicateApiService
import io.github.vietdsk6.porta.domain.model.credit.creditSystemConfig
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.AiGenerationProvider
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateViewModel
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.subscription.api.MockSubscriptionProvider
import io.github.vietdsk6.porta.subscription.api.NoOpSubscriptionProviderUi
import io.github.vietdsk6.porta.subscription.api.SubscriptionProvider
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderFactory
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderUi
import io.github.vietdsk6.porta.util.ApplicationScope
import io.github.vietdsk6.porta.util.Constants
import io.github.vietdsk6.porta.util.defaultAsyncDispatcher
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.isAndroid
import io.github.vietdsk6.porta.util.logging.Logger
import io.github.vietdsk6.porta.util.logging.NapierLogger
import io.github.vietdsk6.porta.util.logging.TelegramLogger
import io.github.vietdsk6.porta.util.platformModule
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/**
 * Koin module graph for the shared app. [AppInitializer] loads [appModules] at startup.
 * Layered by concern: [domainModule] (deterministic planning engines), [dataModule] (infra + repositories),
 * [presentationModule] (ViewModels), plus the per-target expect/actual `platformModule`.
 */

// Pure, deterministic Porta engines. Keeping them in the graph gives ViewModels one shared source
// of truth without coupling the domain layer to Koin.
private val domainModule = module {
    singleOf(::EligibilityEvaluator)
    singleOf(::ScenarioEvaluator)
    singleOf(::ActionFrontierEngine)
}

// Infrastructure + repositories: scopes/dispatchers, preferences, network, auth & subscription
// providers (selected via Constants), repositories, loggers, AI provider, and the credit system.
private val dataModule = module {
    singleOf(::ApplicationScope)
    single { CurrentLocalDateProvider() }
    factory { defaultAsyncDispatcher } bind CoroutineContext::class
    factory { BackgroundExecutor.IO } bind BackgroundExecutor::class
    // Use the codec's intentionally configured default Json instance. Constructor DSL would try
    // to resolve Json from Koin instead of applying the Kotlin default argument.
    single { OpportunityPackCodec() }
    singleOf(::BundledOpportunityPackProvider)
    singleOf(::OpportunityPackValidator)
    singleOf(::OpportunityPackParser)
    single { ProfileExportCodec() }

    // Preferences Source. The DataStore instance stays out of the Koin graph on
    // purpose — generic types erase to `DataStore`, so a second DataStore<T>
    // registered later would silently collide with this one.
    single { UserPreferencesImpl(get<PreferencesDataStoreProvider>().providePreferencesDataStore()) } bind UserPreferences::class

    // Remote source
    single { HttpClientFactory.default() }
    single(named("aiDirectClient")) { HttpClientFactory.noAuth() }
    single { AiTransport(proxyClient = get(), directClient = get(named("aiDirectClient"))) }
    single { TemporaryFileUploadApiService(HttpClientFactory.fileUpload()) }

    factoryOf(::ApiService)
    factoryOf(::OpenAiApiService)
    factoryOf(::ReplicateApiService)

    // Subscription Provider. When no real SDK key is set (isSubscriptionMockActive), swap in the
    // MockSubscriptionProvider so the paywall/purchase/unlock flow is explorable with zero keys.
    // Auto-reverts to the real provider (Adapty/RevenueCat) the moment a key is configured.
    factory { AppConfiguration.subscriptionProviderFactory } bind SubscriptionProviderFactory::class
    single {
        if (isSubscriptionMockActive()) {
            val userPreferences = get<UserPreferences>()
            MockSubscriptionProvider(
                readPremiumPurchased = {
                    userPreferences.getBoolean(MockSubscriptionProvider.KEY_MOCK_PREMIUM_PURCHASED, false)
                },
                writePremiumPurchased = {
                    userPreferences.putBoolean(MockSubscriptionProvider.KEY_MOCK_PREMIUM_PURCHASED, it)
                },
                premiumAccessId = Constants.PAYWALL_PREMIUM_ACCESS,
                creditPackPrefix = Constants.CREDIT_PACK_PRODUCT_ID_PREFIX,
                creditPackPlacementId = Constants.PAYWALL_PLACEMENT_CREDITS_PACK,
                currentTimeMillis = ::nowEpochMillis,
            )
        } else {
            get<SubscriptionProviderFactory>().createProvider()
        }
    } bind SubscriptionProvider::class
    factory {
        if (isSubscriptionMockActive()) {
            NoOpSubscriptionProviderUi
        } else {
            get<SubscriptionProviderFactory>().createProviderUi()
        }
    } bind SubscriptionProviderUi::class

    // Repositories
    single { UserRepository(get(), get(), get(), get()) }
    single { SubscriptionRepository(get(), get(), get(), get()) }
    single { GenerationRepository(get(), get(), get(), get(), get(), get(), get()) }
    singleOf(::OpportunityPackRepository)
    singleOf(::PlanRepository)
    singleOf(::ProfileRepository)
    singleOf(::RuleAssertionRepository)
    singleOf(::SavedOpportunityRepository)

    // Loggers
    factory { TelegramLogger(get(), get(), get()) } bind Logger::class
    factory { NapierLogger() } bind Logger::class

//    factory<AiGenerationProvider> { OpenAiImageGenerationProvider(get(), get()) }
    factory<AiGenerationProvider> { ReplicateGenerationProvider(get(), get()) }

    initializeCreditSystem()
}

// Porta ViewModels, scoped per NavEntry.
private val presentationModule = module {
    viewModelOf(::OnBoardingViewModel)
    viewModelOf(::ProfileSetupViewModel)
    viewModelOf(::DoorsViewModel)
    viewModel { (opportunityId: String) -> OpportunityDetailViewModel(opportunityId, get(), get(), get(), get(), get()) }
    viewModelOf(::FrontierViewModel)
    viewModel { (actionId: String) -> ScenarioPreviewViewModel(actionId, get(), get(), get(), get(), get()) }
    viewModelOf(::ActivePlanViewModel)
    viewModelOf(::StudentProfileViewModel)
    viewModelOf(::PackInspectorViewModel)
    viewModelOf(::ActualProfileUpdateViewModel)
    // Add new view models below — generate_screen.sh inserts here.
}

private fun Module.initializeCreditSystem() {
    single {
        val subscriptionRepository = get<SubscriptionRepository>()
        val appCreditSystemConfig = creditSystemConfig {
            oneTimeBonus("welcome_bonus_credit", 1)
//            val userPreferences = get<UserPreferences>()
//            oneTimeBonus(
//                id = "referral_bonus",
//                amount = 1,
//                condition = {
//                    userPreferences.getBoolean(UserPreferences.KEY_REFERRAL_COMPLETED)
//                }
//            )
//            recurringWeekly(
//                id = "free_plan_weekly",
//                amount = 2,
//                condition = {
//                    !subscriptionRepository.hasPremiumAccess()
//                }
//            )

            recurringWeekly(
                id = "premium_plan_weekly", // Gives all premium users 10 credits per week
                amount = 10,
                condition = {
                    subscriptionRepository.hasPremiumAccess()
                },
            )
        }

        CreditRepository(appCreditSystemConfig, get(), get(), get(), get())
    }
}

/**
 * True when the active-platform subscription SDK key is still a placeholder, i.e. no real
 * Adapty/RevenueCat account is wired yet. While true the app runs [MockSubscriptionProvider] so the
 * whole paywall → purchase → unlock flow is explorable with zero keys. Auto-off once a real key is set.
 */
private fun isSubscriptionMockActive(): Boolean {
    val key =
        if (isAndroid) {
            BuildConfig.SUBSCRIPTION_PROVIDER_ANDROID_API_KEY
        } else {
            BuildConfig.SUBSCRIPTION_PROVIDER_IOS_API_KEY
        }
    return key.isBlank() || key == MockSubscriptionProvider.PLACEHOLDER_KEY
}

// All Koin modules loaded at startup. platformModule is the expect/actual per-target module.
internal val appModules: List<Module> get() = platformModule + domainModule + dataModule + presentationModule
