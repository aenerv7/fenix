/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.ui.tabitems

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import mozilla.components.compose.base.PromoCard
import mozilla.components.ui.animation.AnimatedIllustration
import org.mozilla.fenix.R
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.theme.FirefoxTheme

private val OnboardingGridItemMinHeight = 104.dp

/**
 * Onboarding for tab groups in the tab manager when in grid view.
 *
 * @param onDismiss Invoked when the dismiss button is clicked.
 * @param modifier The Modifier.
 */
@Composable
fun TabGroupOnboardingGridItem(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PromoCard(
        description = stringResource(R.string.tab_group_onboarding_grid_item_description),
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = OnboardingGridItemMinHeight)
                .testTag(TabsTrayTestTag.TAB_GROUP_ONBOARDING_GRID_ITEM)
                .focusable(true)
                .semantics(mergeDescendants = true) {},
        title = stringResource(R.string.tab_group_onboarding_item_title),
        illustration = {
            AnimatedIllustration(
                animationResource = R.raw.mozac_ic_kit_tab_groups_animation,
                staticDrawableResource = R.drawable.mozac_ic_kit_tab_groups,
                modifier =
                    Modifier.height(OnboardingGridItemMinHeight)
                        .padding(vertical = FirefoxTheme.layout.space.static150)
                        .testTag(TabsTrayTestTag.TAB_GROUP_ONBOARDING_ILLUSTRATION),
                contentDescription = null,
                iterationDelay = 8.seconds,
            )
        },
        contentSpacing = 0.dp,
        verticalAlignment = Alignment.CenterVertically,
        closeButtonContentDescription = stringResource(R.string.tab_group_onboarding_item_dismiss_content_description),
        onDismiss = onDismiss,
    )
}

@PreviewLightDark
@Composable
private fun TabGroupOnboardingGridItemPreview() {
    FirefoxTheme {
        Surface {
            TabGroupOnboardingGridItem(
                modifier = Modifier.padding(FirefoxTheme.layout.space.static100),
                onDismiss = {},
            )
        }
    }
}
