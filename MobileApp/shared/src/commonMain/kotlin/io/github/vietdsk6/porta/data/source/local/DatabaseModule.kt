package io.github.vietdsk6.porta.data.source.local

import io.github.vietdsk6.porta.util.defaultAsyncDispatcher
import org.koin.dsl.module

// Koin wiring for the database + DAOs. The single AppDatabase is built from the platform's
// DatabaseProvider.
val databaseModule = module {
    single<AppDatabase> {
        get<DatabaseProvider>()
            .provideAppDatabaseBuilder()
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .setQueryCoroutineContext(defaultAsyncDispatcher)
            .build()
    }
    single { get<AppDatabase>().exampleDao() }
    single { get<AppDatabase>().profileFactDao() }
    single { get<AppDatabase>().profileImportDao() }
    single { get<AppDatabase>().ruleAssertionDao() }
    single { get<AppDatabase>().opportunityPackDao() }
    single { get<AppDatabase>().savedOpportunityDao() }
    single { get<AppDatabase>().planItemDao() }
    // Add new DAO accessors above — make_local.sh inserts here.
}
