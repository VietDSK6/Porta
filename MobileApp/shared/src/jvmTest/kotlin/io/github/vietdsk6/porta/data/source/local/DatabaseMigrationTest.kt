package io.github.vietdsk6.porta.data.source.local

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseMigrationTest {
    @Test
    fun `migration 1 to 2 preserves profile saved door and active plan`() = runTest {
        val connection = BundledSQLiteDriver().open(":memory:")
        try {
            connection.execSQL("CREATE TABLE profile_fact (id TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)")
            connection.execSQL("CREATE TABLE saved_opportunity (id TEXT NOT NULL PRIMARY KEY)")
            connection.execSQL(
                "CREATE TABLE plan_item (id TEXT NOT NULL PRIMARY KEY, profile_id TEXT NOT NULL)",
            )
            connection.execSQL("INSERT INTO profile_fact VALUES ('default:gpa', '3.6')")
            connection.execSQL("INSERT INTO saved_opportunity VALUES ('aurora')")
            connection.execSQL("INSERT INTO plan_item VALUES ('plan-1', 'default')")

            MIGRATION_1_2.migrate(connection)

            assertEquals("3.6", connection.singleText("SELECT value FROM profile_fact WHERE id = 'default:gpa'"))
            assertEquals("aurora", connection.singleText("SELECT id FROM saved_opportunity"))
            assertEquals("plan-1", connection.singleText("SELECT id FROM plan_item"))
            assertEquals(
                "PROFILE_FACT",
                connection.singleText("SELECT target_scope FROM plan_item WHERE id = 'plan-1'"),
            )
            assertTrue(connection.tableExists("rule_assertion"))
            assertTrue(connection.columnNames("plan_item").containsAll(EXPECTED_PLAN_COLUMNS))
        } finally {
            connection.close()
        }
    }

    @Test
    fun `migration 2 to 3 removes retired tables and preserves Porta data`() = runTest {
        val connection = BundledSQLiteDriver().open(":memory:")
        try {
            connection.execSQL("CREATE TABLE generation_outputs (id TEXT NOT NULL PRIMARY KEY)")
            connection.execSQL("CREATE TABLE credit_transaction (id TEXT NOT NULL PRIMARY KEY)")
            connection.execSQL("CREATE TABLE profile_fact (id TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)")
            connection.execSQL("CREATE TABLE saved_opportunity (id TEXT NOT NULL PRIMARY KEY)")
            connection.execSQL("INSERT INTO generation_outputs VALUES ('generation-1')")
            connection.execSQL("INSERT INTO credit_transaction VALUES ('credit-1')")
            connection.execSQL("INSERT INTO profile_fact VALUES ('default:gpa', '3.6')")
            connection.execSQL("INSERT INTO saved_opportunity VALUES ('aurora')")

            MIGRATION_2_3.migrate(connection)

            assertTrue(!connection.tableExists("generation_outputs"))
            assertTrue(!connection.tableExists("credit_transaction"))
            assertEquals("3.6", connection.singleText("SELECT value FROM profile_fact WHERE id = 'default:gpa'"))
            assertEquals("aurora", connection.singleText("SELECT id FROM saved_opportunity"))
        } finally {
            connection.close()
        }
    }

    private fun androidx.sqlite.SQLiteConnection.singleText(sql: String): String = prepare(sql).use { statement ->
        check(statement.step()) { "Query returned no rows: $sql" }
        statement.getText(0)
    }

    private fun androidx.sqlite.SQLiteConnection.tableExists(name: String): Boolean = prepare(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
    ).use { statement ->
        statement.bindText(1, name)
        statement.step()
    }

    private fun androidx.sqlite.SQLiteConnection.columnNames(table: String): Set<String> = prepare(
        "PRAGMA table_info($table)",
    ).use { statement ->
        buildSet {
            while (statement.step()) add(statement.getText(1))
        }
    }

    private companion object {
        val EXPECTED_PLAN_COLUMNS = setOf(
            "target_scope",
            "opportunity_id",
            "rule_id",
            "rule_fingerprint",
        )
    }
}
