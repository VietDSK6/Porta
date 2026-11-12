package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.ExplanationCode
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_explain_deadline_count
import io.github.vietdsk6.porta.generated.resources.frontier_explain_improve
import io.github.vietdsk6.porta.generated.resources.frontier_explain_open_count
import io.github.vietdsk6.porta.generated.resources.frontier_explain_prepare
import io.github.vietdsk6.porta.generated.resources.frontier_explain_verify
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Turns typed engine evidence into interface copy in the selected language. */
@Composable
internal fun ExplanationCode.FrontierImpact.toText(kind: FrontierMoveKind): String {
    val base = stringResource(
        when (kind) {
            FrontierMoveKind.VERIFY -> Res.string.frontier_explain_verify
            FrontierMoveKind.IMPROVE -> Res.string.frontier_explain_improve
            FrontierMoveKind.PREPARE -> Res.string.frontier_explain_prepare
        },
    )
    val opened = if (newlyOpenCount > 0) {
        pluralStringResource(Res.plurals.frontier_explain_open_count, newlyOpenCount, newlyOpenCount)
    } else {
        null
    }
    val deadline = if (timingUnverifiedCount > 0) {
        pluralStringResource(Res.plurals.frontier_explain_deadline_count, timingUnverifiedCount, timingUnverifiedCount)
    } else {
        null
    }
    return listOfNotNull(base, opened, deadline).joinToString(" ")
}
