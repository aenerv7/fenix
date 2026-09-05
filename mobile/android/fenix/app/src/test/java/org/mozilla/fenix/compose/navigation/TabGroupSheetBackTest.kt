/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.compose.navigation

import androidx.activity.ComponentDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.compose.base.utils.LocalUnderTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabgroups.ExpandedTabGroup
import org.mozilla.fenix.tabgroups.ExpandedTabGroupActions
import org.mozilla.fenix.tabstray.controller.NoOpTabInteractionHandler
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState.Mode
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import kotlin.test.assertEquals

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28, 35])
class TabGroupSheetBackTest {
    @get:Rule
    val compose = createComposeRule()

    private val tab = createTab("https://example.com")
    private val group = createTabGroup(tabs = listOf(tab))
    private val mode = mutableStateOf<Mode>(Mode.Normal)
    private val stack = mutableStateListOf("root", "group")
    private var navigationCount = 0
    private var dismissalCount = 0

    @Test
    fun `system Back with no selection invokes navigation without dismissing the sheet`() {
        showGroup()
        pressBack()
        compose.runOnIdle {
            assertEquals(1, navigationCount)
            assertEquals(0, dismissalCount)
        }
    }

    @Test
    fun `Back clears selection and the next Back invokes navigation`() {
        mode.value = Mode.Select(setOf(tab), tabGroupId = group.id)
        showGroup()
        pressBack()
        compose.runOnIdle {
            assertEquals(Mode.Normal, mode.value)
            assertEquals(0, navigationCount)
            assertEquals(0, dismissalCount)
        }
        pressBack()
        compose.runOnIdle { assertEquals(1, navigationCount) }
    }

    @Test
    fun `Back in empty selection mode invokes navigation`() {
        mode.value = Mode.Select(tabGroupId = group.id)
        showGroup()
        pressBack()
        compose.runOnIdle {
            assertEquals(1, navigationCount)
            assertEquals(0, dismissalCount)
        }
    }

    @Test
    fun `manual dismissal does not invoke focused tab navigation`() {
        showGroup()
        compose.onNodeWithContentDescription("Dismiss group").performSemanticsAction(SemanticsActions.OnClick)
        compose.runOnIdle {
            assertEquals(0, navigationCount)
            assertEquals(1, dismissalCount)
            assertEquals(listOf("root"), stack.toList())
        }
    }

    @Test
    fun `dragging the sheet down dismisses without focused tab navigation`() {
        showGroup()
        compose.onNodeWithContentDescription("Dismiss group").performTouchInput {
            swipeDown(endY = bottom + 1000f)
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, navigationCount)
            assertEquals(1, dismissalCount)
        }
    }

    @Test
    fun `manual dismissal with selected tabs clears selection and collapses`() {
        mode.value = Mode.Select(setOf(tab), tabGroupId = group.id)
        showGroup()
        compose.onNodeWithContentDescription("Dismiss group").performSemanticsAction(SemanticsActions.OnClick)
        compose.runOnIdle {
            assertEquals(0, navigationCount)
            assertEquals(1, dismissalCount)
            assertEquals(Mode.Normal, mode.value)
        }
    }

    @Test
    fun `scrim dismissal action does not invoke focused tab navigation`() {
        showGroup()
        compose.onNodeWithContentDescription("Close sheet").performSemanticsAction(SemanticsActions.OnClick)
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, navigationCount)
            assertEquals(1, dismissalCount)
        }
    }

    @Test
    fun `Back works when the focused group was opened without animation`() {
        showGroup(skipOpeningAnimation = true)
        pressBack()
        compose.runOnIdle {
            assertEquals(1, navigationCount)
            assertEquals(0, dismissalCount)
        }
    }

    private fun pressBack() {
        compose.runOnUiThread {
            (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed()
        }
        compose.waitForIdle()
    }

    private fun showGroup(skipOpeningAnimation: Boolean = false) {
        compose.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    NavDisplay(
                        backStack = stack,
                        onBack = {
                            dismissalCount++
                            mode.value = Mode.Normal
                            stack.removeAt(stack.lastIndex)
                        },
                        sceneStrategies = listOf(BottomSheetSceneStrategy()),
                        entryProvider = entryProvider {
                            entry("root") { Text("root") }
                            entry(
                                "group",
                                metadata = BottomSheetSceneStrategy.bottomSheet(
                                    skipPartiallyExpanded = true,
                                    skipOpeningAnimation = skipOpeningAnimation,
                                    handleContentDescription = "Dismiss group",
                                    modalBottomSheetProperties = ModalBottomSheetProperties(
                                        shouldDismissOnBackPress = false,
                                    ),
                                ),
                            ) {
                                ExpandedTabGroup(
                                    group = group,
                                    actions = ExpandedTabGroupActions({}, {}, {}, {}, {}, {}, {}),
                                    displayTabsInGrid = true,
                                    tabInteractionHandler = NoOpTabInteractionHandler,
                                    selectionMode = mode.value,
                                    onExitSelectMode = { mode.value = Mode.Normal },
                                    onBack = { navigationCount++ },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
