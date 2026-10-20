package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

data class OpportunityPack(
    val id: String,
    val name: String,
    val publisher: String,
    val schemaVersion: Int,
    val version: Int,
    val rawJson: String,
    val sourceUrl: String? = null,
    val lastCheckedOn: LocalDate,
    val importedAtEpochMillis: Long,
    val isActive: Boolean,
)
