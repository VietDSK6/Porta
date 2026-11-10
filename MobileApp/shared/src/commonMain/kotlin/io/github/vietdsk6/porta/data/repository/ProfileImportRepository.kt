package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.ProfileImportDao
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.util.file.FileManager

class ProfileImportRepository(
    private val profileImportDao: ProfileImportDao,
    private val backgroundExecutor: BackgroundExecutor,
    private val fileManager: FileManager? = null,
) {
    suspend fun replaceActiveProfile(snapshot: ProfileSnapshot): Result<Unit> = backgroundExecutor.execute {
        require(snapshot.facts.all { (key, fact) -> key == fact.key }) {
            "Imported fact keys must match their map entries."
        }
        require(snapshot.ruleAssertions.all { (ruleRef, assertion) -> ruleRef == assertion.ruleRef }) {
            "Imported rule references must match their map entries."
        }

        val profileId = ProfileFact.DEFAULT_PROFILE_ID
        val facts = snapshot.facts.values.map { fact ->
            fact.copy(
                id = "$profileId:${fact.key.name.lowercase()}",
                profileId = profileId,
                evidence = fact.evidence.asImportedDescriptor(),
            ).toEntity()
        }
        val assertions = snapshot.ruleAssertions.values.map { assertion ->
            assertion.copy(
                profileId = profileId,
                evidence = assertion.evidence.asImportedDescriptor(),
            ).toEntity()
        }
        profileImportDao.replaceProfile(
            profileId = profileId,
            facts = facts,
            assertions = assertions,
        )
        fileManager?.clearEvidenceAttachments(profileId)
        Result.success(Unit)
    }
}

private fun EvidenceMetadata?.asImportedDescriptor(): EvidenceMetadata? = this?.copy(
    attachment = attachment?.copy(storageKey = null),
)
