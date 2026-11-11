package io.github.vietdsk6.porta.data.source.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import io.github.vietdsk6.porta.data.source.local.dao.OpportunityPackDao
import io.github.vietdsk6.porta.data.source.local.dao.PlanItemDao
import io.github.vietdsk6.porta.data.source.local.dao.ProfileFactDao
import io.github.vietdsk6.porta.data.source.local.dao.ProfileImportDao
import io.github.vietdsk6.porta.data.source.local.dao.RuleAssertionDao
import io.github.vietdsk6.porta.data.source.local.dao.SavedOpportunityDao
import io.github.vietdsk6.porta.data.source.local.entity.OpportunityPackEntity
import io.github.vietdsk6.porta.data.source.local.entity.PlanItemEntity
import io.github.vietdsk6.porta.data.source.local.entity.ProfileFactEntity
import io.github.vietdsk6.porta.data.source.local.entity.RuleAssertionEntity
import io.github.vietdsk6.porta.data.source.local.entity.SavedOpportunityEntity

// The app's Room 3 database (lives in commonMain). Bump `version` and add a Migration whenever
// the shipped schema changes so existing local Porta data remains readable.
@Database(
    entities = [PlanItemEntity::class, SavedOpportunityEntity::class, OpportunityPackEntity::class, ProfileFactEntity::class, RuleAssertionEntity::class],
    version = 5,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileFactDao(): ProfileFactDao
    abstract fun profileImportDao(): ProfileImportDao
    abstract fun ruleAssertionDao(): RuleAssertionDao
    abstract fun opportunityPackDao(): OpportunityPackDao
    abstract fun savedOpportunityDao(): SavedOpportunityDao
    abstract fun planItemDao(): PlanItemDao
    // Add new DAOs above — make_local.sh inserts here.
}

// Each platform supplies a DatabaseProvider actual that picks the right SQLite driver
// (bundled native on Android/iOS/JVM, web-worker on js/wasmJs).
interface DatabaseProvider {
    fun provideAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
