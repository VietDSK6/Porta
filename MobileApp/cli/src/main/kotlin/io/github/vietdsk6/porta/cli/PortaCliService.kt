package io.github.vietdsk6.porta.cli

import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.PackValidationResult
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.RoutePlanner
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import kotlinx.datetime.LocalDate

/** The terminal uses the same parser, profile export, and decision engine as the app. */
class PortaCliService(
    private val packCodec: OpportunityPackCodec = OpportunityPackCodec(),
    private val packValidator: OpportunityPackValidator = OpportunityPackValidator(),
    private val profileCodec: ProfileExportCodec = ProfileExportCodec(),
    private val eligibilityEvaluator: EligibilityEvaluator = EligibilityEvaluator(),
) {
    private val packParser = OpportunityPackParser(packCodec, packValidator)
    private val bundledProvider = BundledOpportunityPackProvider(packCodec)
    private val routePlanner = RoutePlanner(eligibilityEvaluator, ScenarioEvaluator(eligibilityEvaluator))

    fun validatePack(rawJson: String): PackValidationResult = packParser.parse(rawJson)

    fun requirePack(rawJson: String): OpportunityPackDocument {
        val result = validatePack(rawJson)
        return result.document?.takeIf { result.isValid }
            ?: throw InvalidCliInput(result.diagnostics.joinToString("\n") { "${it.path}: ${it.message}" })
    }

    fun bundledPackJson(): String = bundledProvider.rawJson()

    fun decodeProfile(rawJson: String): ProfileSnapshot = try {
        profileCodec.decode(rawJson)
    } catch (error: IllegalArgumentException) {
        throw InvalidCliInput("Invalid Porta profile export: ${error.message}")
    }

    fun evaluate(
        profile: ProfileSnapshot,
        pack: OpportunityPackDocument,
        date: LocalDate,
    ): List<Pair<String, EligibilityReceipt>> = pack.opportunities.map { opportunity ->
        opportunity.title to eligibilityEvaluator.evaluate(profile, opportunity, date)
    }

    fun route(
        profile: ProfileSnapshot,
        pack: OpportunityPackDocument,
        date: LocalDate,
        horizonDays: Int,
        maxCost: CostBand,
        maxEffort: EffortLevel,
    ): List<PlannedRoute> {
        require(horizonDays in 1..3650) { "Horizon must be between 1 and 3650 days." }
        return routePlanner.plan(
            profile = profile,
            opportunities = pack.opportunities,
            savedOpportunityIds = emptySet(),
            constraints = RouteConstraints(
                horizonDays = horizonDays,
                maxCostPerStep = maxCost,
                maxEffort = maxEffort,
            ),
            today = date,
        )
    }
}

class InvalidCliInput(message: String) : IllegalArgumentException(message)
