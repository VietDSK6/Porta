package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonStyle
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

private const val TOTAL_STEPS = 4

@Composable
fun OnBoardingScreenVariation1(
    modifier: Modifier = Modifier,
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingTopBar(
            currentStep = uiState.currentStep,
            onSkip = { onUiEvent(OnBoardingUiEvent.SkipDemo) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = AppTheme.spacing.outerSpacing,
                    end = AppTheme.spacing.outerSpacing,
                    bottom = AppTheme.spacing.sectionSpacing,
                ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            when (uiState.currentStep) {
                0 -> GoalStep(uiState = uiState, onUiEvent = onUiEvent)
                1 -> ScenarioStep(uiState = uiState, onUiEvent = onUiEvent)
                2 -> ResultStep(uiState = uiState, onUiEvent = onUiEvent)
                else -> NextMoveStep(uiState = uiState, onUiEvent = onUiEvent)
            }
        }

        if (uiState.currentStep < TOTAL_STEPS - 1) {
            OnboardingBottomBar(uiState = uiState, onUiEvent = onUiEvent)
        }
    }
}

@Composable
private fun OnboardingTopBar(
    currentStep: Int,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 620.dp)
            .fillMaxWidth()
            .padding(
                start = AppTheme.spacing.outerSpacing,
                end = AppTheme.spacing.outerSpacing,
                top = AppTheme.spacing.defaultSpacing,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PORTA",
                style = AppTheme.typography.h5,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onSkip) {
                Text("Skip demo", color = AppTheme.colors.text.secondary)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { stateDescription = "Step ${currentStep + 1} of $TOTAL_STEPS" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(TOTAL_STEPS) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(100))
                        .background(
                            if (index <= currentStep) {
                                AppTheme.colors.primary
                            } else {
                                AppTheme.colors.outline.copy(alpha = 0.35f)
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun GoalStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    StepHeading(
        eyebrow = "STEP 1 · YOUR DIRECTION",
        title = "Which doors matter to you?",
        body = "Pick one focus for now. Porta will use it to organize your map—not to judge your chances.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
        OnboardingGoal.entries.forEach { goal ->
            GoalCard(
                goal = goal,
                selected = uiState.selectedGoal == goal,
                onClick = { onUiEvent(OnBoardingUiEvent.GoalSelected(goal)) },
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: OnboardingGoal,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding-goal-${goal.storageValue}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AppTheme.colors.alternative else AppTheme.colors.surfaceContainer,
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) AppTheme.colors.primary else AppTheme.colors.outline.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.sectionSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) AppTheme.colors.primary else AppTheme.colors.alternative)
                    .padding(horizontal = 13.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (selected) "✓" else "◇",
                    color = if (selected) AppTheme.colors.onPrimary else AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(goal.label, style = AppTheme.typography.h6, color = AppTheme.colors.text.primary)
                Text(
                    goal.description,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@Composable
private fun ScenarioStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    StepHeading(
        eyebrow = "STEP 2 · SAMPLE WHAT-IF",
        title = "See one change ripple outward",
        body = "This is a fictional student and synthetic opportunities. Move one fact; no real profile is changed.",
    )
    SampleBadge()
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("IELTS", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.secondary)
                    Text(
                        text = uiState.sampleIelts.asBand(),
                        style = AppTheme.typography.h1,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (uiState.sampleIelts >= 7.0) "TARGET REACHED" else "TARGET 7.0",
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Slider(
                modifier = Modifier.testTag("onboarding-ielts-slider"),
                value = uiState.sampleIelts.toFloat(),
                onValueChange = { onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(it.toDouble())) },
                valueRange = 6f..8f,
                steps = 3,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("6.0", color = AppTheme.colors.text.secondary)
                Spacer(modifier = Modifier.weight(1f))
                Text("8.0", color = AppTheme.colors.text.secondary)
            }
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding-try-ielts"),
                text = "Try IELTS 7.0",
                style = if (uiState.sampleIelts >= 7.0) ButtonStyle.ALTERNATIVE else ButtonStyle.PRIMARY,
                onClick = { onUiEvent(OnBoardingUiEvent.TryIeltsSeven) },
            )
        }
    }
    DoorPreviewGrid(isUnlocked = uiState.sampleIelts >= 7.0)
}

@Composable
private fun ResultStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    StepHeading(
        eyebrow = "STEP 3 · THE RECEIPT",
        title = "One step opens four doors",
        body = "The result is useful only when Porta can show why each state changed.",
    )
    SampleBadge()
    Surface(
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "4 DOORS OPENED",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
            Text(
                "IELTS 6.5 → 7.0",
                style = AppTheme.typography.h3,
                color = AppTheme.colors.text.primary,
            )
            Text(
                "This is a deterministic rule result—not a prediction that the student will be accepted.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
    DoorPreviewGrid(isUnlocked = true)
    ReceiptCard(
        isExpanded = uiState.isReceiptExpanded,
        onToggle = { onUiEvent(OnBoardingUiEvent.ReceiptToggled) },
    )
}

@Composable
private fun ReceiptCard(
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding-receipt"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Why Aurora Scholars changed", style = AppTheme.typography.h6)
                    Text(
                        "Hard rule · IELTS ≥ 7.0",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(if (isExpanded) "Hide" else "Inspect")
                }
            }
            if (isExpanded) {
                Text("Actual in scenario: 7.0 band", color = AppTheme.colors.text.primary)
                Text("Required by sample rule: 7.0 band", color = AppTheme.colors.text.primary)
                Text(
                    "Synthetic demo only · no official provider source. Real opportunity rules must carry a source and verification date.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun NextMoveStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    StepHeading(
        eyebrow = "STEP 4 · YOUR NEXT MOVE",
        title = "Keep exploring or build your map",
        body = "You stay in control. Sample facts never become your real profile.",
    )
    SampleBadge()
    ChoiceCard(
        title = "Build my real map",
        description = "Enter only the minimum facts needed to evaluate your first doors.",
        action = "Start profile",
        primary = true,
        testTag = "onboarding-build-profile",
        onClick = { onUiEvent(OnBoardingUiEvent.BuildMyMap) },
    )
    ChoiceCard(
        title = "Explore the sample map",
        description = "Browse Doors, receipts, and the Action Frontier with clearly labelled sample data.",
        action = "Explore sample",
        primary = false,
        testTag = "onboarding-explore-sample",
        onClick = { onUiEvent(OnBoardingUiEvent.ExploreSample) },
    )
    Text(
        text = "Focus: ${uiState.selectedGoal?.label ?: "Not selected"} · Stored locally on this device",
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.text.secondary,
        style = AppTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ChoiceCard(
    title: String,
    description: String,
    action: String,
    primary: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (primary) AppTheme.colors.primary else AppTheme.colors.surfaceContainer,
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (primary) null else BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = AppTheme.typography.h5,
                color = if (primary) AppTheme.colors.onPrimary else AppTheme.colors.text.primary,
            )
            Text(
                description,
                style = AppTheme.typography.bodyMedium,
                color = if (primary) {
                    AppTheme.colors.onPrimary.copy(alpha = 0.82f)
                } else {
                    AppTheme.colors.text.secondary
                },
            )
            Text(
                "$action  →",
                fontWeight = FontWeight.Bold,
                color = if (primary) AppTheme.colors.onPrimary else AppTheme.colors.primary,
            )
        }
    }
}

@Composable
private fun DoorPreviewGrid(isUnlocked: Boolean) {
    val doors = listOf(
        "Aurora Scholars",
        "Forge Student Lab",
        "Horizon Exchange",
        "Spark Fellowship",
    )
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
        Text(
            text = if (isUnlocked) "4 doors now meet the sample rule" else "4 doors need one measurable step",
            style = AppTheme.typography.h6,
            color = AppTheme.colors.text.primary,
        )
        doors.chunked(2).forEach { rowDoors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            ) {
                rowDoors.forEach { title ->
                    PortalDoor(
                        modifier = Modifier.weight(1f),
                        title = title,
                        isUnlocked = isUnlocked,
                        testTag = "onboarding-door-${doors.indexOf(title)}",
                    )
                }
            }
        }
    }
}

@Composable
private fun PortalDoor(
    modifier: Modifier,
    title: String,
    isUnlocked: Boolean,
    testTag: String,
) {
    Card(
        modifier = modifier
            .height(142.dp)
            .testTag(testTag)
            .semantics { stateDescription = if (isUnlocked) "Open" else "Needs IELTS 7.0" },
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) AppTheme.colors.alternative else AppTheme.colors.surfaceContainer,
        ),
        shape = RoundedCornerShape(
            topStart = 64.dp,
            topEnd = 64.dp,
            bottomStart = 18.dp,
            bottomEnd = 18.dp,
        ),
        border = BorderStroke(
            if (isUnlocked) 2.dp else 1.dp,
            if (isUnlocked) AppTheme.colors.primary else AppTheme.colors.outline.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (isUnlocked) "OPEN" else "ONE GAP",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = title,
                modifier = Modifier.padding(top = 6.dp),
                color = AppTheme.colors.text.primary,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SampleBadge() {
    Surface(
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(100),
    ) {
        Text(
            text = "SAMPLE PROFILE · FICTIONAL DATA",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        )
    }
}

@Composable
private fun StepHeading(
    eyebrow: String,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier.padding(top = AppTheme.spacing.largeSpacing),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            eyebrow,
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
        Text(
            title,
            color = AppTheme.colors.text.primary,
            style = AppTheme.typography.h2,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            body,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyExtraLarge,
        )
    }
}

@Composable
private fun OnboardingBottomBar(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.background,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .padding(AppTheme.spacing.outerSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            if (uiState.currentStep > 0) {
                AppButton(
                    modifier = Modifier.weight(0.42f),
                    text = "Back",
                    style = ButtonStyle.ALTERNATIVE,
                    onClick = { onUiEvent(OnBoardingUiEvent.Back) },
                )
            }
            AppButton(
                modifier = Modifier
                    .weight(1f)
                    .testTag("onboarding-continue"),
                text = when (uiState.currentStep) {
                    0 -> "Try a sample"
                    1 -> "See what changed"
                    else -> "Choose my next move"
                },
                enabled = when (uiState.currentStep) {
                    0 -> uiState.selectedGoal != null
                    1 -> uiState.sampleIelts >= 7.0
                    else -> true
                },
                onClick = { onUiEvent(OnBoardingUiEvent.Next) },
            )
        }
    }
}

private fun Double.asBand(): String = "${toInt()}.${((this * 10).toInt() % 10)}"
