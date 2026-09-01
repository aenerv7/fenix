/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:OptIn(ExperimentalMaterial3Api::class)

package org.mozilla.fenix.tabstray.ui.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import mozilla.components.compose.base.badge.BadgedIcon
import mozilla.components.compose.base.button.IconButton
import mozilla.components.compose.base.menu.DropdownMenu
import mozilla.components.compose.base.menu.MenuItem
import mozilla.components.compose.base.text.Text
import mozilla.components.compose.base.text.value
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.Banner
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.navigation.TabManagerNavDestination.ExpandedTabGroup
import org.mozilla.fenix.tabstray.redux.action.TabGroupAction
import org.mozilla.fenix.tabstray.redux.action.TabsTrayAction
import org.mozilla.fenix.tabstray.redux.state.Page
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState.Mode
import org.mozilla.fenix.tabstray.redux.store.TabsTrayStore
import org.mozilla.fenix.tabstray.syncedtabs.SyncedTabsListItem
import org.mozilla.fenix.tabstray.ui.tabstray.TabsTray
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.ThemedValue
import org.mozilla.fenix.theme.ThemedValueProvider
import kotlin.math.max
import mozilla.components.ui.icons.R as iconsR

private const val TAB_COUNT_SHOW_CFR = 6
private const val MENU_ENTER_DURATION_MILLIS = 120
private const val MENU_ENTER_FADE_DURATION_MILLIS = 30
private const val MENU_EXIT_DURATION_MILLIS = 75
private val RowHeight = 48.dp
private val MultiSelectMenuMinWidth = 112.dp
private val MultiSelectMenuMaxWidth = 280.dp

internal enum class MultiSelectMenuPlacement {
    Dropdown,
    AboveToolbar,
}

internal fun TabsTrayState.shouldShowMultiSelectBanner(): Boolean =
    mode is Mode.Select && backStack.lastOrNull() !is ExpandedTabGroup

/**
 * Top-level UI for displaying the banner in [TabsTray].
 *
 * @param state The current snapshot of [TabsTrayState].
 * @param onAction Invoked to pass upwards a [TabsTrayAction] in response to a UI event.
 * @param onTabPageIndicatorClicked Invoked when the user clicks on a tab page indicator.
 * @param onSaveToCollectionClick Invoked when the user clicks the "Save to Collection" button in multi-select mode.
 * @param onShareSelectedTabsClick Invoked when the user clicks the "Share" button in multi-select mode.
 * @param onDeleteSelectedTabsClick Invoked when the user clicks the "Close Selected Tabs" menu item.
 * @param onBookmarkSelectedTabsClick Invoked when the user clicks the "Bookmark Selected Tabs" menu item.
 * @param onForceSelectedTabsAsInactiveClick Invoked when the user clicks the "Mark Tabs as Inactive" menu item.
 * @param onTabAutoCloseBannerViewOptionsClick Invoked when the user clicks to view auto-close settings from the banner.
 * @param onTabsTrayPbmLockedClick Invoked when the user interacts with the lock private browsing mode banner.
 * @param onTabsTrayPbmLockedDismiss Invoked when the user clicks on either button in the
 * lock private browsing mode banner.
 * @param onTabAutoCloseBannerDismiss Invoked when the user dismisses the auto-close banner.
 * @param onTabAutoCloseBannerShown Invoked when the auto-close banner is shown to the user.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun TabsTrayBanner(
    state: TabsTrayState,
    onAction: (TabsTrayAction) -> Unit,
    onTabPageIndicatorClicked: (Page) -> Unit,
    onSaveToCollectionClick: () -> Unit,
    onShareSelectedTabsClick: () -> Unit,
    onDeleteSelectedTabsClick: () -> Unit,
    onBookmarkSelectedTabsClick: () -> Unit,
    onForceSelectedTabsAsInactiveClick: () -> Unit,
    onTabAutoCloseBannerViewOptionsClick: () -> Unit,
    onTabsTrayPbmLockedClick: () -> Unit,
    onTabsTrayPbmLockedDismiss: () -> Unit,
    onTabAutoCloseBannerDismiss: () -> Unit,
    onTabAutoCloseBannerShown: () -> Unit,
) {
    val isInMultiSelectMode by remember(state.mode, state.backStack) {
        derivedStateOf {
            state.shouldShowMultiSelectBanner()
        }
    }
    val showTabAutoCloseBanner by remember(
        state.config.showTabAutoCloseBanner,
        state.normalTabsState.tabCount,
        state.privateBrowsing.tabs.size,
    ) {
        derivedStateOf {
            state.config.showTabAutoCloseBanner && max(
                state.normalTabsState.tabCount,
                state.privateBrowsing.tabs.size,
            ) >= TAB_COUNT_SHOW_CFR
        }
    }
    val syncedTabCount = remember(state.sync.syncedTabs) {
        state.sync.syncedTabs
            .filterIsInstance<SyncedTabsListItem.DeviceSection>()
            .sumOf { deviceSection: SyncedTabsListItem.DeviceSection -> deviceSection.tabs.size }
    }

    var hasAcknowledgedAutoCloseBanner by remember { mutableStateOf(false) }
    var hasAcknowledgedPbmLockBanner by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.testTag(tag = TabsTrayTestTag.BANNER_ROOT),
    ) {
        if (isInMultiSelectMode) {
            MultiSelectTabsTrayBanner(
                selectedTabCount = state.mode.selectedTabs.size,
                shouldShowInactiveButton = state.config.isInDebugMode,
                shouldShowAddToTabGroupButton = state.config.tabGroupsEnabled,
                shouldShowSaveToCollectionButton = state.config.collectionsEnabled,
                onExitSelectModeClick = { onAction(TabsTrayAction.ExitSelectMode) },
                onSaveToCollectionsClick = onSaveToCollectionClick,
                onShareSelectedTabs = onShareSelectedTabsClick,
                onBookmarkSelectedTabsClick = onBookmarkSelectedTabsClick,
                onCloseSelectedTabsClick = onDeleteSelectedTabsClick,
                onMakeSelectedTabsInactive = onForceSelectedTabsAsInactiveClick,
                onAddToTabGroup = { onAction(TabGroupAction.AddToTabGroup) },
            )
        } else {
            TabPageBanner(
                selectedPage = state.selectedPage,
                normalTabCount = state.normalTabsState.tabCount,
                privateTabCount = state.privateBrowsing.tabs.size,
                shouldShowTabGroupsPage = state.config.tabGroupsEnabled,
                tabGroupCount = state.tabGroupState.groups.size,
                shouldShowTabGroupBadge = state.shouldShowTabGroupBadge,
                syncedTabCount = syncedTabCount,
                onTabPageIndicatorClicked = onTabPageIndicatorClicked,
                hasTabDataLoaded = state.hasTabDataLoaded,
            )
        }

        when {
            !hasAcknowledgedAutoCloseBanner && showTabAutoCloseBanner -> {
                onTabAutoCloseBannerShown()

                HorizontalDivider()

                Banner(
                    message = stringResource(id = R.string.tab_tray_close_tabs_banner_message),
                    button1Text = stringResource(id = R.string.tab_tray_close_tabs_banner_negative_button_text),
                    button2Text = stringResource(id = R.string.tab_tray_close_tabs_banner_positive_button_text),
                    onButton1Click = {
                        hasAcknowledgedAutoCloseBanner = true
                        onTabAutoCloseBannerDismiss()
                    },
                    onButton2Click = {
                        hasAcknowledgedAutoCloseBanner = true
                        onTabAutoCloseBannerViewOptionsClick()
                    },
                )
            }

            !hasAcknowledgedPbmLockBanner && state.privateBrowsing.showLockBanner -> {
                // After this bug: https://bugzilla.mozilla.org/show_bug.cgi?id=1965545
                // is resolved, we should swap the button 1 and button 2 click actions.
                Banner(
                    message = stringResource(id = R.string.private_tab_cfr_title),
                    button1Text = stringResource(id = R.string.private_tab_cfr_negative),
                    button2Text = stringResource(id = R.string.private_tab_cfr_positive),
                    onButton1Click = {
                        hasAcknowledgedPbmLockBanner = true
                        onTabsTrayPbmLockedDismiss()
                    },
                    onButton2Click = {
                        hasAcknowledgedPbmLockBanner = true
                        onTabsTrayPbmLockedClick()
                        onTabsTrayPbmLockedDismiss()
                    },
                )
            }
        }
    }
}

/**
 * Banner displayed when in [Mode.Normal].
 *
 * @param selectedPage The currently-active tab [Page].
 * @param normalTabCount The amount of open Normal tabs.
 * @param privateTabCount The amount of open Private tabs.
 * @param shouldShowTabGroupsPage Whether to show the tab groups page.
 * @param tabGroupCount The amount of tab groups.
 * @param shouldShowTabGroupBadge Whether to show the new-content badge on the tab groups page button.
 * @param syncedTabCount The amount of synced tabs.
 * @param hasTabDataLoaded Whether the tab data has loaded.
 * @param onTabPageIndicatorClicked Invoked when the user clicks on a tab page button. Passes along the
 * [Page] that was clicked.
 */
@Suppress("LongParameterList")
@Composable
private fun TabPageBanner(
    selectedPage: Page,
    normalTabCount: Int,
    privateTabCount: Int,
    shouldShowTabGroupsPage: Boolean,
    tabGroupCount: Int,
    shouldShowTabGroupBadge: Boolean,
    syncedTabCount: Int,
    hasTabDataLoaded: Boolean,
    onTabPageIndicatorClicked: (Page) -> Unit,
) {
    val selectedTabIndex = Page.pageToPosition(
        page = selectedPage,
        shouldShowTabGroupsPage = shouldShowTabGroupsPage,
    )

    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(insets = TopAppBarDefaults.windowInsets),
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = Color.Transparent,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        selectedTabIndex = selectedTabIndex,
                        matchContentSize = true,
                    ),
                    width = Dp.Unspecified,
                    shape = RoundedCornerShape(
                        topStartPercent = 50,
                        topEndPercent = 50,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            divider = {},
        ) {
            TabPageBannerTabs(
                selectedPage = selectedPage,
                normalTabCount = normalTabCount,
                privateTabCount = privateTabCount,
                shouldShowTabGroupsPage = shouldShowTabGroupsPage,
                tabGroupCount = tabGroupCount,
                shouldShowTabGroupBadge = shouldShowTabGroupBadge,
                syncedTabCount = syncedTabCount,
                onTabPageIndicatorClicked = onTabPageIndicatorClicked,
                hasTabDataLoaded = hasTabDataLoaded,
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun TabPageBannerTabs(
    selectedPage: Page,
    normalTabCount: Int,
    privateTabCount: Int,
    shouldShowTabGroupsPage: Boolean,
    tabGroupCount: Int,
    shouldShowTabGroupBadge: Boolean,
    syncedTabCount: Int,
    hasTabDataLoaded: Boolean,
    onTabPageIndicatorClicked: (Page) -> Unit,
) {
    val privateTabDescription = stringResource(
        id = R.string.tabs_header_private_tabs_counter_title,
        privateTabCount.toString(),
    )
    val normalTabDescription = stringResource(
        id = R.string.tabs_header_normal_tabs_counter_title,
        normalTabCount.toString(),
    )
    val tabGroupsDescription = pluralStringResource(
        id = R.plurals.tabs_header_tab_group_counter_title,
        count = tabGroupCount,
        tabGroupCount,
    )
    val syncedTabDescription = stringResource(
        id = R.string.tabs_header_synced_tabs_counter_title,
        syncedTabCount.toString(),
    )

    BannerTab(
        selected = selectedPage == Page.PrivateTabs,
        testTag = TabsTrayTestTag.PRIVATE_TABS_PAGE_BUTTON,
        contentDescription = privateTabDescription,
        onClick = { onTabPageIndicatorClicked(Page.PrivateTabs) },
    ) {
        Icon(painterResource(iconsR.drawable.mozac_ic_private_mode_24), null)
    }

    BannerTab(
        selected = selectedPage == Page.NormalTabs,
        testTag = TabsTrayTestTag.NORMAL_TABS_PAGE_BUTTON,
        contentDescription = normalTabDescription,
        onClick = { onTabPageIndicatorClicked(Page.NormalTabs) },
    ) {
        TabCounter(
            tabCount = normalTabCount,
            showTabCount = hasTabDataLoaded,
        )
    }

    if (shouldShowTabGroupsPage) {
        BannerTab(
            selected = selectedPage == Page.TabGroups,
            testTag = TabsTrayTestTag.TAB_GROUPS_PAGE_BUTTON,
            contentDescription = tabGroupsDescription,
            onClick = { onTabPageIndicatorClicked(Page.TabGroups) },
        ) {
            BadgedIcon(
                painter = painterResource(iconsR.drawable.mozac_ic_tab_group_24),
                isHighlighted = shouldShowTabGroupBadge,
                tint = LocalContentColor.current,
            )
        }
    }

    BannerTab(
        selected = selectedPage == Page.SyncedTabs,
        testTag = TabsTrayTestTag.SYNCED_TABS_PAGE_BUTTON,
        contentDescription = syncedTabDescription,
        onClick = { onTabPageIndicatorClicked(Page.SyncedTabs) },
    ) {
        Icon(painterResource(iconsR.drawable.mozac_ic_sync_tabs_24), null)
    }
}

@Composable
private fun BannerTab(
    selected: Boolean,
    testTag: String,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription }
            .height(RowHeight),
        selectedContentColor = MaterialTheme.colorScheme.onSurface,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        content()
    }
}

/**
 * Banner displayed when in [Mode.Select].
 *
 * @param selectedTabCount The amount of selected tabs.
 * @param shouldShowInactiveButton Whether to show the inactive tabs menu item.
 * @param shouldShowAddToTabGroupButton Whether the add to tab group button should be displayed.
 * @param shouldShowSaveToCollectionButton Whether the save to collection button should be displayed.
 * @param onExitSelectModeClick Invoked when the user clicks to exit selection mode.
 * @param onSaveToCollectionsClick Invoked when the user clicks on the save to collection button.
 * @param onShareSelectedTabs Invoked when the user clicks on the share tabs button.
 * @param onBookmarkSelectedTabsClick Invoked when the user clicks the menu item to bookmark the selected tabs.
 * @param onCloseSelectedTabsClick Invoked when the user clicks the menu item to close the selected tabs.
 * @param onMakeSelectedTabsInactive Invoked when the user clicks the menu item to set the
 * selected tabs as inactive.
 * @param onAddToTabGroup Invoked when the user adds to a tab group.
 * @param onRemoveFromTabGroup Invoked when the user removes the selected tabs from their group.
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun MultiSelectTabsTrayBanner(
    selectedTabCount: Int,
    shouldShowInactiveButton: Boolean,
    shouldShowAddToTabGroupButton: Boolean,
    shouldShowSaveToCollectionButton: Boolean = true,
    shouldShowRemoveFromTabGroupButton: Boolean = false,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    menuPlacement: MultiSelectMenuPlacement = MultiSelectMenuPlacement.Dropdown,
    menuItems: List<MenuItem>? = null,
    menuExpanded: Boolean? = null,
    onMenuExpandedChange: (Boolean) -> Unit = {},
    onExitSelectModeClick: () -> Unit,
    onSaveToCollectionsClick: () -> Unit,
    onShareSelectedTabs: () -> Unit,
    onBookmarkSelectedTabsClick: () -> Unit,
    onCloseSelectedTabsClick: () -> Unit,
    onMakeSelectedTabsInactive: () -> Unit,
    onAddToTabGroup: () -> Unit,
    onRemoveFromTabGroup: () -> Unit = {},
) {
    val buttonsEnabled by remember(selectedTabCount) {
        derivedStateOf {
            selectedTabCount > 0
        }
    }
    val buttonTint = if (buttonsEnabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.secondary
    }
    var internalMenuExpanded by remember { mutableStateOf(false) }
    val showMenu = menuExpanded ?: internalMenuExpanded
    val setMenuExpanded: (Boolean) -> Unit = { expanded ->
        if (menuExpanded == null) {
            internalMenuExpanded = expanded
        }
        onMenuExpandedChange(expanded)
    }
    val resolvedMenuItems = menuItems ?: generateMultiSelectBannerMenuItems(
        shouldShowInactiveButton = shouldShowInactiveButton,
        shouldShowAddToTabGroupButton = shouldShowAddToTabGroupButton,
        shouldShowSaveToCollectionButton = shouldShowSaveToCollectionButton,
        shouldShowRemoveFromTabGroupButton = shouldShowRemoveFromTabGroupButton,
        onShareSelectedTabs = onShareSelectedTabs,
        onSaveToCollectionsClick = onSaveToCollectionsClick,
        onMakeSelectedTabsInactive = onMakeSelectedTabsInactive,
        onAddToTabGroup = onAddToTabGroup,
        onRemoveFromTabGroup = onRemoveFromTabGroup,
    )

    Box {
        TopAppBar(
        title = {
            Text(
                text = if (selectedTabCount == 0) {
                    stringResource(R.string.tab_tray_multi_select_title_empty)
                } else {
                    stringResource(R.string.tab_tray_multi_select_title, selectedTabCount)
                },
                modifier = Modifier.testTag(TabsTrayTestTag.SELECTION_COUNTER),
                style = FirefoxTheme.typography.headline6,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onExitSelectModeClick,
                contentDescription = stringResource(id = R.string.tab_tray_close_multiselect_content_description),
            ) {
                Icon(
                    painter = painterResource(id = iconsR.drawable.mozac_ic_back_24),
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(
                onClick = onBookmarkSelectedTabsClick,
                contentDescription = stringResource(
                    id = R.string.tab_manager_multiselect_menu_item_bookmark_content_description,
                ),
                enabled = buttonsEnabled,
            ) {
                Icon(
                    painter = painterResource(id = iconsR.drawable.mozac_ic_bookmark_24),
                    contentDescription = null,
                )
            }

            IconButton(
                onClick = onCloseSelectedTabsClick,
                contentDescription = stringResource(
                    id = R.string.tab_manager_multiselect_menu_item_close_content_description,
                ),
                enabled = buttonsEnabled,
            ) {
                Icon(
                    painter = painterResource(id = iconsR.drawable.mozac_ic_delete_24),
                    contentDescription = null,
                )
            }

            IconButton(
                onClick = { setMenuExpanded(true) },
                contentDescription = stringResource(id = R.string.tab_tray_multiselect_menu_content_description),
                modifier = Modifier.testTag(TabsTrayTestTag.THREE_DOT_BUTTON),
                enabled = buttonsEnabled,
            ) {
                if (menuPlacement == MultiSelectMenuPlacement.Dropdown) {
                    DropdownMenu(
                        menuItems = resolvedMenuItems,
                        expanded = showMenu,
                        onDismissRequest = { setMenuExpanded(false) },
                    )
                }

                Icon(
                    painter = painterResource(iconsR.drawable.mozac_ic_ellipsis_vertical_24),
                    contentDescription = null,
                )
            }
            },
            expandedHeight = RowHeight,
            windowInsets = windowInsets,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                actionIconContentColor = buttonTint,
            ),
        )
    }
}

@Composable
internal fun MultiSelectTabsTrayMenu(
    visible: Boolean,
    menuItems: List<MenuItem>,
    onDismissRequest: () -> Unit,
) {
    val visibilityState = remember { MutableTransitionState(false) }
    visibilityState.targetState = visible

    if (visibilityState.currentState || visibilityState.targetState) {
        val density = LocalDensity.current
        val endPadding = FirefoxTheme.layout.space.static100
        val bottomPadding = FirefoxTheme.layout.space.static50
        val navigationBarBottomInset = WindowInsets.navigationBars.getBottom(density)
        val positionProvider = remember(density, endPadding, bottomPadding, navigationBarBottomInset) {
            AboveToolbarMenuPositionProvider(
                endPaddingPx = with(density) { endPadding.roundToPx() },
                bottomPaddingPx = with(density) { bottomPadding.roundToPx() },
                toolbarHeightPx = with(density) { RowHeight.roundToPx() },
                navigationBarBottomInsetPx = navigationBarBottomInset,
            )
        }

        Popup(
            popupPositionProvider = positionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = false,
            ),
        ) {
            AnimatedVisibility(
                visibleState = visibilityState,
                enter = scaleIn(
                    animationSpec = tween(
                        durationMillis = MENU_ENTER_DURATION_MILLIS,
                        easing = LinearOutSlowInEasing,
                    ),
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 1f),
                    initialScale = 0.8f,
                ) + fadeIn(
                    animationSpec = tween(durationMillis = MENU_ENTER_FADE_DURATION_MILLIS),
                ),
                exit = scaleOut(
                    animationSpec = tween(
                        durationMillis = 1,
                        delayMillis = MENU_EXIT_DURATION_MILLIS - 1,
                    ),
                    transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 1f),
                    targetScale = 0.8f,
                ) + fadeOut(
                    animationSpec = tween(durationMillis = MENU_EXIT_DURATION_MILLIS),
                ),
            ) {
                BottomToolbarMenu(
                    menuItems = menuItems,
                    onDismissRequest = onDismissRequest,
                )
            }
        }
    }
}

internal data class AboveToolbarMenuPositionProvider(
    val endPaddingPx: Int,
    val bottomPaddingPx: Int,
    val toolbarHeightPx: Int,
    val navigationBarBottomInsetPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = if (layoutDirection == LayoutDirection.Ltr) {
            windowSize.width - popupContentSize.width - endPaddingPx
        } else {
            endPaddingPx
        }.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
        y = (
            windowSize.height -
                navigationBarBottomInsetPx -
                toolbarHeightPx -
                bottomPaddingPx -
                popupContentSize.height
            ).coerceAtLeast(0),
    )
}

@Composable
private fun BottomToolbarMenu(
    menuItems: List<MenuItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(MultiSelectMenuMinWidth, MultiSelectMenuMaxWidth),
        color = MaterialTheme.colorScheme.surfaceBright,
        shape = MaterialTheme.shapes.large,
        shadowElevation = FirefoxTheme.layout.space.static100,
    ) {
        Column(modifier = Modifier.padding(vertical = FirefoxTheme.layout.space.static100)) {
            menuItems.forEach { item ->
                when (item) {
                    is MenuItem.IconItem -> {
                        val itemColor = if (item.level == MenuItem.FixedItem.Level.Critical) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item.text.value,
                                    style = FirefoxTheme.typography.body1,
                                )
                            },
                            onClick = {
                                onDismissRequest()
                                item.onClick()
                            },
                            modifier = Modifier
                                .height(RowHeight)
                                .testTag(item.testTag),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(item.drawableRes),
                                    contentDescription = null,
                                )
                            },
                            enabled = item.enabled,
                            colors = MenuDefaults.itemColors(
                                textColor = itemColor,
                                leadingIconColor = itemColor,
                            ),
                            contentPadding = PaddingValues(horizontal = FirefoxTheme.layout.space.static150),
                        )
                    }

                    is MenuItem.Divider -> HorizontalDivider()
                    else -> Unit
                }
            }
        }
    }
}

internal fun generateMultiSelectBannerMenuItems(
    shouldShowInactiveButton: Boolean,
    shouldShowAddToTabGroupButton: Boolean,
    shouldShowSaveToCollectionButton: Boolean = true,
    shouldShowRemoveFromTabGroupButton: Boolean,
    onShareSelectedTabs: () -> Unit,
    onSaveToCollectionsClick: () -> Unit,
    onMakeSelectedTabsInactive: () -> Unit,
    onAddToTabGroup: () -> Unit,
    onRemoveFromTabGroup: () -> Unit,
): List<MenuItem> {
    val menuItems = mutableListOf(
        MenuItem.IconItem(
            text = Text.Resource(R.string.tab_manager_multiselect_menu_item_share),
            drawableRes = iconsR.drawable.mozac_ic_share_android_24,
            testTag = TabsTrayTestTag.SHARE_BUTTON,
            onClick = onShareSelectedTabs,
        ),
    )
    if (shouldShowSaveToCollectionButton) {
        menuItems.add(
            MenuItem.IconItem(
                text = Text.Resource(R.string.tab_manager_multiselect_menu_item_add_to_collection),
                drawableRes = iconsR.drawable.mozac_ic_collection_24,
                testTag = TabsTrayTestTag.COLLECTIONS_BUTTON,
                onClick = onSaveToCollectionsClick,
            ),
        )
    }
    if (shouldShowInactiveButton) {
        menuItems.add(
            MenuItem.IconItem(
                text = Text.Resource(R.string.inactive_tabs_menu_item_2),
                drawableRes = iconsR.drawable.mozac_ic_cross_circle_24,
                onClick = onMakeSelectedTabsInactive,
            ),
        )
    }
    if (shouldShowAddToTabGroupButton) {
        menuItems.add(
            MenuItem.IconItem(
                text = Text.Resource(R.string.tab_manager_multiselect_menu_item_add_to_tab_group),
                drawableRes = iconsR.drawable.mozac_ic_tab_group_24,
                onClick = onAddToTabGroup,
            ),
        )
    }
    if (shouldShowRemoveFromTabGroupButton) {
        menuItems.add(
            MenuItem.IconItem(
                text = Text.Resource(R.string.tab_manager_multiselect_menu_item_remove_from_tab_group),
                drawableRes = iconsR.drawable.mozac_ic_tab_ungroup_24,
                testTag = TabsTrayTestTag.REMOVE_FROM_TAB_GROUP,
                onClick = onRemoveFromTabGroup,
            ),
        )
    }
    return menuItems
}

private val previewData = listOf(
    Pair(
        "Private page selected",
        TabsTrayState(
            selectedPage = Page.PrivateTabs,
        ),
    ),
    Pair(
        "Normal page selected - 0 tabs",
        TabsTrayState(
            selectedPage = Page.NormalTabs,
            normalTabsState = TabsTrayState.NormalTabsState(tabCount = 0),
        ),
    ),
    Pair(
        "Normal page selected - infinity tabs",
        TabsTrayState(
            selectedPage = Page.NormalTabs,
            normalTabsState = TabsTrayState.NormalTabsState(tabCount = 100),
        ),
    ),
    Pair(
        "Tab groups page selected",
        TabsTrayState(
            selectedPage = Page.TabGroups,
            config = TabsTrayState.TabsTrayConfig(tabGroupsEnabled = true),
        ),
    ),
    Pair(
        "Synced page selected",
        TabsTrayState(
            selectedPage = Page.SyncedTabs,
        ),
    ),
    Pair(
        "Normal tab page selected with badge on tab group page",
        TabsTrayState(
            selectedPage = Page.NormalTabs,
            tabGroupState = TabsTrayState.TabGroupState(
                groups = listOf(createTabGroup()),
                hasViewedTabGroupsPage = false,
            ),
            config = TabsTrayState.TabsTrayConfig(tabGroupsEnabled = true),
        ),
    ),
    Pair(
        "Auto close banner is displayed",
        TabsTrayState(
            normalTabsState = TabsTrayState.NormalTabsState(tabCount = 10),
            config = TabsTrayState.TabsTrayConfig(showTabAutoCloseBanner = true),
        ),
    ),
    Pair(
        "Multiselection mode with 2 tabs selected",
        TabsTrayState(
            mode = Mode.Select(
                selectedTabs = setOf(
                    createTab("www.mozilla.com"),
                    createTab("www.mozilla.com"),
                ),
            ),
        ),
    ),
    Pair(
        "Multiselection mode with 0 tabs selected",
        TabsTrayState(
            mode = Mode.Select(),
        ),
    ),
)

private class TabsTrayBannerParameterProvider : ThemedValueProvider<TabsTrayState>(
    baseValues = previewData.map { it.second }.asSequence(),
    getDisplayName = { index, _ -> previewData[index].first },
)

@Preview
@Composable
private fun TabsTrayBannerPreview(
    @PreviewParameter(TabsTrayBannerParameterProvider::class) previewState: ThemedValue<TabsTrayState>,
) {
    val tabsTrayStore = remember { TabsTrayStore(initialState = previewState.value) }
    val state by tabsTrayStore.stateFlow.collectAsState()

    FirefoxTheme(theme = previewState.theme) {
        Box(modifier = Modifier.size(400.dp)) {
            TabsTrayBanner(
                state = state,
                onAction = tabsTrayStore::dispatch,
                onTabPageIndicatorClicked = { page ->
                    tabsTrayStore.dispatch(TabsTrayAction.PageSelected(page))
                },
                onSaveToCollectionClick = {},
                onShareSelectedTabsClick = {},
                onBookmarkSelectedTabsClick = {},
                onDeleteSelectedTabsClick = {},
                onForceSelectedTabsAsInactiveClick = {},
                onTabAutoCloseBannerViewOptionsClick = {},
                onTabsTrayPbmLockedClick = {},
                onTabsTrayPbmLockedDismiss = {},
                onTabAutoCloseBannerDismiss = {},
                onTabAutoCloseBannerShown = {},
            )
        }
    }
}
