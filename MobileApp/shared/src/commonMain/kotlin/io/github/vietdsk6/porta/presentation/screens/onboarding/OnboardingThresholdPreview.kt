package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_after
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_badge
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_before
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_count_after
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_count_before
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_disclosure
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_met
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_pending
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_reset
import io.github.vietdsk6.porta.generated.resources.onboarding_lesson_try
import org.jetbrains.compose.resources.stringResource

/** A decorative path and threshold mark; it does not imply any real opportunity has opened. */
@Composable
internal fun OnboardingPathMark() {
    val pathColor = AppTheme.colors.primary
    val quietColor = AppTheme.colors.outline
    Canvas(modifier = Modifier.fillMaxWidth().height(146.dp)) {
        val line = Path().apply {
            moveTo(size.width * 0.04f, size.height * 0.84f)
            cubicTo(
                size.width * 0.30f,
                size.height * 0.84f,
                size.width * 0.42f,
                size.height * 0.60f,
                size.width * 0.68f,
                size.height * 0.60f,
            )
        }
        drawPath(line, pathColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(pathColor, radius = 5.dp.toPx(), center = Offset(size.width * 0.04f, size.height * 0.84f))
        drawCircle(pathColor, radius = 5.dp.toPx(), center = Offset(size.width * 0.68f, size.height * 0.60f))

        val arch = Path().apply {
            moveTo(size.width * 0.68f, size.height * 0.86f)
            lineTo(size.width * 0.68f, size.height * 0.34f)
            cubicTo(
                size.width * 0.68f,
                size.height * 0.06f,
                size.width * 0.94f,
                size.height * 0.06f,
                size.width * 0.94f,
                size.height * 0.34f,
            )
            lineTo(size.width * 0.94f, size.height * 0.86f)
        }
        drawPath(arch, pathColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawLine(
            color = quietColor,
            start = Offset(size.width * 0.70f, size.height * 0.60f),
            end = Offset(size.width * 0.92f, size.height * 0.60f),
            strokeWidth = 2.dp.toPx(),
        )
    }
}

@Composable
internal fun OnboardingThresholdPreview(
    lesson: OnboardingThresholdLessonUiState,
    applied: Boolean,
    onToggle: () -> Unit,
) {
    val changedCount = lesson.changedOpportunityTitles.size
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("onboarding-threshold-preview"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(Res.string.onboarding_lesson_badge),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoreReading(
                    label = stringResource(Res.string.onboarding_lesson_before),
                    score = lesson.beforeScore.toString(),
                    highlighted = !applied,
                    modifier = Modifier.weight(1f),
                )
                ScoreReading(
                    label = stringResource(Res.string.onboarding_lesson_after),
                    score = lesson.afterScore.toString(),
                    highlighted = applied,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = if (applied) {
                    stringResource(Res.string.onboarding_lesson_count_after, changedCount)
                } else {
                    stringResource(Res.string.onboarding_lesson_count_before, changedCount)
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (applied) AppTheme.colors.status.success else AppTheme.colors.text.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("onboarding-threshold-outcome"),
            )
            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().testTag("onboarding-try-threshold"),
            ) {
                Text(
                    if (applied) {
                        stringResource(Res.string.onboarding_lesson_reset, lesson.beforeScore.toString())
                    } else {
                        stringResource(Res.string.onboarding_lesson_try, lesson.afterScore.toString())
                    },
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lesson.changedOpportunityTitles.forEachIndexed { index, title ->
                    SampleCheckRow(title = title, applied = applied, index = index)
                }
            }
            Text(
                stringResource(Res.string.onboarding_lesson_disclosure),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun ScoreReading(label: String, score: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.text.secondary)
        Text(
            score,
            style = MaterialTheme.typography.displaySmall,
            color = if (highlighted) AppTheme.colors.primary else AppTheme.colors.text.secondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SampleCheckRow(title: String, applied: Boolean, index: Int) {
    val status = stringResource(if (applied) Res.string.onboarding_lesson_met else Res.string.onboarding_lesson_pending)
    val animatedColor by animateColorAsState(
        targetValue = if (applied) AppTheme.colors.status.success else AppTheme.colors.outline,
        animationSpec = tween(durationMillis = 180, delayMillis = index * 35),
    )
    Row(
        modifier = Modifier.fillMaxWidth().testTag("onboarding-sample-check-$index")
            .semantics { stateDescription = status },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ThresholdArch(color = animatedColor)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                status,
                style = MaterialTheme.typography.labelSmall,
                color = if (applied) animatedColor else AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun ThresholdArch(color: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.size(width = 28.dp, height = 34.dp)) {
        val arch = Path().apply {
            moveTo(2.dp.toPx(), size.height - 2.dp.toPx())
            lineTo(2.dp.toPx(), size.height * 0.42f)
            cubicTo(
                2.dp.toPx(),
                -size.height * 0.04f,
                size.width - 2.dp.toPx(),
                -size.height * 0.04f,
                size.width - 2.dp.toPx(),
                size.height * 0.42f,
            )
            lineTo(size.width - 2.dp.toPx(), size.height - 2.dp.toPx())
        }
        drawPath(arch, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawLine(
            color,
            start = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
            end = Offset(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
        )
    }
}
