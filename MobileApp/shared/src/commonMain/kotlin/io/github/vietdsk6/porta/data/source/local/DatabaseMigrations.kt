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

/** Removes the retired AI generation history and credit ledger without touching Porta data. */
val MIGRATION_2_3 = Migration(2, 3) { connection ->
    connection.execSQL("DROP TABLE IF EXISTS generation_outputs")
    connection.execSQL("DROP TABLE IF EXISTS credit_transaction")
}

/** Adds optional app-private evidence attachment descriptors without changing existing evidence. */
val MIGRATION_3_4 = Migration(3, 4) { connection ->
    listOf("profile_fact", "rule_assertion").forEach { table ->
        connection.execSQL("ALTER TABLE $table ADD COLUMN evidence_attachment_name TEXT")
        connection.execSQL("ALTER TABLE $table ADD COLUMN evidence_attachment_mime_type TEXT")
        connection.execSQL("ALTER TABLE $table ADD COLUMN evidence_attachment_size INTEGER")
        connection.execSQL("ALTER TABLE $table ADD COLUMN evidence_attachment_storage_key TEXT")
    }
}

/** Removes the unused starter example table while preserving every Porta table. */
val MIGRATION_4_5 = Migration(4, 5) { connection ->
    connection.execSQL("DROP TABLE IF EXISTS example")
}

/** Adds ordered route membership without changing existing one-step plans. */
val MIGRATION_5_6 = Migration(5, 6) { connection ->
    connection.execSQL("ALTER TABLE plan_item ADD COLUMN route_id TEXT")
    connection.execSQL("ALTER TABLE plan_item ADD COLUMN sequence INTEGER")
}
