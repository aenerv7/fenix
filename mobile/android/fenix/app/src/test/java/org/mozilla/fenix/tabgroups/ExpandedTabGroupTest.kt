/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabgroups

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import mozilla.components.compose.base.utils.LocalUnderTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabstray.LocalTabManagementFeatureHelper
import org.mozilla.fenix.tabstray.TabManagementFeatureHelper
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.tabstray.controller.NoOpTabInteractionHandler
import org.mozilla.fenix.tabstray.data.TabsTrayItem
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.ui.banner.AboveToolbarMenuPositionProvider
import org.mozilla.fenix.tabstray.ui.banner.MultiSelectMenuPlacement
import org.mozilla.fenix.tabstray.ui.banner.MultiSelectTabsTrayBanner
import org.mozilla.fenix.tabstray.ui.banner.MultiSelectTabsTrayMenu
import org.mozilla.fenix.tabstray.ui.banner.generateMultiSelectBannerMenuItems
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme

@RunWith(AndroidJUnit4::class)
class ExpandedTabGroupTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    val testGroupTitle = "Test Tab Group"

    private val tabManagementFeatureHelper = object : TabManagementFeatureHelper {
        override val openingAnimationEnabled: Boolean = false
        override val tabGroupsEnabled: Boolean = true
        override val tabGroupsDragAndDropEnabled: Boolean = false
        override val shareTabGroupEnabled: Boolean = true
        override val tabGroupsOnboardingEnabled: Boolean = false
        override val tabGroupsLiveReorderEnabled: Boolean = false
    }

    @Test
    fun verifyVisibleItems() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTabManagementFeatureHelper provides tabManagementFeatureHelper) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(),
                            onItemClick = {},
                            onTabClose = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_BOTTOM_SHEET_ROOT)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.BOTTOM_SHEET_SHARE_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_THREE_DOT_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(
            TabsTrayTestTag.BOTTOM_SHEET_CIRCLE,
            useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    @Test
    fun verifyMenuItems() {
        composeTestRule.setContent {
            FirefoxTheme(theme = Theme.Light) {
                Surface {
                    ExpandedTabGroup(
                        group = fakeTabGroup(),
                        onItemClick = {},
                        onTabClose = {},
                        onDeleteTabGroupClick = {},
                        onEditTabGroupClick = {},
                        onCloseTabGroupClick = {},
                        tabInteractionHandler = NoOpTabInteractionHandler,
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_THREE_DOT_BUTTON)
            .performClick()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.EDIT_TAB_GROUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.CLOSE_TAB_GROUP).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.DELETE_TAB_GROUP).assertIsDisplayed()
    }

    @Test
    fun verifyTabGroupItemClick() {
        var itemClicked = false
        val tab = createTab(url = "test tab")

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(tabs = mutableListOf(tab)),
                            onItemClick = {
                                if (it == tab) {
                                    itemClicked = true
                                }
                            },
                            onTabClose = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    }
                }
            }
        }
        composeTestRule
            .onNodeWithTag(TabsTrayTestTag.TAB_ITEM_ROOT)
            .performClick()

        assertTrue(itemClicked)
    }

    @Test
    fun verifyTabGroupItemLongClick() {
        var longClicked = false
        val tab = createTab(url = "test tab")

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(tabs = mutableListOf(tab)),
                            onItemClick = {},
                            onTabClose = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                            onItemLongClick = { longClicked = it == tab },
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_ITEM_ROOT)
            .performTouchInput { longClick() }

        assertTrue(longClicked)
    }

    @Test
    fun verifySelectionBannerIsShownInFrontOfTabGrid() {
        val tab = createTab(url = "test tab")
        val selectionBannerTag = "selectionBanner"
        val snackbarHostState = SnackbarHostState()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(tabs = mutableListOf(tab)),
                            onItemClick = {},
                            onTabClose = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                            selectionMode = TabsTrayState.Mode.Select(selectedTabs = setOf(tab)),
                            selectionBanner = { _, _ ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag(selectionBannerTag),
                                ) {}
                            },
                            snackbarHostState = snackbarHostState,
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(selectionBannerTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_THREE_DOT_BUTTON).assertIsDisplayed()
        val tabGridBounds = composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GRID)
            .fetchSemanticsNode().boundsInWindow
        val selectionBannerBounds = composeTestRule.onNodeWithTag(selectionBannerTag)
            .fetchSemanticsNode().boundsInWindow
        val overlayBounds = composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_FIXED_OVERLAY)
            .fetchSemanticsNode().boundsInWindow
        assertTrue(selectionBannerBounds.top < tabGridBounds.bottom)
        assertEquals(overlayBounds.top, selectionBannerBounds.top)
        assertEquals(selectionBannerBounds.bottom, overlayBounds.bottom)
    }

    @Test
    fun verifySnackbarIsShownInsideExpandedTabGroup() {
        val snackbarMessage = "Bookmark saved"
        val selectionBannerTag = "selectionBanner"
        val snackbarHostState = SnackbarHostState()
        val tab = createTab(url = "test tab")

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    LaunchedEffect(snackbarHostState) {
                        snackbarHostState.showSnackbar(snackbarMessage)
                    }
                    ExpandedTabGroup(
                        group = fakeTabGroup(tabs = mutableListOf(tab)),
                        onItemClick = {},
                        onTabClose = {},
                        onDeleteTabGroupClick = {},
                        onEditTabGroupClick = {},
                        onCloseTabGroupClick = {},
                        tabInteractionHandler = NoOpTabInteractionHandler,
                        selectionMode = TabsTrayState.Mode.Select(selectedTabs = setOf(tab)),
                        selectionBanner = { _, _ ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag(selectionBannerTag),
                            ) {}
                        },
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(snackbarMessage).assertIsDisplayed()
        composeTestRule.onNodeWithTag(selectionBannerTag).assertIsDisplayed()
        val snackbarBounds = composeTestRule.onNodeWithText(snackbarMessage)
            .fetchSemanticsNode().boundsInWindow
        val selectionBannerBounds = composeTestRule.onNodeWithTag(selectionBannerTag)
            .fetchSemanticsNode().boundsInWindow
        assertTrue(snackbarBounds.bottom <= selectionBannerBounds.top)
    }

    @Test
    fun verifySelectionBannerPositionDoesNotFollowSheet() {
        val windowSize = IntSize(width = 320, height = 640)
        val toolbarAndNavigationBarSize = IntSize(width = 320, height = 72)
        val expectedPosition = IntOffset(x = 0, y = 568)
        val positionProvider = FixedBottomPopupPositionProvider()

        val positionForCompactSheet = positionProvider.calculatePosition(
            anchorBounds = IntRect(left = 0, top = 400, right = 320, bottom = 640),
            windowSize = windowSize,
            layoutDirection = LayoutDirection.Ltr,
            popupContentSize = toolbarAndNavigationBarSize,
        )
        val positionForExpandedSheet = positionProvider.calculatePosition(
            anchorBounds = IntRect(left = 0, top = 40, right = 320, bottom = 640),
            windowSize = windowSize,
            layoutDirection = LayoutDirection.Ltr,
            popupContentSize = toolbarAndNavigationBarSize,
        )

        assertEquals(expectedPosition, positionForCompactSheet)
        assertEquals(expectedPosition, positionForExpandedSheet)
    }

    @Test
    fun verifySelectionBannerIsRemovedAfterSelectionModeEnds() {
        val tab = createTab(url = "test tab")
        val selectionBannerTag = "selectionBanner"
        val selectionMode = mutableStateOf<TabsTrayState.Mode>(
            TabsTrayState.Mode.Select(selectedTabs = setOf(tab)),
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    ExpandedTabGroup(
                        group = fakeTabGroup(tabs = mutableListOf(tab)),
                        onItemClick = {},
                        onTabClose = {},
                        onDeleteTabGroupClick = {},
                        onEditTabGroupClick = {},
                        onCloseTabGroupClick = {},
                        tabInteractionHandler = NoOpTabInteractionHandler,
                        selectionMode = selectionMode.value,
                        selectionBanner = { _, _ ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag(selectionBannerTag),
                            ) {}
                        },
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(selectionBannerTag).assertIsDisplayed()
        composeTestRule.runOnIdle {
            selectionMode.value = TabsTrayState.Mode.Normal
        }
        composeTestRule.onNodeWithTag(selectionBannerTag).assertDoesNotExist()
    }

    @Test
    fun verifyBottomToolbarMenuOpensAboveToolbar() {
        val tab = createTab(url = "test tab")
        composeTestRule.mainClock.autoAdvance = false
        val menuItems = generateMultiSelectBannerMenuItems(
            shouldShowInactiveButton = false,
            shouldShowAddToTabGroupButton = true,
            shouldShowRemoveFromTabGroupButton = true,
            onShareSelectedTabs = {},
            onSaveToCollectionsClick = {},
            onMakeSelectedTabsInactive = {},
            onAddToTabGroup = {},
            onRemoveFromTabGroup = {},
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Dark) {
                    ExpandedTabGroup(
                        group = fakeTabGroup(tabs = mutableListOf(tab)),
                        onItemClick = {},
                        onTabClose = {},
                        onDeleteTabGroupClick = {},
                        onEditTabGroupClick = {},
                        onCloseTabGroupClick = {},
                        tabInteractionHandler = NoOpTabInteractionHandler,
                        selectionMode = TabsTrayState.Mode.Select(selectedTabs = setOf(tab)),
                        selectionBanner = { menuExpanded, onMenuExpandedChange ->
                            MultiSelectTabsTrayBanner(
                                selectedTabCount = 1,
                                shouldShowInactiveButton = false,
                                shouldShowAddToTabGroupButton = true,
                                shouldShowRemoveFromTabGroupButton = true,
                                windowInsets = WindowInsets(0),
                                menuPlacement = MultiSelectMenuPlacement.AboveToolbar,
                                menuItems = menuItems,
                                menuExpanded = menuExpanded,
                                onMenuExpandedChange = onMenuExpandedChange,
                                onExitSelectModeClick = {},
                                onSaveToCollectionsClick = {},
                                onShareSelectedTabs = {},
                                onBookmarkSelectedTabsClick = {},
                                onCloseSelectedTabsClick = {},
                                onMakeSelectedTabsInactive = {},
                                onAddToTabGroup = {},
                            )
                        },
                        selectionMenu = { menuExpanded, onMenuExpandedChange ->
                            MultiSelectTabsTrayMenu(
                                visible = menuExpanded,
                                menuItems = menuItems,
                                onDismissRequest = { onMenuExpandedChange(false) },
                            )
                        },
                    )
                }
            }
        }

        val toolbarButton = composeTestRule.onNodeWithTag(TabsTrayTestTag.THREE_DOT_BUTTON)
        val toolbarBoundsBeforeMenu = toolbarButton.fetchSemanticsNode().boundsInWindow

        toolbarButton.performClick()
        repeat(20) {
            composeTestRule.mainClock.advanceTimeBy(16L)
            assertEquals(toolbarBoundsBeforeMenu, toolbarButton.fetchSemanticsNode().boundsInWindow)
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.REMOVE_FROM_TAB_GROUP).assertIsDisplayed()
        val toolbarBoundsWithMenu = toolbarButton.fetchSemanticsNode().boundsInWindow

        assertEquals(toolbarBoundsBeforeMenu, toolbarBoundsWithMenu)

        composeTestRule.onNodeWithTag(TabsTrayTestTag.REMOVE_FROM_TAB_GROUP).performClick()
        repeat(20) {
            composeTestRule.mainClock.advanceTimeBy(16L)
            assertEquals(toolbarBoundsBeforeMenu, toolbarButton.fetchSemanticsNode().boundsInWindow)
        }
    }

    @Test
    fun verifyBottomToolbarMenuPosition() {
        val windowSize = IntSize(width = 320, height = 640)
        val menuSize = IntSize(width = 280, height = 240)
        val toolbarHeight = 48
        val navigationBarInset = 24
        val bottomPadding = 4
        val positionProvider = AboveToolbarMenuPositionProvider(
            endPaddingPx = 8,
            bottomPaddingPx = bottomPadding,
            toolbarHeightPx = toolbarHeight,
            navigationBarBottomInsetPx = navigationBarInset,
        )

        val menuPosition = positionProvider.calculatePosition(
            anchorBounds = IntRect.Zero,
            windowSize = windowSize,
            layoutDirection = LayoutDirection.Ltr,
            popupContentSize = menuSize,
        )
        val toolbarTop = windowSize.height - navigationBarInset - toolbarHeight

        assertEquals(32, menuPosition.x)
        assertEquals(toolbarTop - bottomPadding - menuSize.height, menuPosition.y)
        assertTrue(menuPosition.y + menuSize.height <= toolbarTop)
    }

    @Test
    fun verifyTabGroupItemCloseClick() {
        var itemClosed = false
        val tab = createTab(url = "test tab")

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(tabs = mutableListOf(tab)),
                            onItemClick = {},
                            onTabClose = {
                                if (it == tab) {
                                    itemClosed = true
                                }
                            },
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    }
                }
            }
        }
        composeTestRule
            .onNodeWithTag(TabsTrayTestTag.TAB_ITEM_CLOSE)
            .performClick()

        assertTrue(itemClosed)
    }

    @Test
    fun verifyDeleteTabGroupClick() {
        var deleteClicked = false
        val group = fakeTabGroup()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = group,
                            onItemClick = {},
                            onTabClose = {},
                            onDeleteTabGroupClick = {
                                deleteClicked = true
                            },
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_THREE_DOT_BUTTON)
            .performClick()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.DELETE_TAB_GROUP)
            .performClick()

        assertTrue(deleteClicked)
    }

    @Test
    fun verifyEditTabGroupClick() {
        var editClicked = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(),
                            onItemClick = {},
                            onTabClose = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {
                                editClicked = true
                            },
                            onCloseTabGroupClick = {},
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_THREE_DOT_BUTTON)
            .performClick()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.EDIT_TAB_GROUP)
            .performClick()

        assertTrue(editClicked)
    }

    @Test
    fun verifyCloseTabGroupClick() {
        var closeClicked = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUnderTest provides true) {
                FirefoxTheme(theme = Theme.Light) {
                    Surface {
                        ExpandedTabGroup(
                            group = fakeTabGroup(),
                            onItemClick = {},
                            onTabClose = {},
                            onDeleteTabGroupClick = {},
                            onEditTabGroupClick = {},
                            onCloseTabGroupClick = { closeClicked = true },
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TabsTrayTestTag.TAB_GROUP_THREE_DOT_BUTTON)
            .performClick()
        composeTestRule.onNodeWithTag(TabsTrayTestTag.CLOSE_TAB_GROUP)
            .performClick()

        assertTrue(closeClicked)
    }

    private fun fakeTabGroup(
        tabs: MutableList<TabsTrayItem.Tab> = mutableListOf(),
    ): TabsTrayItem.TabGroup {
        return createTabGroup(
            title = testGroupTitle,
            tabs = tabs,
        )
    }
}
