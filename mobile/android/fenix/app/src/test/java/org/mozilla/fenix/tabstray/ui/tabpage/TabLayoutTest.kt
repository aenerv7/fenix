/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.ui.tabpage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import kotlin.test.assertEquals
import mozilla.components.compose.base.utils.LocalUnderTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.tabstray.controller.TabInteractionHandler
import org.mozilla.fenix.tabstray.data.TabsTrayItem
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.ui.tabitems.TabGridColumnCountKey
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme

// Number of tabs supplied to the layout under test.
private const val TAB_COUNT = 10

// The list layout is a single column grid.
private const val LIST_COLUMN_COUNT = 1

// Long enough for tab item appearance, placement and disappearance animations to settle.
private const val ANIMATION_SETTLE_MS = 2000L

@RunWith(AndroidJUnit4::class)
class TabLayoutTest {
    @get:Rule val composeTestRule = createComposeRule()

    private val tabletLandscapeSize = DpSize(1280.dp, 800.dp)
    private val tabletPortraitSize = DpSize(800.dp, 1280.dp)

    @Test
    fun `WHEN the container for TabLayout is large on a large device in landscape THEN 5 columns are created`() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(tabletLandscapeSize)) {
                GridContainer(1280.dp)
            }
        }

        assertEquals(5, gridColumnCount)
    }

    @Test
    fun `WHEN the container for TabLayout is medium with a large device in landscape THEN 4 columns are created`() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(tabletLandscapeSize)) {
                GridContainer(800.dp)
            }
        }

        assertEquals(3, gridColumnCount)
    }

    @Test
    fun `WHEN the container for TabLayout is small with a large device in landscape THEN 3 columns are created`() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(tabletLandscapeSize)) {
                GridContainer(500.dp)
            }
        }

        assertEquals(3, gridColumnCount)
    }

    @Test
    fun `WHEN the container for TabLayout is large on a large device in portrait THEN 5 columns are created`() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(tabletPortraitSize)) {
                GridContainer(1280.dp)
            }
        }

        assertEquals(4, gridColumnCount)
    }

    @Test
    fun `WHEN the container for TabLayout is small with a large device in portrait THEN 3 columns are created`() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(tabletPortraitSize)) {
                GridContainer(400.dp)
            }
        }

        assertEquals(2, gridColumnCount)
    }

    @Test
    fun `WHEN screen width is large THEN columnCount is 4 in portrait`() {
        assertEquals(expected = 4, numberOfGridColumnsPortrait(screenWidthDp = 800f))
    }

    @Test
    fun `WHEN screen width is medium THEN columnCount is 3 in portrait`() {
        assertEquals(expected = 3, numberOfGridColumnsPortrait(screenWidthDp = 500f))
    }

    @Test
    fun `WHEN screen width is small THEN columnCount is 2 in portrait`() {
        assertEquals(expected = 2, numberOfGridColumnsPortrait(screenWidthDp = 200f))
    }

    @Test
    fun `WHEN screen width is large THEN columnCount is 5 in landscape`() {
        assertEquals(expected = 5, numberOfGridColumnsLandscape(screenWidthDp = 1280f))
    }

    @Test
    fun `WHEN screen width is medium THEN columnCount is 4 in landscape`() {
        assertEquals(expected = 4, numberOfGridColumnsLandscape(screenWidthDp = 1000f))
    }

    @Test
    fun `WHEN screen width is small THEN columnCount is 3 in landscape`() {
        assertEquals(expected = 3, numberOfGridColumnsLandscape(screenWidthDp = 200f))
    }

    @Test
    fun `GIVEN the tab group onboarding card is shown in grid view WHEN onboarding is no longer displayed THEN the card is removed`() {
        var displayTabGroupOnboarding by mutableStateOf(true)
        composeTestRule.setContent {
            ComposableUnderTest(
                displayTabsInGrid = true,
                displayTabGroupOnboarding = displayTabGroupOnboarding,
            )
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_ONBOARDING_GRID_ITEM).assertExists()

        displayTabGroupOnboarding = false

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_ONBOARDING_GRID_ITEM).assertDoesNotExist()
    }

    @Test
    fun `GIVEN the tab group onboarding card is shown in list view WHEN onboarding is no longer displayed THEN the card is removed`() {
        var displayTabGroupOnboarding by mutableStateOf(true)
        composeTestRule.setContent {
            ComposableUnderTest(
                displayTabsInGrid = false,
                displayTabGroupOnboarding = displayTabGroupOnboarding,
            )
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_ONBOARDING_LIST_ITEM).assertExists()

        displayTabGroupOnboarding = false

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_ONBOARDING_LIST_ITEM).assertDoesNotExist()
    }

    @Test
    fun `WHEN the selected tab becomes a tab group THEN the group is scrolled into view`() {
        val selectedTab =
            createTab(
                id = "selected-tab",
                title = "Selected tab",
                url = "https://www.mozilla.org/selected",
            )
        val groupTab = createTab(id = "group-tab", url = "https://www.mozilla.org/group")
        val group =
            createTabGroup(
                id = "target-group",
                title = "Target group",
                tabs = listOf(groupTab),
            )
        val standaloneTabs =
            List(12) { index ->
                createTab(
                    id = "tab-$index",
                    title = "Tab $index",
                    url = "https://www.mozilla.org/$index",
                )
            }
        var tabs by mutableStateOf<List<TabsTrayItem>>(listOf(group) + standaloneTabs + selectedTab)
        var selectedItemIndex by mutableStateOf(tabs.lastIndex)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        TabLayout(
                            tabs = tabs,
                            displayTabsInGrid = false,
                            dragAndDropEnabled = true,
                            displayTabGroupOnboarding = false,
                            selectedItemIndex = selectedItemIndex,
                            selectionMode = TabsTrayState.Mode.Normal,
                            focusEnabled = true,
                            tabInteractionHandler = fakeTabInteractionHandler(),
                            onTabClose = {},
                            onItemClick = {},
                            onItemLongClick = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            onShareTabGroupClick = {},
                            onTabGroupOnboardingDismiss = {},
                            liveReorderEnabled = false,
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        tabs = listOf(group.copy(tabs = group.tabs + selectedTab)) + standaloneTabs
        selectedItemIndex = 0
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("${TabsTrayTestTag.TAB_GROUP_ROOT}.${group.id}").assertIsDisplayed()
    }

    @Test
    fun `WHEN the last top-level tab is closed in list view THEN it is removed from the layout`() {
        assertLastTopLevelTabIsRemoved(displayTabsInGrid = false)
    }

    @Test
    fun `WHEN the last top-level tab is closed in grid view THEN it is removed from the layout`() {
        assertLastTopLevelTabIsRemoved(displayTabsInGrid = true)
    }

    @Test
    fun `GIVEN several tabs are selected WHEN they enter a group THEN none of them stays in list view`() {
        assertGroupingSelectedTabsLeavesNoStaleTab(displayTabsInGrid = false)
    }

    @Test
    fun `GIVEN several tabs are selected WHEN they enter a group THEN none of them stays in grid view`() {
        assertGroupingSelectedTabsLeavesNoStaleTab(displayTabsInGrid = true)
    }

    private fun assertGroupingSelectedTabsLeavesNoStaleTab(displayTabsInGrid: Boolean) {
        val tabA = createTab(id = "tab-a", url = "https://www.mozilla.org/a")
        val tabB = createTab(id = "tab-b", url = "https://www.mozilla.org/b")
        val tabC = createTab(id = "tab-c", url = "https://www.mozilla.org/c")
        val groupId = "new-group"
        var tabs by mutableStateOf<List<TabsTrayItem>>(listOf(tabA, tabB, tabC))
        var selectionMode by
            mutableStateOf<TabsTrayState.Mode>(TabsTrayState.Mode.Select(selectedTabs = setOf(tabA, tabB)))
        var enteringGroupId by mutableStateOf<String?>(null)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        TabLayout(
                            tabs = tabs,
                            displayTabsInGrid = displayTabsInGrid,
                            dragAndDropEnabled = true,
                            displayTabGroupOnboarding = false,
                            selectedItemIndex = 0,
                            selectionMode = selectionMode,
                            focusEnabled = true,
                            tabInteractionHandler = fakeTabInteractionHandler(),
                            onTabClose = {},
                            onItemClick = {},
                            onItemLongClick = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            onShareTabGroupClick = {},
                            onTabGroupOnboardingDismiss = {},
                            liveReorderEnabled = false,
                            enteringGroupId = enteringGroupId,
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag(TabsTrayTestTag.TAB_ITEM_ROOT).assertCountEquals(3)

        // The store removes the selected tabs from the list first, which starts their disappearance
        // animation, and only then marks the new group as entering. The main clock is driven manually so
        // the list change and the entering flag land in different frames, as they do in the app.
        composeTestRule.mainClock.autoAdvance = false
        tabs = listOf(createTabGroup(id = groupId, tabs = listOf(tabA, tabB)), tabC)
        selectionMode = TabsTrayState.Mode.Normal
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeByFrame()

        enteringGroupId = groupId
        composeTestRule.mainClock.advanceTimeBy(ANIMATION_SETTLE_MS)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        // The group card takes the place of the two grouped tabs, so only tab C is left as a tab item. In the
        // grid the group card is itself tagged as a tab item root, in the list it is not.
        val remainingTabItems = if (displayTabsInGrid) 2 else 1
        composeTestRule.onAllNodesWithTag(TabsTrayTestTag.TAB_ITEM_ROOT).assertCountEquals(remainingTabItems)
    }

    private fun assertLastTopLevelTabIsRemoved(displayTabsInGrid: Boolean) {
        val group =
            createTabGroup(
                id = "group",
                tabs = listOf(createTab(id = "grouped-tab", url = "https://www.mozilla.org/grouped")),
            )
        val ungroupedTab = createTab(id = "ungrouped-tab", url = "https://www.mozilla.org/ungrouped")
        var tabs by mutableStateOf<List<TabsTrayItem>>(listOf(group, ungroupedTab))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        TabLayout(
                            tabs = tabs,
                            displayTabsInGrid = displayTabsInGrid,
                            dragAndDropEnabled = true,
                            displayTabGroupOnboarding = false,
                            selectedItemIndex = tabs.lastIndex,
                            selectionMode = TabsTrayState.Mode.Normal,
                            focusEnabled = true,
                            tabInteractionHandler = fakeTabInteractionHandler(),
                            onTabClose = { closedTab -> tabs = tabs.filterNot { it.id == closedTab.id } },
                            onItemClick = {},
                            onItemLongClick = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            onShareTabGroupClick = {},
                            onTabGroupOnboardingDismiss = {},
                            liveReorderEnabled = false,
                        )
                    }
                }
            }
        }

        val tabItemCountWithUngroupedTab = if (displayTabsInGrid) 2 else 1
        val tabItemCountWithoutUngroupedTab = if (displayTabsInGrid) 1 else 0
        composeTestRule.onAllNodesWithTag(TabsTrayTestTag.TAB_ITEM_ROOT).assertCountEquals(tabItemCountWithUngroupedTab)
        if (displayTabsInGrid) {
            composeTestRule.mainClock.autoAdvance = false
        }
        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_ITEM_CLOSE).performClick()
        if (displayTabsInGrid) {
            composeTestRule.mainClock.advanceTimeByFrame()
        }
        composeTestRule.waitForIdle()
        composeTestRule
            .onAllNodesWithTag(TabsTrayTestTag.TAB_ITEM_ROOT)
            .assertCountEquals(tabItemCountWithoutUngroupedTab)
        composeTestRule.mainClock.autoAdvance = true
    }

    private val gridColumnCount: Int
        get() =
            composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GRID).fetchSemanticsNode().config[TabGridColumnCountKey]

    @Composable
    private fun GridContainer(width: Dp) {
        CompositionLocalProvider(LocalUnderTest provides true) {
            FirefoxTheme(theme = Theme.Light) {
                Surface {
                    Box(Modifier.requiredWidth(width)) {
                        TabLayoutGrid()
                    }
                }
            }
        }
    }

    @Composable
    private fun TabLayoutGrid(modifier: Modifier = Modifier) {
        val tabs =
            List(10) {
                createTab(url = "www.mozilla.org")
            }
        TabLayout(
            tabs = tabs,
            displayTabsInGrid = true,
            dragAndDropEnabled = true,
            displayTabGroupOnboarding = true,
            selectedItemIndex = 0,
            selectionMode = TabsTrayState.Mode.Normal,
            focusEnabled = true,
            tabInteractionHandler = fakeTabInteractionHandler(),
            modifier = modifier,
            trackersBlockedCount = 0,
            onTabClose = { _ -> },
            onItemClick = { _ -> },
            onItemLongClick = { _ -> },
            onEditTabGroupClick = { _ -> },
            onCloseTabGroupClick = { _ -> },
            onShareTabGroupClick = { _ -> },
            onDeleteTabGroupClick = { _ -> },
            onTabGroupOnboardingDismiss = {},
            onPrivacyReportTapped = {},
            liveReorderEnabled = false,
        )
    }

    private fun fakeTabInteractionHandler() =
        object : TabInteractionHandler {
            override fun onMove(sourceKey: String, targetKey: String?, placeAfter: Boolean) {
                // no op
            }

            override fun onDrop(sourceKey: String, targetKey: String) {
                // no op
            }

            override fun onDragCancel() {
                // no op
            }

            override fun onDragStart(sourceKey: String, preserveSelectMode: Boolean) {
                // no op
            }
        }

    @Composable
    private fun ComposableUnderTest(
        displayTabsInGrid: Boolean,
        displayTabGroupOnboarding: Boolean = false,
        header: (@Composable () -> Unit)? = null,
        trackersBlockedCount: Int? = null,
    ) {
        CompositionLocalProvider(LocalUnderTest provides true) {
            FirefoxTheme(theme = Theme.Light) {
                Surface {
                    TabLayout(
                        tabs = List(TAB_COUNT) { createTab(url = "www.mozilla.org") },
                        displayTabsInGrid = displayTabsInGrid,
                        dragAndDropEnabled = true,
                        displayTabGroupOnboarding = displayTabGroupOnboarding,
                        selectedItemIndex = 0,
                        selectionMode = TabsTrayState.Mode.Normal,
                        focusEnabled = true,
                        tabInteractionHandler = fakeTabInteractionHandler(),
                        onTabClose = { _ -> },
                        onItemClick = { _ -> },
                        onItemLongClick = { _ -> },
                        onDeleteTabGroupClick = { _ -> },
                        onEditTabGroupClick = { _ -> },
                        onCloseTabGroupClick = { _ -> },
                        onShareTabGroupClick = { _ -> },
                        onTabGroupOnboardingDismiss = {},
                        liveReorderEnabled = false,
                        header = header,
                        trackersBlockedCount = trackersBlockedCount,
                    )
                }
            }
        }
    }
}
