package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonSize
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator

private const val TOTAL_STEPS = 4
private const val MOTION_DURATION_MILLIS = 220

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

        val scrollState = remember(uiState.currentStep) { ScrollState(0) }
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
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
            .testTag("onboarding-header")
            .padding(
                start = AppTheme.spacing.outerSpacing,
                end = AppTheme.spacing.outerSpacing,
                top = AppTheme.spacing.defaultSpacing,
                bottom = AppTheme.spacing.groupedVerticalElementSpacing,
            ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PORTA",
                style = AppTheme.typography.h5,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
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
                .testTag("onboarding-progress")
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
                            if (index <= currentStep) AppTheme.colors.primary else AppTheme.colors.outline,
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
        title = "What opportunities excite you?",
        body = "Choose what you'd like to explore first. We'll bring that kind of example forward in this fictional demo.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
        OnboardingGoal.selectableEntries.forEach { goal ->
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
    val containerColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.alternative else AppTheme.colors.surfaceContainer,
        animationSpec = tween(MOTION_DURATION_MILLIS),
        label = "goal container",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.primary else AppTheme.colors.outline,
        animationSpec = tween(MOTION_DURATION_MILLIS),
        label = "goal border",
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding-goal-${goal.storageValue}")
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = borderColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
        ) {
            GoalSelectionIndicator(selected = selected)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = goal.label,
                    style = AppTheme.typography.h6,
                    color = AppTheme.colors.text.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = goal.description,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@Composable
private fun GoalSelectionIndicator(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AppTheme.colors.primary else AppTheme.colors.background)
            .border(
                width = 1.dp,
                color = if (selected) AppTheme.colors.primary else AppTheme.colors.outline,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = AppTheme.colors.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ScenarioStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    StepHeading(
        eyebrow = "STEP 2 · INTERACTIVE DEMO",
        title = "One change. More possibilities.",
        body = "Try improving a sample student's IELTS score and see what changes.",
    )
    SampleBadge(text = "INTERACTIVE DEMO · FICTIONAL DATA")
    IeltsControl(uiState = uiState, onUiEvent = onUiEvent)
    DemoImpactSummary(uiState = uiState)
}

@Composable
private fun IeltsControl(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    var previousTargetState by remember { mutableStateOf(uiState.isDemoComplete) }
    LaunchedEffect(uiState.isDemoComplete) {
        if (uiState.isDemoComplete && !previousTargetState) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        previousTargetState = uiState.isDemoComplete
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        "SAMPLE IELTS",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    )
                    Text(
                        text = uiState.sampleIelts.asBand(),
                        style = AppTheme.typography.h3,
                        color = AppTheme.colors.text.primary,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = if (uiState.isDemoComplete) {
                        AppTheme.colors.status.successContainer
                    } else {
                        AppTheme.colors.alternative
                    },
                    shape = RoundedCornerShape(100),
                ) {
                    Text(
                        text = if (uiState.isDemoComplete) "7.0 REACHED" else "TARGET 7.0",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = if (uiState.isDemoComplete) {
                            AppTheme.colors.status.success
                        } else {
                            AppTheme.colors.primary
                        },
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding-ielts-slider")
                    .semantics {
                        contentDescription = "Sample IELTS score"
                        stateDescription = uiState.sampleIelts.asBand()
                    },
                value = uiState.sampleIelts.toFloat(),
                onValueChange = { onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(it.toDouble())) },
                valueRange = SAMPLE_IELTS_BASELINE.toFloat()..SAMPLE_IELTS_TARGET.toFloat(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("6.5 · Before", color = AppTheme.colors.text.secondary)
                Spacer(modifier = Modifier.weight(1f))
                Text("7.0 · Target", color = AppTheme.colors.text.secondary)
            }
            if (uiState.isDemoComplete) {
                TextButton(
                    modifier = Modifier.align(Alignment.End).testTag("onboarding-reset-demo"),
                    onClick = { onUiEvent(OnBoardingUiEvent.ResetDemo) },
                ) {
                    Text("Replay from 6.5")
                }
            }
        }
    }
}

@Composable
private fun DemoImpactSummary(uiState: OnBoardingUiState) {
    val count = uiState.satisfiedIeltsRequirementCount
    val total = uiState.demoDoors.size
    val success = count > 0
    val containerColor by animateColorAsState(
        targetValue = if (success) {
            AppTheme.colors.status.successContainer
        } else {
            AppTheme.colors.alternative
        },
        animationSpec = tween(MOTION_DURATION_MILLIS),
        label = "impact summary",
    )
    Surface(color = containerColor, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = if (success) {
                    "$count of $total sample IELTS requirements met"
                } else {
                    "Move the score to 7.0 to test $total sample requirements"
                },
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.demoDoors.forEach { door ->
                    val dotColor by animateColorAsState(
                        targetValue = if (door.isRequirementMet) {
                            AppTheme.colors.status.success
                        } else {
                            AppTheme.colors.outline
                        },
                        animationSpec = tween(MOTION_DURATION_MILLIS),
                        label = "requirement dot",
                    )
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor))
                }
            }
            Text(
                text = uiState.demoDoors.joinToString(" · ", transform = OnboardingDemoDoorUiState::title),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            AnimatedVisibility(
                visible = success,
                enter = fadeIn(tween(MOTION_DURATION_MILLIS)),
                exit = fadeOut(tween(MOTION_DURATION_MILLIS / 2)),
            ) {
                Text(
                    text = "Other eligibility conditions are not assessed in this demo.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@Composable
private fun ResultStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    StepHeading(
        eyebrow = "STEP 3 · WHAT CHANGED",
        title = "Look what one improvement can do.",
        body = "The same IELTS change affected several fictional opportunities at once.",
    )
    BeforeAfterSummary(uiState = uiState)
    DoorPreviewGrid(doors = uiState.demoDoors)
    RequirementExplanation(
        door = uiState.demoDoors.firstOrNull(),
        isExpanded = uiState.isReceiptExpanded,
        onToggle = { onUiEvent(OnBoardingUiEvent.ReceiptToggled) },
    )
    Text(
        text = "These results show rule changes in a fictional example, not admission predictions.",
        color = AppTheme.colors.text.secondary,
        style = AppTheme.typography.bodySmall,
    )
}

@Composable
private fun BeforeAfterSummary(uiState: OnBoardingUiState) {
    Surface(color = AppTheme.colors.alternative, shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            ) {
                IeltsMoment(
                    modifier = Modifier.weight(1f),
                    label = "BEFORE",
                    value = SAMPLE_IELTS_BASELINE.asBand(),
                )
                IeltsMoment(
                    modifier = Modifier.weight(1f),
                    label = "AFTER",
                    value = uiState.sampleIelts.asBand(),
                )
            }
            Text(
                text = "${uiState.satisfiedIeltsRequirementCount} sample IELTS requirements met",
                style = AppTheme.typography.h6,
                color = AppTheme.colors.status.success,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun IeltsMoment(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
        )
        Text(text = "IELTS $value", style = AppTheme.typography.h5, color = AppTheme.colors.text.primary)
    }
}

@Composable
private fun RequirementExplanation(
    door: OnboardingDemoDoorUiState?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    door ?: return
    Card(
        modifier = Modifier.fillMaxWidth().testTag("onboarding-requirement-explanation"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Why ${door.title} changed", style = AppTheme.typography.h6)
                    Text(
                        "Sample rule · IELTS ≥ ${door.requiredIelts.asBand()}",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(if (isExpanded) "Hide" else "Inspect")
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Before: IELTS ${SAMPLE_IELTS_BASELINE.asBand()}", color = AppTheme.colors.text.primary)
                    Text("After: IELTS ${SAMPLE_IELTS_TARGET.asBand()}", color = AppTheme.colors.text.primary)
                    Text(
                        "The IELTS requirement is met. Other conditions remain outside this fictional demonstration.",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
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
        eyebrow = "STEP 4 · MAKE IT YOURS",
        title = "Now, let's find yours.",
        body = "You've seen how one change can affect multiple opportunities. Discover what your own next step could be.",
    )
    ChoiceCard(
        title = "Create your profile",
        description = "Add a few academic details to see relevant opportunities and what they require.",
        action = "Get started →",
        primary = true,
        testTag = "onboarding-build-profile",
        onClick = { onUiEvent(OnBoardingUiEvent.BuildMyMap) },
    )
    ChoiceCard(
        title = "Keep exploring",
        description = "Browse example opportunities without creating a profile.",
        action = "Browse examples →",
        primary = false,
        testTag = "onboarding-explore-sample",
        onClick = { onUiEvent(OnBoardingUiEvent.ExploreSample) },
    )
    Surface(color = AppTheme.colors.alternative, shape = RoundedCornerShape(14.dp)) {
        Text(
            text = "Example data is separate from your profile. Your profile stays on this device.",
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
    }
    uiState.selectedGoal?.let { goal ->
        Text(
            text = "Your demo focus: ${goal.label}",
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
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
            .clickable(role = Role.Button, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (primary) AppTheme.colors.primary else AppTheme.colors.surfaceContainer,
        ),
        shape = RoundedCornerShape(18.dp),
        border = if (primary) null else BorderStroke(1.dp, AppTheme.colors.outline),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
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
                color = if (primary) AppTheme.colors.onPrimary else AppTheme.colors.text.secondary,
            )
            Text(
                action,
                fontWeight = FontWeight.Bold,
                color = if (primary) AppTheme.colors.onPrimary else AppTheme.colors.primary,
            )
        }
    }
}

@Composable
private fun DoorPreviewGrid(doors: List<OnboardingDemoDoorUiState>) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
        Text(
            text = "Affected opportunities",
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
        )
        BoxWithConstraints {
            val useSingleColumn = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.5f
            if (useSingleColumn) {
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
                    doors.forEachIndexed { index, door ->
                        AnimatedDoor(index = index, door = door, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
                    doors.chunked(2).forEach { rowDoors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
                        ) {
                            rowDoors.forEach { door ->
                                AnimatedDoor(
                                    index = doors.indexOf(door),
                                    door = door,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowDoors.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedDoor(
    index: Int,
    door: OnboardingDemoDoorUiState,
    modifier: Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = true,
        enter = fadeIn(tween(MOTION_DURATION_MILLIS, delayMillis = index * 45)),
    ) {
        PortalDoor(modifier = Modifier.fillMaxWidth(), door = door)
    }
}

@Composable
private fun PortalDoor(
    modifier: Modifier,
    door: OnboardingDemoDoorUiState,
) {
    val containerColor by animateColorAsState(
        targetValue = if (door.isRequirementMet) {
            AppTheme.colors.alternative
        } else {
            AppTheme.colors.surfaceContainer
        },
        animationSpec = tween(MOTION_DURATION_MILLIS),
        label = "door container",
    )
    Card(
        modifier = modifier
            .heightIn(min = 154.dp)
            .testTag("onboarding-door-${door.opportunityId}")
            .semantics {
                stateDescription = if (door.isRequirementMet) {
                    "IELTS requirement met; other criteria not assessed"
                } else {
                    "Needs IELTS ${door.requiredIelts.asBand()}"
                }
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(
            topStart = 64.dp,
            topEnd = 64.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp,
        ),
        border = BorderStroke(
            width = if (door.isRequirementMet) 2.dp else 1.dp,
            color = if (door.isRequirementMet) AppTheme.colors.primary else AppTheme.colors.outline,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (door.isRequirementMet) {
                    "IELTS REQUIREMENT MET"
                } else {
                    "IELTS ${door.requiredIelts.asBand()} NEEDED"
                },
                color = if (door.isRequirementMet) AppTheme.colors.status.success else AppTheme.colors.primary,
                style = AppTheme.typography.bodyExtraSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = door.title,
                modifier = Modifier.padding(top = 6.dp),
                color = AppTheme.colors.text.primary,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Other criteria not assessed",
                modifier = Modifier.padding(top = 6.dp),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyExtraSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SampleBadge(text: String) {
    Surface(color = AppTheme.colors.alternative, shape = RoundedCornerShape(100)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodyExtraSmall,
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
        modifier = Modifier.padding(top = AppTheme.spacing.sectionHeaderSpacing),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            eyebrow,
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
        Text(title, color = AppTheme.colors.text.primary, style = AppTheme.typography.h3)
        Text(body, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyLarge)
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
        border = BorderStroke(1.dp, AppTheme.colors.outline),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.outerSpacing, vertical = 12.dp),
            ) {
                val useStackedActions = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.5f
                if (useStackedActions) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OnboardingContinueButton(
                            uiState = uiState,
                            onContinue = { onUiEvent(OnBoardingUiEvent.Next) },
                        )
                        if (uiState.currentStep > 0) {
                            OnboardingBackButton(
                                modifier = Modifier.fillMaxWidth(),
                                onBack = { onUiEvent(OnBoardingUiEvent.Back) },
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
                    ) {
                        if (uiState.currentStep > 0) {
                            OnboardingBackButton(
                                onBack = { onUiEvent(OnBoardingUiEvent.Back) },
                            )
                        }
                        OnboardingContinueButton(
                            modifier = Modifier.weight(1f),
                            uiState = uiState,
                            onContinue = { onUiEvent(OnBoardingUiEvent.Next) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        modifier = modifier.heightIn(min = 48.dp),
        onClick = onBack,
    ) {
        Text("Back", color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OnboardingContinueButton(
    uiState: OnBoardingUiState,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppButton(
        modifier = modifier.fillMaxWidth().testTag("onboarding-continue"),
        text = when (uiState.currentStep) {
            0 -> "See what's possible →"
            1 -> "See what changed →"
            else -> "Make it personal →"
        },
        size = ButtonSize.SMALL,
        enabled = when (uiState.currentStep) {
            0 -> uiState.selectedGoal != null
            1 -> uiState.isDemoComplete
            else -> true
        },
        onClick = onContinue,
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun GoalStepPreview() {
    AppTheme(isDarkMode = true) {
        OnBoardingScreenVariation1(
            uiState = previewOnboardingState(currentStep = 0, sampleIelts = SAMPLE_IELTS_BASELINE),
            onUiEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ScenarioStepPreview() {
    AppTheme(isDarkMode = true) {
        OnBoardingScreenVariation1(
            uiState = previewOnboardingState(currentStep = 1, sampleIelts = SAMPLE_IELTS_TARGET),
            onUiEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ResultStepPreview() {
    AppTheme(isDarkMode = true) {
        OnBoardingScreenVariation1(
            uiState = previewOnboardingState(currentStep = 2, sampleIelts = SAMPLE_IELTS_TARGET),
            onUiEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NextMoveStepPreview() {
    AppTheme(isDarkMode = true) {
        OnBoardingScreenVariation1(
            uiState = previewOnboardingState(currentStep = 3, sampleIelts = SAMPLE_IELTS_TARGET),
            onUiEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun ResultStepCompactAccessibilityPreview() {
    AppTheme(isDarkMode = true) {
        OnBoardingScreenVariation1(
            uiState = previewOnboardingState(currentStep = 2, sampleIelts = SAMPLE_IELTS_TARGET),
            onUiEvent = {},
        )
    }
}

private fun previewOnboardingState(
    currentStep: Int,
    sampleIelts: Double,
): OnBoardingUiState = OnBoardingUiState(
    currentStep = currentStep,
    selectedGoal = OnboardingGoal.SCHOLARSHIPS,
    sampleIelts = sampleIelts,
    demoDoors = buildOnboardingDemoDoors(
        eligibilityEvaluator = EligibilityEvaluator(),
        sampleIelts = sampleIelts,
        selectedGoal = OnboardingGoal.SCHOLARSHIPS,
    ),
    isLoading = false,
)

private fun Double.asBand(): String = "${toInt()}.${((this * 10).toInt() % 10)}"
