/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.trustpanel.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.R
import org.mozilla.fenix.components.menu.store.IPProtectionMenuState
import org.mozilla.fenix.settings.trustpanel.store.WebsiteInfoState
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme

@RunWith(AndroidJUnit4::class)
class ProtectionPanelTest {
    @get:Rule val composeTestRule = createComposeRule()

    private val resources
        get() = ApplicationProvider.getApplicationContext<Context>().resources

    private val appName
        get() = resources.getString(R.string.app_name_firefox)

    private val protectedOnGuardTitle
        get() = resources.getString(R.string.protection_panel_banner_protected_title, appName)

    private fun blockedTrackersDescription(count: Int): String =
        resources.getQuantityString(
            R.plurals.protection_panel_banner_protected_blocked_trackers_description,
            count,
            count,
        )

    private fun gradientBannerContentDescription(count: Int): String =
        "$protectedOnGuardTitle. ${blockedTrackersDescription(count)}"

    @Test
    fun `WHEN multiple trackers were blocked THEN plural banner is shown`() {
        setProtectionPanel(numberOfTrackersBlocked = 5)

        composeTestRule.onNodeWithContentDescription(gradientBannerContentDescription(5)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(resources.getString(R.string.protection_panel_num_trackers_blocked, 5))
            .assertDoesNotExist()
    }

    @Test
    fun `WHEN banner is shown with trackers blocked THEN clicking it invokes the trackers callback`() {
        var clicked = false
        setProtectionPanel(
            numberOfTrackersBlocked = 5,
            onTrackerBlockedMenuClick = { clicked = true },
        )

        composeTestRule.onNodeWithContentDescription(gradientBannerContentDescription(5)).performClick()
        assertTrue(clicked)
    }

    @Test
    fun `WHEN a single tracker was blocked THEN singular banner is shown`() {
        setProtectionPanel(numberOfTrackersBlocked = 1)

        composeTestRule.onNodeWithContentDescription(gradientBannerContentDescription(1)).assertIsDisplayed()
    }

    @Test
    fun `WHEN no trackers were blocked THEN gradient banner is shown with no-blocked-trackers description`() {
        setProtectionPanel(numberOfTrackersBlocked = 0)

        composeTestRule.onNodeWithContentDescription(protectedOnGuardTitle, substring = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(blockedTrackersDescription(1), substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun `WHEN the site is not secure THEN the not secure banner is shown`() {
        setProtectionPanel(numberOfTrackersBlocked = 0, isWebsiteSecured = false)

        composeTestRule
            .onNodeWithContentDescription(
                "${resources.getString(R.string.protection_panel_banner_not_secure_title)}. " +
                    resources.getString(R.string.protection_panel_banner_not_secure_description)
            )
            .assertIsDisplayed()
    }

    @Test
    fun `WHEN tracking protection is disabled THEN the not protected banner is shown`() {
        setProtectionPanel(numberOfTrackersBlocked = 0, isTrackingProtectionEnabled = false)

        composeTestRule
            .onNodeWithContentDescription(
                "${resources.getString(R.string.protection_panel_banner_not_protected_title)}. " +
                    resources.getString(
                        R.string.protection_panel_banner_not_protected_description,
                        appName,
                    )
            )
            .assertIsDisplayed()
    }

    @Test
    fun `WHEN tab is private THEN clear site data menu is not shown`() {
        setProtectionPanel(
            numberOfTrackersBlocked = 0,
            isPrivate = true,
        )

        composeTestRule
            .onNodeWithText(resources.getString(R.string.clear_site_data), useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `WHEN tab is not private THEN clear site data menu is shown`() {
        setProtectionPanel(
            numberOfTrackersBlocked = 0,
            isPrivate = false,
        )

        composeTestRule
            .onNodeWithText(resources.getString(R.string.clear_site_data), useUnmergedTree = true)
            .assertExists()
    }

    private fun setProtectionPanel(
        numberOfTrackersBlocked: Int,
        isPrivate: Boolean = false,
        isWebsiteSecured: Boolean = true,
        isTrackingProtectionEnabled: Boolean = true,
        onTrackerBlockedMenuClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            FirefoxTheme(theme = Theme.Light) {
                ProtectionPanel(
                    websiteInfoState =
                        WebsiteInfoState(
                            isSecured = isWebsiteSecured,
                            websiteUrl = "https://www.mozilla.org",
                            websiteTitle = "Mozilla",
                            certificate = null,
                        ),
                    ipProtectionMenuState = IPProtectionMenuState(),
                    icon = null,
                    isTrackingProtectionEnabled = isTrackingProtectionEnabled,
                    isGlobalTrackingProtectionEnabled = true,
                    isLocalPdf = false,
                    isPrivate = isPrivate,
                    showIPProtection = false,
                    numberOfTrackersBlocked = numberOfTrackersBlocked,
                    websitePermissions = emptyList(),
                    onTrackerBlockedMenuClick = onTrackerBlockedMenuClick,
                    onTrackingProtectionToggleClick = {},
                    onClearSiteDataMenuClick = {},
                    onPrivacySecuritySettingsClick = {},
                    onAutoplayValueClick = {},
                    onToggleablePermissionClick = {},
                    onViewCertificateClick = {},
                    onViewQWACClick = {},
                    onIPProtectionToggle = {},
                    onIPProtectionNavigate = {},
                )
            }
        }
    }
}
