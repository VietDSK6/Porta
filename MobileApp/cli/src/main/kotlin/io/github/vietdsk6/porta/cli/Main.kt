package io.github.vietdsk6.porta.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.PackDiagnosticSeverity
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.File
import java.io.IOException
import java.time.LocalDate as JavaLocalDate

fun main(args: Array<String>) {
    PortaCommand().subcommands(
        ValidateCommand(),
        EvaluateCommand(),
        RouteCommand(),
        ExportBundledCommand(),
    ).main(args)
}

private class PortaCommand : CliktCommand(name = "porta") {
    override fun help(context: Context): String = "Source-linked opportunity checks and routes."

    override fun run() = Unit
}

private abstract class PortaSubcommand(
    name: String,
    help: String,
) : CliktCommand(name = name) {
    private val helpText = help

    override fun help(context: Context): String = helpText

    protected val service = PortaCliService()

    protected fun read(path: String): String = try {
        File(path).readText()
    } catch (error: IOException) {
        throw UsageError("Cannot read '$path': ${error.message}")
    }

    protected fun pack(path: String?) = try {
        service.requirePack(path?.let(::read) ?: service.bundledPackJson())
    } catch (error: InvalidCliInput) {
        throw UsageError(error.message.orEmpty())
    }

    protected fun profile(path: String) = try {
        service.decodeProfile(read(path))
    } catch (error: InvalidCliInput) {
        throw UsageError(error.message.orEmpty())
    }

    protected fun date(value: String?): LocalDate = try {
        LocalDate.parse(value ?: JavaLocalDate.now().toString())
    } catch (error: IllegalArgumentException) {
        throw UsageError("Date must be ISO yyyy-MM-dd: ${error.message}")
    }
}

private class ValidateCommand :
    PortaSubcommand(
        name = "validate",
        help = "Validate a Porta opportunity pack and show every diagnostic.",
    ) {
    private val path by argument(help = "Pack JSON file")

    override fun run() {
        val result = service.validatePack(read(path))
        result.diagnostics.forEach { diagnostic ->
            echo("${diagnostic.severity}: ${diagnostic.path}: ${diagnostic.code}: ${diagnostic.message}")
        }
        if (!result.isValid) {
            if (result.diagnostics.isEmpty()) echo("ERROR: Pack is invalid.")
            throw ProgramResult(1)
        }
        val pack = checkNotNull(result.document)
        echo("Valid: ${pack.metadata.name} v${pack.metadata.version} (${pack.opportunities.size} doors)")
        if (result.diagnostics.any { it.severity == PackDiagnosticSeverity.WARNING }) {
            echo("Warnings require curator review before publishing.")
        }
    }
}

private class EvaluateCommand :
    PortaSubcommand(
        name = "evaluate",
        help = "Evaluate a Porta profile export against a source pack on a local date.",
    ) {
    private val profilePath by option("--profile", help = "Porta profile export JSON").required()
    private val packPath by option("--pack", help = "Pack JSON; defaults to bundled catalog")
    private val dateValue by option("--date", help = "ISO yyyy-MM-dd; defaults to today")

    override fun run() {
        val date = date(dateValue)
        val pack = pack(packPath)
        val profile = profile(profilePath)
        echo("Encoded checks on $date · ${pack.metadata.name}")
        echo("State             Door")
        service.evaluate(profile, pack, date).forEach { (title, receipt) ->
            echo("${receipt.state.name.padEnd(17)} $title")
        }
        echo("These states describe only Porta's encoded checks. Confirm with each official provider.")
    }
}

private class RouteCommand :
    PortaSubcommand(
        name = "route",
        help = "Plan up to three milestone routes through source-linked opportunity checks.",
    ) {
    private val profilePath by option("--profile", help = "Porta profile export JSON").required()
    private val packPath by option("--pack", help = "Pack JSON; defaults to bundled catalog")
    private val dateValue by option("--date", help = "ISO yyyy-MM-dd; defaults to today")
    private val horizon by option("--horizon", help = "Planning horizon in days").int().default(180)
    private val budget by option("--budget", help = "Maximum estimated cost band per step: free, low, medium, high, unknown")
        .default("low")
    private val effort by option("--effort", help = "Maximum effort per step: very_low, low, medium, high, very_high")
        .default("high")
    private val asJson by option("--json", help = "Emit machine-readable JSON").flag()

    override fun run() {
        val cost = enumValue<CostBand>(budget, "budget")
        val maxEffort = enumValue<EffortLevel>(effort, "effort")
        val routes = try {
            service.route(profile(profilePath), pack(packPath), date(dateValue), horizon, cost, maxEffort)
        } catch (error: IllegalArgumentException) {
            throw UsageError(error.message.orEmpty())
        }
        if (asJson) {
            echo(routes.toJson().toString())
        } else {
            printRoutes(routes)
        }
    }

    private inline fun <reified T : Enum<T>> enumValue(value: String, optionName: String): T = enumValues<T>().firstOrNull { it.name.equals(value.replace('-', '_'), ignoreCase = true) }
        ?: throw UsageError("Unknown $optionName '$value'. Choose: ${enumValues<T>().joinToString { it.name.lowercase() }}")

    private fun printRoutes(routes: List<PlannedRoute>) {
        if (routes.isEmpty()) {
            echo("No action route improves the encoded checks within these constraints.")
            return
        }
        routes.forEachIndexed { index, route ->
            echo(
                "Route ${index + 1}: ${route.metrics.openedTotal} doors would open; " +
                    "${route.metrics.totalDays} days; cost rank ${route.metrics.costRankSum}; " +
                    "peak effort ${route.metrics.peakEffort.name.lowercase()}",
            )
            route.steps.forEachIndexed { stepIndex, step ->
                echo("  ${stepIndex + 1}. ${step.template.title} · ${step.startDate} → ${step.endDate}")
                if (step.doorsOpened.isNotEmpty()) echo("     Would open: ${step.doorsOpened.joinToString()}")
                if (step.doorsImproved.isNotEmpty()) echo("     Checks improved: ${step.doorsImproved.joinToString()}")
                if (step.doorsLost.isNotEmpty()) echo("     Windows lost: ${step.doorsLost.joinToString()}")
            }
        }
        echo("A route reflects encoded checks, not admission or selection. Verify official sources and deadlines.")
    }
}

private class ExportBundledCommand :
    PortaSubcommand(
        name = "export-bundled",
        help = "Export Porta's bundled opportunity catalog as JSON.",
    ) {
    private val out by option("--out", help = "Output path; defaults to standard output")

    override fun run() {
        val json = service.bundledPackJson()
        if (out == null) {
            echo(json)
        } else {
            try {
                File(checkNotNull(out)).writeText(json)
            } catch (error: IOException) {
                throw UsageError("Cannot write '$out': ${error.message}")
            }
            echo("Exported bundled pack to $out", err = true)
        }
    }
}

private fun List<PlannedRoute>.toJson(): JsonArray = buildJsonArray {
    this@toJson.forEach { route ->
        add(
            buildJsonObject {
                put("id", route.id)
                putJsonObject("metrics") {
                    put("openedTotal", route.metrics.openedTotal)
                    put("openedSaved", route.metrics.openedSaved)
                    route.metrics.daysToFirstDoor?.let { put("daysToFirstDoor", it) }
                    put("totalDays", route.metrics.totalDays)
                    put("costRankSum", route.metrics.costRankSum)
                    put("peakEffort", route.metrics.peakEffort.name)
                }
                putJsonArray("steps") {
                    route.steps.forEach { step ->
                        add(
                            buildJsonObject {
                                put("actionId", step.template.id)
                                put("title", step.template.title)
                                put("startDate", step.startDate.toString())
                                put("endDate", step.endDate.toString())
                                putJsonArray("doorsOpened") { step.doorsOpened.forEach { add(JsonPrimitive(it)) } }
                                putJsonArray("doorsImproved") { step.doorsImproved.forEach { add(JsonPrimitive(it)) } }
                                putJsonArray("doorsLost") { step.doorsLost.forEach { add(JsonPrimitive(it)) } }
                            },
                        )
                    }
                }
            },
        )
    }
}
