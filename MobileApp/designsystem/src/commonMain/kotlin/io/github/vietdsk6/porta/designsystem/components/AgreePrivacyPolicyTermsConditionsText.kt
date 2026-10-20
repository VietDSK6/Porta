package io.github.vietdsk6.porta.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.and
import io.github.vietdsk6.porta.designsystem.generated.resources.privacy_policy
import io.github.vietdsk6.porta.designsystem.generated.resources.terms_conditions
import io.github.vietdsk6.porta.designsystem.generated.resources.txt_agree_privacy_policy_and_terms
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.designsystem.util.PreviewHelper
import io.github.vietdsk6.porta.designsystem.util.appendLinkIfNotEmpty
import org.jetbrains.compose.resources.stringResource

@Composable
fun AgreePrivacyPolicyTermsConditionsText(
    privacyPolicyUrl: String,
    termsConditionsUrl: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val privacyPolicy = stringResource(UiRes.string.privacy_policy)
        val termsConditions = stringResource(UiRes.string.terms_conditions)
        val customStyle =
            SpanStyle(
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.text.secondary,
                textDecoration = TextDecoration.Underline,
            )
        val annotatedString =
            buildAnnotatedString {
                append(stringResource(UiRes.string.txt_agree_privacy_policy_and_terms))

                withStyle(style = customStyle) {
                    appendLinkIfNotEmpty(url = privacyPolicyUrl, text = privacyPolicy)
                }
                append(" ${stringResource(UiRes.string.and)} ")
                withStyle(customStyle) {
                    appendLinkIfNotEmpty(url = termsConditionsUrl, text = termsConditions)
                }
            }
        Text(
            text = annotatedString,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
@Preview
internal fun AgreePrivacyPolicyPreview() {
    PreviewHelper {
        AgreePrivacyPolicyTermsConditionsText(
            privacyPolicyUrl = "https://example.com/privacy-policy",
            termsConditionsUrl = "https://example.com/terms-of-service",
        )
    }
}
