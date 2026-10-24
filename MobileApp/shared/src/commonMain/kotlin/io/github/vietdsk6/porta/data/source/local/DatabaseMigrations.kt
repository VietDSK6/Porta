package io.github.vietdsk6.porta.data.source.local

import androidx.room3.migration.Migration
import androidx.sqlite.execSQL

/** Preserves existing profiles and plans while introducing opportunity-scoped rule assertions. */
val MIGRATION_1_2 = Migration(1, 2) { connection ->
    connection.execSQL(
        "ALTER TABLE plan_item ADD COLUMN target_scope TEXT NOT NULL DEFAULT 'PROFILE_FACT'",
    )
    connection.execSQL("ALTER TABLE plan_item ADD COLUMN opportunity_id TEXT")
    connection.execSQL("ALTER TABLE plan_item ADD COLUMN rule_id TEXT")
    connection.execSQL("ALTER TABLE plan_item ADD COLUMN rule_fingerprint TEXT")
    connection.execSQL(
        """
        CREATE TABLE IF NOT EXISTS rule_assertion (
            profile_id TEXT NOT NULL,
            opportunity_id TEXT NOT NULL,
            rule_id TEXT NOT NULL,
            rule_fingerprint TEXT NOT NULL,
            fact_key TEXT NOT NULL,
            value_type TEXT NOT NULL,
            number_value REAL,
            text_value TEXT,
            boolean_value INTEGER,
            list_value_json TEXT,
            unit TEXT,
            duration_amount INTEGER,
            evidence_label TEXT,
            evidence_reference TEXT,
            evidence_checked_at INTEGER,
            updated_at INTEGER NOT NULL,
            PRIMARY KEY(profile_id, opportunity_id, rule_id)
        )
        """.trimIndent(),
    )
}
