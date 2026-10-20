package io.github.vietdsk6.porta.data.source.local

import io.github.vietdsk6.porta.util.defaultAsyncDispatcher
import org.koin.dsl.module

// Koin wiring for the database + DAOs. The single AppDatabase is built from the platform's
// DatabaseProvider.
val databaseModule = module {
    single<AppDatabase> {
        get<DatabaseProvider>()
            .provideAppDatabaseBuilder()
            // No real migrations yet: on any schema change Room drops & recreates all tables.
            // Replace with proper Migrations before shipping data you can't lose.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setQueryCoroutineContext(defaultAsyncDispatcher)
            .build()
    }
    single { get<AppDatabase>().exampleDao() }
    single { get<AppDatabase>().creditTransactionDao() }
    single { get<AppDatabase>().generationOutputDao() }
    single { get<AppDatabase>().profileFactDao() }
    single { get<AppDatabase>().opportunityPackDao() }
    single { get<AppDatabase>().savedOpportunityDao() }
    single { get<AppDatabase>().planItemDao() }
    // Add new DAO accessors above — make_local.sh inserts here.
}
