/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.fenix.tabstray.ui.tabpage

import android.content.res.Configuration
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import mozilla.components.compose.base.annotation.FlexibleWindowPreview
import mozilla.components.compose.base.modifier.thenConditional
import mozilla.components.compose.base.theme.Theme
import mozilla.components.compose.base.theme.ThemedValue
import mozilla.components.compose.base.theme.ThemedValueProvider
import mozilla.components.compose.draggable.GridInteractionState
import mozilla.components.compose.draggable.InteractableDragItemContainer
import mozilla.components.compose.draggable.InteractionState
import mozilla.components.compose.draggable.ItemInteractionHandler
import mozilla.components.compose.draggable.NoOpItemInteractionHandler
import mozilla.components.compose.draggable.createGridInteractionState
import mozilla.components.compose.draggable.interactableGrid
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.rememberSwipeToDismissBoxState
import org.mozilla.fenix.tabgroups.TabGroupCard
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.tabstray.browser.compose.ReorderableDragItemContainer
import org.mozilla.fenix.tabstray.browser.compose.legacy.GridReorderState
import org.mozilla.fenix.tabstray.browser.compose.legacy.ReorderableDragItemContainer
import org.mozilla.fenix.tabstray.browser.compose.legacy.createGridReorderState
import org.mozilla.fenix.tabstray.browser.compose.legacy.detectGridPressAndDragGestures
import org.mozilla.fenix.tabstray.data.TabGroupTheme
import org.mozilla.fenix.tabstray.data.TabsTrayItem
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.ui.tabitems.TabGridTabItem
import org.mozilla.fenix.tabstray.ui.tabitems.TabGroupOnboardingGridItem
import org.mozilla.fenix.tabstray.ui.tabitems.TabsTrayItemClickHandler
import org.mozilla.fenix.tabstray.ui.tabitems.TabsTrayItemSelectionState
import org.mozilla.fenix.tabstray.ui.tabitems.gridItemAspectRatio
import org.mozilla.fenix.tabstray.ui.tabitems.tabGridColumnCount
import org.mozilla.fenix.tabstray.ui.tabitems.tabGroupEntranceAnimation
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.trackingprotection.TrackersBlockedCard

// Key for the span item at the bottom of the tray, used to make the item not reorderable.
private const val SPAN_ITEM_KEY = "span"

// Key for the header item at the top of the tray, used to make the item not reorderable.
private const val HEADER_ITEM_KEY = "header"

// Key for the tab group onboarding item, used to make the item not reorderable.
private const val TAB_GROUP_ONBOARDING_ITEM_KEY = "tab_group_onboarding"

private const val TAB_GRID_PORTRAIT_WIDTH_THRESHOLD_1 = 320
private const val TAB_GRID_PORTRAIT_WIDTH_THRESHOLD_2 = 480
private const val TAB_GRID_PORTRAIT_WIDTH_THRESHOLD_3 = 800

private const val TAB_GRID_LANDSCAPE_WIDTH_THRESHOLD_1 = 600
private const val TAB_GRID_LANDSCAPE_WIDTH_THRESHOLD_2 = 917
private const val TAB_GRID_LANDSCAPE_WIDTH_THRESHOLD_3 = 1280

private const val NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_1 = 2
private const val NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_2 = 3
private const val NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_3 = 4

private const val NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_1 = 3
private const val NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_2 = 4
private const val NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_3 = 5

private val ignoredItems = setOf(HEADER_ITEM_KEY, SPAN_ITEM_KEY, TAB_GROUP_ONBOARDING_ITEM_KEY)

/**
 * Top-level UI for displaying the tabs tray grid.
 *
 * @param tabs The list of [TabsTrayItem] to display.
 * @param dragAndDropEnabled Whether drag and drop is enabled for tab groups.
 * @param displayTabGroupOnboarding Whether onboarding for tab groups should be shown.
 * @param liveReorderEnabled Whether tab reorders should happen 'live' during a drag.
 * @param selectedItemIndex The index of the currently selected tab. This will be scrolled to on first-render.
 * @param selectionMode [TabsTrayState.Mode] indicating whether the Tabs Tray is in single selection or multi-selection
 *   and contains the set of selected tabs.
 * @param focusEnabled Whether the focus indication state is enabled.
 * @param itemInteractionHandler Handles tab interactions such as moves and drag and drop.
 * @param modifier [Modifier] to be applied to the layout.
 * @param reorderingEnabled Whether tabs can be reordered by dragging.
 * @param trackersBlockedCount The number of trackers blocked to display in the footer card.
 * @param dragProcessingState The lifecycle state of tab-group drag handling
 * @param onTabClose Invoked when the user clicks to close a tab.
 * @param onItemClick Invoked when the user clicks on a tab.
 * @param onItemLongClick Invoked when the user long clicks a tab.
 * @param onEditTabGroupClick Invoked when the user clicks to edit a tab group.
 * @param onCloseTabGroupClick Invoked when the user clicks to close a tab group.
 * @param onShareTabGroupClick Invoked when the user clicks to share a tab group.
 * @param onDeleteTabGroupClick Invoked when the user clicks on delete tab group.
 * @param onUngroupTabGroupClick Invoked when the user clicks to ungroup a tab group.
 * @param onTabGroupOnboardingDismiss Invoked when the user dismisses the tab group onboarding card.
 * @param onTabGroupOnboardingShown Invoked when the tab group onboarding card is shown to the user.
 * @param header Optional layout to display before [tabs].
 * @param contentPadding [PaddingValues] applied around the tabs when displayed in a grid.
 * @param onPrivacyReportTapped Invoked when the trackers blocked pill is tapped.
 * @param enteringGroupId The id of a group entering composition for the first time, if any. Can be null.
 * @param onGroupEntranceAnimationPlayed Called when a new group's entrance animation is played.
 */
@Suppress("LongParameterList")
@Composable
fun TabLayout(
    tabs: List<TabsTrayItem>,
    dragAndDropEnabled: Boolean,
    displayTabGroupOnboarding: Boolean,
    liveReorderEnabled: Boolean,
    selectedItemIndex: Int,
    selectionMode: TabsTrayState.Mode,
    focusEnabled: Boolean,
    itemInteractionHandler: ItemInteractionHandler,
    modifier: Modifier = Modifier,
    reorderingEnabled: Boolean = true,
    trackersBlockedCount: Int? = null,
    dragProcessingState: TabsTrayState.DragProcessingState = TabsTrayState.DragProcessingState.UNINITIALIZED,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onItemClick: (TabsTrayItem) -> Unit,
    onItemLongClick: (TabsTrayItem) -> Unit,
    onEditTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onCloseTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onShareTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onDeleteTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onUngroupTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onTabGroupOnboardingDismiss: () -> Unit,
    onTabGroupOnboardingShown: () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = defaultTabLayoutContentPadding(),
    onPrivacyReportTapped: (() -> Unit)? = null,
    enteringGroupId: String? = null,
    onGroupEntranceAnimationPlayed: () -> Unit = {},
) {
    TabGrid(
        tabs = tabs,
        selectedItemIndex = selectedItemIndex,
        selectionMode = selectionMode,
        modifier = modifier,
        trackersBlockedCount = trackersBlockedCount,
        onTabClose = onTabClose,
        onItemClick = onItemClick,
        onItemLongClick = onItemLongClick,
        itemInteractionHandler = itemInteractionHandler,
        onEditTabGroupClick = onEditTabGroupClick,
        onCloseTabGroupClick = onCloseTabGroupClick,
        onShareTabGroupClick = onShareTabGroupClick,
        onDeleteTabGroupClick = onDeleteTabGroupClick,
        onUngroupTabGroupClick = onUngroupTabGroupClick,
        onTabGroupOnboardingDismiss = onTabGroupOnboardingDismiss,
        onTabGroupOnboardingShown = onTabGroupOnboardingShown,
        header = header,
        contentPadding = contentPadding,
        focusEnabled = focusEnabled,
        dragAndDropEnabled = dragAndDropEnabled,
        reorderingEnabled = reorderingEnabled,
        onPrivacyReportTapped = onPrivacyReportTapped,
        displayTabGroupOnboarding = displayTabGroupOnboarding,
        liveReorderEnabled = liveReorderEnabled,
        enteringGroupId = enteringGroupId,
        onGroupEntranceAnimationPlayed = onGroupEntranceAnimationPlayed,
        dragProcessingState = dragProcessingState,
    )
}

@Composable
@Suppress("LongParameterList")
private fun TabGrid(
    tabs: List<TabsTrayItem>,
    dragAndDropEnabled: Boolean,
    displayTabGroupOnboarding: Boolean,
    liveReorderEnabled: Boolean,
    selectedItemIndex: Int,
    selectionMode: TabsTrayState.Mode,
    focusEnabled: Boolean,
    itemInteractionHandler: ItemInteractionHandler,
    enteringGroupId: String?,
    modifier: Modifier = Modifier,
    reorderingEnabled: Boolean = true,
    trackersBlockedCount: Int? = null,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onItemClick: (TabsTrayItem) -> Unit,
    onItemLongClick: (TabsTrayItem) -> Unit,
    onEditTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onCloseTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onShareTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onDeleteTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onUngroupTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onTabGroupOnboardingDismiss: () -> Unit = {},
    onTabGroupOnboardingShown: () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = defaultTabLayoutContentPadding(),
    onPrivacyReportTapped: (() -> Unit)? = null,
    onGroupEntranceAnimationPlayed: () -> Unit,
    dragProcessingState: TabsTrayState.DragProcessingState = TabsTrayState.DragProcessingState.UNINITIALIZED,
) {
    if (dragAndDropEnabled) {
        InteractableTabGrid(
            tabs = tabs,
            displayTabGroupOnboarding = displayTabGroupOnboarding,
            selectedItemIndex = selectedItemIndex,
            selectionMode = selectionMode,
            modifier = modifier,
            trackersBlockedCount = trackersBlockedCount,
            onTabClose = onTabClose,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            itemInteractionHandler = itemInteractionHandler,
            onEditTabGroupClick = onEditTabGroupClick,
            onCloseTabGroupClick = onCloseTabGroupClick,
            onShareTabGroupClick = onShareTabGroupClick,
            onDeleteTabGroupClick = onDeleteTabGroupClick,
            onUngroupTabGroupClick = onUngroupTabGroupClick,
            onTabGroupOnboardingDismiss = onTabGroupOnboardingDismiss,
            onTabGroupOnboardingShown = onTabGroupOnboardingShown,
            header = header,
            contentPadding = contentPadding,
            focusEnabled = focusEnabled,
            onPrivacyReportTapped = onPrivacyReportTapped,
            liveReorderEnabled = liveReorderEnabled,
            enteringGroupId = enteringGroupId,
            onGroupEntranceAnimationPlayed = onGroupEntranceAnimationPlayed,
            dragProcessingState = dragProcessingState,
        )
    } else {
        ReorderableTabGrid(
            tabs = tabs,
            displayTabGroupOnboarding = displayTabGroupOnboarding,
            selectedItemIndex = selectedItemIndex,
            selectionMode = selectionMode,
            modifier = modifier,
            onTabClose = onTabClose,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            itemInteractionHandler = itemInteractionHandler,
            onEditTabGroupClick = onEditTabGroupClick,
            onCloseTabGroupClick = onCloseTabGroupClick,
            onShareTabGroupClick = onShareTabGroupClick,
            onDeleteTabGroupClick = onDeleteTabGroupClick,
            onUngroupTabGroupClick = onUngroupTabGroupClick,
            onTabGroupOnboardingDismiss = onTabGroupOnboardingDismiss,
            header = header,
            contentPadding = contentPadding,
            trackersBlockedCount = trackersBlockedCount,
            focusEnabled = focusEnabled,
            reorderingEnabled = reorderingEnabled,
            onPrivacyReportTapped = onPrivacyReportTapped,
        )
    }
}

@Composable
private fun TabLayoutScrollHelper(
    state: ScrollableState,
    selectedTabIndex: Int,
    selectedTabGroupId: String?,
    bottomPadding: Dp,
    isHeaderPresent: Boolean,
    showOnboarding: Boolean = false,
) {
    val density = LocalDensity.current
    val bottomPaddingPx = with(density) { bottomPadding.roundToPx() }
    var hasScrolledToInitialPosition by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabGroupId) {
        if (selectedTabIndex < 0) return@LaunchedEffect
        if (hasScrolledToInitialPosition && selectedTabGroupId == null) return@LaunchedEffect

        val headerOffset = if (isHeaderPresent) 1 else 0
        val onboardingOffset = if (showOnboarding) 1 else 0
        val targetIndex = selectedTabIndex + headerOffset + onboardingOffset

        val scrollToItem: suspend (Int, Int) -> Unit =
            when (state) {
                is LazyListState -> state::scrollToItem
                is LazyGridState -> state::scrollToItem
                else -> return@LaunchedEffect
            }

        snapshotFlow { calculateScrollDimensions(state) }
            .filterNotNull()
            .first { (viewportHeight, itemHeight) ->
                viewportHeight > 0 && itemHeight > 0
            }
            .let { (viewportHeight, itemHeight) ->
                val offset = -(viewportHeight - itemHeight - bottomPaddingPx)
                withFrameNanos {}
                scrollToItem(targetIndex, offset)
                hasScrolledToInitialPosition = true
            }
    }
}

private fun calculateScrollDimensions(state: ScrollableState): Pair<Int, Int>? {
    val (viewportHeight, items) =
        when (state) {
            is LazyListState ->
                state.layoutInfo.viewportSize.height to
                    state.layoutInfo.visibleItemsInfo.map {
                        it.key to it.size
                    }

            is LazyGridState ->
                state.layoutInfo.viewportSize.height to
                    state.layoutInfo.visibleItemsInfo.map {
                        it.key to it.size.height
                    }

            else -> return null
        }

    if (viewportHeight <= 0) return null

    val itemHeight = items.firstOrNull { it.first != HEADER_ITEM_KEY }?.second ?: 0
    return viewportHeight to itemHeight
}

// Tab Grid that supports reordering only.
@Suppress("LongParameterList", "LongMethod")
@Composable
private fun ReorderableTabGrid(
    tabs: List<TabsTrayItem>,
    displayTabGroupOnboarding: Boolean,
    selectedItemIndex: Int,
    selectionMode: TabsTrayState.Mode,
    focusEnabled: Boolean,
    modifier: Modifier = Modifier,
    reorderingEnabled: Boolean = true,
    contentPadding: PaddingValues,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onItemClick: (TabsTrayItem) -> Unit,
    onItemLongClick: (TabsTrayItem) -> Unit,
    itemInteractionHandler: ItemInteractionHandler,
    onEditTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onCloseTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onShareTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onDeleteTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onUngroupTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onTabGroupOnboardingDismiss: () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    trackersBlockedCount: Int? = null,
    onPrivacyReportTapped: (() -> Unit)? = null,
) {
    val gridState = rememberLazyGridState()
    val tabGridBottomPadding = dimensionResource(id = R.dimen.tab_tray_grid_bottom_padding)
    val spacing = FirefoxTheme.layout.space.static200
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    TabLayoutScrollHelper(
        state = gridState,
        selectedTabIndex = selectedItemIndex,
        selectedTabGroupId = (tabs.getOrNull(selectedItemIndex) as? TabsTrayItem.TabGroup)?.id,
        showOnboarding = displayTabGroupOnboarding,
        bottomPadding = contentPadding.calculateBottomPadding() + spacing + tabGridBottomPadding + navigationBarPadding,
        isHeaderPresent = header != null,
    )
    var isInMultiSelectMode by remember { mutableStateOf(selectionMode is TabsTrayState.Mode.Select) }
    val reorderState =
        createGridReorderState(
            gridState = gridState,
            onMove = { initialTab, newTab ->
                itemInteractionHandler.onMove(
                    (initialTab.key as String),
                    (newTab.key as String),
                    initialTab.index < newTab.index,
                )
            },
            onLongPress = { itemInfo ->
                tabs
                    .firstOrNull { tabItem -> tabItem.id == itemInfo.key }
                    ?.let { tab ->
                        onItemLongClick(tab)
                    }
            },
            ignoredItems = ignoredItems.toList(),
            itemInteractionHandler = itemInteractionHandler,
        )
    val tabKeys = tabs.mapTo(hashSetOf()) { it.id }
    LaunchedEffect(tabKeys) {
        reorderState.resetForItemChange()
    }
    // Prevent a race between multi-select and drag by updating the select mode only if the dragging key is null
    LaunchedEffect(selectionMode, reorderState.draggingItemKey) {
        if (reorderState.draggingItemKey == null) {
            isInMultiSelectMode = selectionMode is TabsTrayState.Mode.Select
            if (selectionMode is TabsTrayState.Mode.Normal) {
                reorderState.resetImmediately()
            }
        }
    }

    BoxWithConstraints {
        val columns = numberOfGridColumns
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = columns),
            modifier =
                modifier
                    .fillMaxSize()
                    .thenConditional(
                        Modifier.detectGridPressAndDragGestures(
                            gridState = gridState,
                            reorderState = reorderState,
                            isInMultiSelectMode = isInMultiSelectMode,
                        )
                    ) {
                        reorderingEnabled
                    }
                    .semantics {
                        tabGridColumnCount = columns
                        testTag = TabsTrayTestTag.TAB_GRID
                    },
            state = gridState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(space = spacing),
            horizontalArrangement = Arrangement.spacedBy(space = horizontalGridPadding),
        ) {
            header?.let {
                item(key = HEADER_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    header()
                }
            }

            tabGridItems(
                tabs = tabs,
                showTabGroupOnboarding = displayTabGroupOnboarding,
                selectedItemIndex = selectedItemIndex,
                columns = columns,
                onTabGroupOnboardingDismiss = onTabGroupOnboardingDismiss,
            ) { _, gridIndex, _, tab ->
                ReorderableTabGridItemContent(
                    tabsTrayItem = tab,
                    gridIndex = gridIndex,
                    thumbnailSizePx = thumbnailSizePx,
                    hasHeader = header != null,
                    isInMultiSelectMode = isInMultiSelectMode,
                    focusEnabled = focusEnabled,
                    isMultiSelected = selectionMode.contains(tab),
                    reorderState = reorderState,
                    onTabClose = onTabClose,
                    onItemClick = onItemClick,
                    onEditTabGroupClick = onEditTabGroupClick,
                    onCloseTabGroupClick = onCloseTabGroupClick,
                    onShareTabGroupClick = onShareTabGroupClick,
                    onDeleteTabGroupClick = onDeleteTabGroupClick,
                    onUngroupTabGroupClick = onUngroupTabGroupClick,
                )
            }

            tabGridFooter(
                trackersBlockedCount = trackersBlockedCount,
                onPrivacyReportTapped = onPrivacyReportTapped,
            )
        }
    }
}

// Tab grid that supports reordering as well as drag and drop.
@Suppress("LongParameterList", "LongMethod", "CognitiveComplexMethod")
@Composable
private fun InteractableTabGrid(
    tabs: List<TabsTrayItem>,
    displayTabGroupOnboarding: Boolean,
    liveReorderEnabled: Boolean,
    selectedItemIndex: Int,
    focusEnabled: Boolean,
    selectionMode: TabsTrayState.Mode,
    itemInteractionHandler: ItemInteractionHandler,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    trackersBlockedCount: Int? = null,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onItemClick: (TabsTrayItem) -> Unit,
    onItemLongClick: (TabsTrayItem) -> Unit,
    onEditTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onCloseTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onShareTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onDeleteTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onUngroupTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onTabGroupOnboardingDismiss: () -> Unit = {},
    onTabGroupOnboardingShown: () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    onPrivacyReportTapped: (() -> Unit)? = null,
    enteringGroupId: String? = null,
    onGroupEntranceAnimationPlayed: () -> Unit,
    dragProcessingState: TabsTrayState.DragProcessingState,
) {
    val gridState = rememberLazyGridState()
    val tabGridBottomPadding = dimensionResource(id = R.dimen.tab_tray_grid_bottom_padding)
    val spacing = FirefoxTheme.layout.space.static200
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    TabLayoutScrollHelper(
        state = gridState,
        selectedTabIndex = selectedItemIndex,
        selectedTabGroupId = (tabs.getOrNull(selectedItemIndex) as? TabsTrayItem.TabGroup)?.id,
        showOnboarding = displayTabGroupOnboarding,
        bottomPadding = contentPadding.calculateBottomPadding() + tabGridBottomPadding + spacing + navigationBarPadding,
        isHeaderPresent = header != null,
    )

    var isInMultiSelectMode by remember { mutableStateOf(selectionMode is TabsTrayState.Mode.Select) }
    val gridInteractionState =
        createGridInteractionState(
            gridState = gridState,
            itemInteractionHandler = itemInteractionHandler,
            onLongPress = { itemInfo ->
                tabs.firstOrNull { tabItem -> tabItem.id == itemInfo.key }?.let(onItemLongClick)
            },
            liveReorderEnabled = liveReorderEnabled,
            ignoredItems = ignoredItems,
        )
    val tabKeys = tabs.mapTo(hashSetOf()) { it.id }
    LaunchedEffect(tabKeys) {
        gridInteractionState.resetForItemChange()
    }
    // Don't show the onboarding card while a drag is active
    val showOnboardingCardInGrid by
        remember(displayTabGroupOnboarding) {
            derivedStateOf { displayTabGroupOnboarding && gridInteractionState.draggedItem.key == null }
        }
    val onboardingCardInView by remember {
        derivedStateOf { gridState.layoutInfo.visibleItemsInfo.any { it.key == TAB_GROUP_ONBOARDING_ITEM_KEY } }
    }
    SideEffect(onboardingCardInView) {
        if (onboardingCardInView) {
            onTabGroupOnboardingShown()
        }
    }

    // Prevent a race between multi-select and drag by updating the select mode only if the dragging key is null
    LaunchedEffect(selectionMode, gridInteractionState.draggedItem.key) {
        if (gridInteractionState.draggedItem.key == null) {
            isInMultiSelectMode = selectionMode is TabsTrayState.Mode.Select
            if (selectionMode is TabsTrayState.Mode.Normal) {
                gridInteractionState.resetImmediately()
            }
        }
    }

    LaunchedEffect(dragProcessingState) {
        if (dragProcessingState == TabsTrayState.DragProcessingState.COMPLETED) {
            gridInteractionState.reset()
        }
    }

    BoxWithConstraints(
        modifier =
            Modifier.interactableGrid(state = gridInteractionState, isInMultiSelectMode = isInMultiSelectMode)
                .drawVerticalReorderIndicator(gridInteractionState = gridInteractionState)
    ) {
        val columns = numberOfGridColumns
        val collectionSemantics =
            TabCollectionSemantics(
                itemCount = tabs.size + if (showOnboardingCardInGrid) columns else 0,
                columns = columns,
            )
        val reorderGeometry = TabReorderGeometry(tabCount = tabs.size, columns = columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = columns),
            modifier =
                modifier.fillMaxSize().semantics {
                    tabGridColumnCount = columns
                    testTag = TabsTrayTestTag.TAB_GRID
                    collectionInfo = collectionSemantics.collectionInfo
                },
            state = gridState,
            userScrollEnabled = gridInteractionState.draggedItem == InteractionState.Grid.None,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(space = spacing),
            horizontalArrangement = Arrangement.spacedBy(space = horizontalGridPadding),
        ) {
            header?.let {
                item(key = HEADER_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    header()
                }
            }

            tabGridItems(
                tabs = tabs,
                showTabGroupOnboarding = showOnboardingCardInGrid,
                selectedItemIndex = selectedItemIndex,
                columns = columns,
                onTabGroupOnboardingDismiss = onTabGroupOnboardingDismiss,
                collectionSemantics = collectionSemantics,
            ) { tabIndex, gridIndex, collectionIndex, tab ->
                val pinnableContainer = LocalPinnableContainer.current
                val isDragged by
                    remember(tab.id) {
                        derivedStateOf {
                            gridInteractionState.draggedItem.key == tab.id
                        }
                    }
                DisposableEffect(isDragged) {
                    val handle = if (isDragged) pinnableContainer?.pin() else null
                    onDispose { handle?.release() }
                }
                val reorderActions =
                    rememberTabReorderActions(
                        item = tab,
                        tabs = tabs,
                        tabIndex = tabIndex,
                        reorderGeometry = reorderGeometry,
                        enabled = !isInMultiSelectMode,
                        itemInteractionHandler = itemInteractionHandler,
                    )
                InteractableTabGridItemContent(
                    tabsTrayItem = tab,
                    gridIndex = gridIndex,
                    thumbnailSizePx = thumbnailSizePx,
                    hasHeader = header != null,
                    isInMultiSelectMode = isInMultiSelectMode,
                    focusEnabled = focusEnabled,
                    isMultiSelected = selectionMode.contains(tab),
                    reorderState = gridInteractionState,
                    onTabClose = onTabClose,
                    onItemClick = onItemClick,
                    onEditTabGroupClick = onEditTabGroupClick,
                    onCloseTabGroupClick = onCloseTabGroupClick,
                    onShareTabGroupClick = onShareTabGroupClick,
                    onDeleteTabGroupClick = onDeleteTabGroupClick,
                    onUngroupTabGroupClick = onUngroupTabGroupClick,
                    enteringGroupId = enteringGroupId,
                    onGroupEntranceAnimationPlayed = onGroupEntranceAnimationPlayed,
                    itemInfo = collectionSemantics.itemInfo(position = collectionIndex),
                    accessibilityActions = reorderActions,
                )
            }

            tabGridFooter(
                trackersBlockedCount = trackersBlockedCount,
                onPrivacyReportTapped = onPrivacyReportTapped,
            )
        }
    }
}

/** Splits grid view [tabs] around the tab group onboarding card when [showTabGroupOnboarding] is true. */
private fun LazyGridScope.tabGridItems(
    tabs: List<TabsTrayItem>,
    showTabGroupOnboarding: Boolean,
    selectedItemIndex: Int,
    columns: Int,
    onTabGroupOnboardingDismiss: () -> Unit,
    collectionSemantics: TabCollectionSemantics? = null,
    tabContent:
        @Composable
        LazyGridItemScope.(
            tabIndex: Int,
            gridIndex: Int,
            collectionIndex: Int,
            tab: TabsTrayItem,
        ) -> Unit,
) {
    // Integer division rounds down so the onboarding card is inserted at the start
    //  of the row containing the selected tab, instead of splitting the row.
    val onboardingInsertIndex = (selectedItemIndex / columns) * columns
    val tabsBeforeOnboarding = if (showTabGroupOnboarding) tabs.subList(0, onboardingInsertIndex) else tabs
    itemsIndexed(
        items = tabsBeforeOnboarding,
        key = { _, tab -> tab.id },
    ) { index, tab ->
        // Before the onboarding card is placed,
        // the tab, index, and collection positions all coincide
        tabContent(index, index, index, tab)
    }

    val modifier =
        collectionSemantics?.let {
            Modifier.semantics {
                collectionItemInfo = it.itemInfo(position = onboardingInsertIndex, columnSpan = columns)
            }
        } ?: Modifier
    if (showTabGroupOnboarding) {
        item(key = TAB_GROUP_ONBOARDING_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
            TabGroupOnboardingGridItem(
                onDismiss = onTabGroupOnboardingDismiss,
                modifier = modifier,
            )
        }

        itemsIndexed(
            items = tabs.subList(onboardingInsertIndex, tabs.size),
            key = { _, tab -> tab.id },
        ) { index, tab ->
            val tabIndex = onboardingInsertIndex + index
            val gridIndex = tabIndex + 1
            val collectionIndex = tabIndex + columns
            tabContent(tabIndex, gridIndex, collectionIndex, tab)
        }
    }
}

/** Footer item for the tab grid: a [TrackersBlockedCard] when trackers are blocked. */
private fun LazyGridScope.tabGridFooter(
    trackersBlockedCount: Int?,
    onPrivacyReportTapped: (() -> Unit)? = null,
) {
    item(key = SPAN_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
        val bottomBarHeight = dimensionResource(id = R.dimen.browser_toolbar_height)
        val tabGridBottomPadding = dimensionResource(id = R.dimen.tab_tray_grid_bottom_padding)
        Column(
            modifier =
                Modifier.thenConditional(
                    Modifier.padding(top = FirefoxTheme.layout.space.static200),
                    { trackersBlockedCount != null },
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (trackersBlockedCount != null) {
                TrackersBlockedCard(
                    trackersBlockedCount = trackersBlockedCount,
                    onPrivacyReportTapped = onPrivacyReportTapped,
                )
                Spacer(modifier = Modifier.height(bottomBarHeight + 16.dp))
            } else {
                Spacer(modifier = Modifier.height(tabGridBottomPadding))
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun LazyGridItemScope.ReorderableTabGridItemContent(
    tabsTrayItem: TabsTrayItem,
    gridIndex: Int,
    thumbnailSizePx: Int,
    hasHeader: Boolean,
    isInMultiSelectMode: Boolean,
    focusEnabled: Boolean,
    isMultiSelected: Boolean,
    reorderState: GridReorderState,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onItemClick: (TabsTrayItem) -> Unit,
    onEditTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onCloseTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onShareTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onDeleteTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onUngroupTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(id = tabsTrayItem.id)
    val shouldClickListen = reorderState.draggingItemKey != tabsTrayItem.id
    val swipingActive by
        remember(swipeToDismissBoxState) {
            derivedStateOf {
                swipeToDismissBoxState.dismissDirection != SwipeToDismissBoxValue.Settled
            }
        }

    ReorderableDragItemContainer(
        state = reorderState,
        position = gridIndex + if (hasHeader) 1 else 0,
        key = tabsTrayItem.id,
        swipingActive = swipingActive,
    ) { interactionState ->
        val selectionState =
            TabsTrayItemSelectionState(
                isFocused = tabsTrayItem.isFocused,
                isSelected = isMultiSelected,
                multiSelectEnabled = isInMultiSelectMode,
                focusEnabled = focusEnabled,
            )
        when (tabsTrayItem) {
            is TabsTrayItem.Tab -> {
                TabGridTabItem(
                    tab = tabsTrayItem,
                    swipeToDismissBoxState = swipeToDismissBoxState,
                    swipingEnabled = !isInMultiSelectMode,
                    interactionState = interactionState,
                    onCloseClick = onTabClose,
                    onClick = onItemClick,
                    thumbnailSizePx = thumbnailSizePx,
                    selectionState = selectionState,
                    shouldClickListen = shouldClickListen,
                )
            }

            is TabsTrayItem.TabGroup -> {
                TabGroupCard(
                    group = tabsTrayItem,
                    selectionState = selectionState,
                    clickHandler =
                        TabsTrayItemClickHandler(
                            enabled = shouldClickListen,
                            onClick = onItemClick,
                        ),
                    interactionState = interactionState,
                    onEditTabGroupClick = { onEditTabGroupClick(tabsTrayItem) },
                    onCloseTabGroupClick = { onCloseTabGroupClick(tabsTrayItem) },
                    onShareTabGroupClick = onShareTabGroupClick,
                    onDeleteTabGroupClick = onDeleteTabGroupClick,
                    onUngroupTabGroupClick = { onUngroupTabGroupClick(tabsTrayItem) },
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun LazyGridItemScope.InteractableTabGridItemContent(
    tabsTrayItem: TabsTrayItem,
    gridIndex: Int,
    thumbnailSizePx: Int,
    hasHeader: Boolean,
    isInMultiSelectMode: Boolean,
    focusEnabled: Boolean,
    isMultiSelected: Boolean,
    reorderState: GridInteractionState,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onItemClick: (TabsTrayItem) -> Unit,
    onEditTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onCloseTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onShareTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onDeleteTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    onUngroupTabGroupClick: (TabsTrayItem.TabGroup) -> Unit,
    enteringGroupId: String?,
    onGroupEntranceAnimationPlayed: () -> Unit,
    modifier: Modifier = Modifier,
    itemInfo: CollectionItemInfo? = null,
    accessibilityActions: List<CustomAccessibilityAction> = emptyList(),
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(id = tabsTrayItem.id)
    val shouldClickListen = reorderState.draggedItem.key != tabsTrayItem.id
    val swipingActive by
        remember(swipeToDismissBoxState) {
            derivedStateOf {
                swipeToDismissBoxState.dismissDirection != SwipeToDismissBoxValue.Settled
            }
        }
    InteractableDragItemContainer(
        state = reorderState,
        position = gridIndex + if (hasHeader) 1 else 0,
        key = tabsTrayItem.id,
        swipingActive = swipingActive,
        enteringItemKey = enteringGroupId,
        enteringItemDecoration = { interactionState ->
            Modifier.tabGroupEntranceAnimation(
                interactionState = interactionState,
                key = tabsTrayItem.id,
                onGroupEntranceAnimationPlayed = onGroupEntranceAnimationPlayed,
            )
        },
    ) { interactionState ->
        val selectionState =
            TabsTrayItemSelectionState(
                isFocused = tabsTrayItem.isFocused,
                isSelected = isMultiSelected,
                multiSelectEnabled = isInMultiSelectMode,
                focusEnabled = focusEnabled,
            )
        when (tabsTrayItem) {
            is TabsTrayItem.Tab -> {
                TabGridTabItem(
                    tab = tabsTrayItem,
                    swipeToDismissBoxState = swipeToDismissBoxState,
                    swipingEnabled = !isInMultiSelectMode,
                    interactionState = interactionState,
                    onCloseClick = onTabClose,
                    onClick = onItemClick,
                    thumbnailSizePx = thumbnailSizePx,
                    selectionState = selectionState,
                    shouldClickListen = shouldClickListen,
                    modifier = modifier,
                    itemInfo = itemInfo,
                    accessibilityActions = accessibilityActions,
                )
            }

            is TabsTrayItem.TabGroup -> {
                TabGroupCard(
                    group = tabsTrayItem,
                    selectionState = selectionState,
                    clickHandler =
                        TabsTrayItemClickHandler(
                            enabled = shouldClickListen,
                            onClick = onItemClick,
                        ),
                    interactionState = interactionState,
                    onEditTabGroupClick = { onEditTabGroupClick(tabsTrayItem) },
                    onCloseTabGroupClick = { onCloseTabGroupClick(tabsTrayItem) },
                    onShareTabGroupClick = onShareTabGroupClick,
                    onDeleteTabGroupClick = onDeleteTabGroupClick,
                    onUngroupTabGroupClick = { onUngroupTabGroupClick(tabsTrayItem) },
                    modifier = modifier,
                    itemInfo = itemInfo,
                    accessibilityActions = accessibilityActions,
                )
            }
        }
    }
}

internal val horizontalGridPadding: Dp
    @ReadOnlyComposable @Composable get() = FirefoxTheme.layout.space.static200

private val BoxWithConstraintsScope.thumbnailSizePx: Int
    @Composable
    @ReadOnlyComposable
    get() {
        val columns = numberOfGridColumns
        val density = LocalDensity.current
        val totalSpacing = horizontalGridPadding * (columns - 1) + FirefoxTheme.layout.space.static50 * columns * 2
        val thumbnailWidth = constraints.maxWidth - with(density) { totalSpacing.roundToPx() }
        val thumbnailHeight = (thumbnailWidth / gridItemAspectRatio).toInt()
        return max(thumbnailWidth, thumbnailHeight)
    }

/** Returns the number of grid columns we can fit on the screen in the tabs tray. */
private val BoxWithConstraintsScope.numberOfGridColumns: Int
    @Composable
    @ReadOnlyComposable
    get() {
        val configuration = LocalConfiguration.current
        return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numberOfGridColumnsLandscape(screenWidthDp = maxWidth.value)
        } else {
            numberOfGridColumnsPortrait(screenWidthDp = maxWidth.value)
        }
    }

/**
 * Returns the number of columns to be rendered for a grid in portrait mode given the screen width.
 *
 * @param screenWidthDp Float representation of screenWidth dp
 */
@VisibleForTesting
internal fun numberOfGridColumnsPortrait(screenWidthDp: Float): Int =
    when {
        screenWidthDp >= TAB_GRID_PORTRAIT_WIDTH_THRESHOLD_3 -> NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_3
        screenWidthDp >= TAB_GRID_PORTRAIT_WIDTH_THRESHOLD_2 -> NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_2
        screenWidthDp >= TAB_GRID_PORTRAIT_WIDTH_THRESHOLD_1 -> NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_1
        else -> NUM_COLUMNS_TAB_GRID_PORTRAIT_THRESHOLD_1
    }

/**
 * Returns the number of columns to be rendered for a grid in landscape mode given the screen width.
 *
 * @param screenWidthDp Float representation of screenWidth dp
 */
@VisibleForTesting
internal fun numberOfGridColumnsLandscape(screenWidthDp: Float): Int =
    when {
        screenWidthDp >= TAB_GRID_LANDSCAPE_WIDTH_THRESHOLD_3 -> NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_3
        screenWidthDp >= TAB_GRID_LANDSCAPE_WIDTH_THRESHOLD_2 -> NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_2
        screenWidthDp >= TAB_GRID_LANDSCAPE_WIDTH_THRESHOLD_1 -> NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_1
        else -> NUM_COLUMNS_TAB_GRID_LANDSCAPE_THRESHOLD_1
    }

private data class TabLayoutPreviewModel(
    val tabCount: Int = 10,
    val selectedTabIndex: Int = 0,
    val tabGroupIndices: List<Int> = emptyList(),
)

private val tabLayoutPreviewData: List<Pair<String, TabLayoutPreviewModel>> =
    listOf(
        Pair(
            "50 Tabs, 25th selected",
            TabLayoutPreviewModel(
                tabCount = 50,
                selectedTabIndex = 25,
            ),
        ),
        Pair(
            "10 Tabs, first selected",
            TabLayoutPreviewModel(tabCount = 10, selectedTabIndex = 0),
        ),
        Pair(
            "10 Tabs, last selected",
            TabLayoutPreviewModel(tabCount = 10, selectedTabIndex = 9),
        ),
        Pair(
            "10 Groups, 1st selected",
            TabLayoutPreviewModel(tabCount = 10, tabGroupIndices = (0..9).toList(), selectedTabIndex = 0),
        ),
        Pair(
            "10 Tabs, 3 groups, 2nd selected",
            TabLayoutPreviewModel(tabCount = 10, tabGroupIndices = listOf(3, 6, 9), selectedTabIndex = 1),
        ),
        Pair(
            "Single, selected tab",
            TabLayoutPreviewModel(tabCount = 1),
        ),
        Pair(
            "Single, selected group",
            TabLayoutPreviewModel(tabCount = 1, tabGroupIndices = listOf(0)),
        ),
    )

private class TabLayoutPreviewParameterProvider :
    ThemedValueProvider<TabLayoutPreviewModel>(
        baseValues = tabLayoutPreviewData.map { it.second }.asSequence(),
        getDisplayName = { index, _ -> tabLayoutPreviewData[index].first },
    )

@Preview
@Composable
private fun TabGridPreview(
    @PreviewParameter(TabLayoutPreviewParameterProvider::class) previewModel: ThemedValue<TabLayoutPreviewModel>
) {
    val tabs = remember {
        generateFakeTabsList(
                tabCount = previewModel.value.tabCount,
                tabGroupIndices = previewModel.value.tabGroupIndices,
                selectedTabIndex = previewModel.value.selectedTabIndex,
                isPrivate = previewModel.theme == Theme.Private,
            )
            .toMutableStateList()
    }

    FirefoxTheme(theme = previewModel.theme) {
        TabLayout(
            tabs = tabs,
            selectionMode = TabsTrayState.Mode.Normal,
            itemInteractionHandler = NoOpItemInteractionHandler,
            selectedItemIndex = previewModel.value.selectedTabIndex,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            dragAndDropEnabled = false,
            displayTabGroupOnboarding = false,
            liveReorderEnabled = false,
            onTabClose = tabs::remove,
            onItemClick = {},
            onItemLongClick = {},
            onEditTabGroupClick = {},
            onCloseTabGroupClick = {},
            onShareTabGroupClick = {},
            onDeleteTabGroupClick = {},
            onUngroupTabGroupClick = {},
            onTabGroupOnboardingDismiss = {},
            focusEnabled = true,
            onGroupEntranceAnimationPlayed = {},
            dragProcessingState = TabsTrayState.DragProcessingState.UNINITIALIZED,
        )
    }
}

@FlexibleWindowPreview
@Composable
private fun TabGridWindowSizePreview() {
    val previewModel = tabLayoutPreviewData[0].second
    val tabs = remember {
        generateFakeTabsList(
                tabCount = previewModel.tabCount,
                tabGroupIndices = previewModel.tabGroupIndices,
                selectedTabIndex = previewModel.selectedTabIndex,
            )
            .toMutableStateList()
    }

    FirefoxTheme(theme = Theme.Light) {
        TabLayout(
            tabs = tabs,
            selectionMode = TabsTrayState.Mode.Normal,
            itemInteractionHandler = NoOpItemInteractionHandler,
            selectedItemIndex = previewModel.selectedTabIndex,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            dragAndDropEnabled = false,
            displayTabGroupOnboarding = false,
            liveReorderEnabled = false,
            onTabClose = tabs::remove,
            onItemClick = {},
            onItemLongClick = {},
            onEditTabGroupClick = {},
            onCloseTabGroupClick = {},
            onShareTabGroupClick = {},
            onDeleteTabGroupClick = {},
            onUngroupTabGroupClick = {},
            onTabGroupOnboardingDismiss = {},
            focusEnabled = true,
            onGroupEntranceAnimationPlayed = {},
            dragProcessingState = TabsTrayState.DragProcessingState.UNINITIALIZED,
        )
    }
}

private const val SELECTED_TAB_COUNT_PREVIEW = 4

@Preview
@Composable
private fun TabGridMultiSelectPreview(
    @PreviewParameter(TabLayoutPreviewParameterProvider::class) previewModel: ThemedValue<TabLayoutPreviewModel>
) {
    MultiSelectPreview(previewModel = previewModel)
}

@Composable
private fun MultiSelectPreview(previewModel: ThemedValue<TabLayoutPreviewModel>) {
    val tabs =
        generateFakeTabsList(
            tabCount = previewModel.value.tabCount,
            tabGroupIndices = previewModel.value.tabGroupIndices,
            selectedTabIndex = previewModel.value.selectedTabIndex,
            isPrivate = previewModel.theme == Theme.Private,
        )
    val selectedTabs = remember {
        tabs.take(SELECTED_TAB_COUNT_PREVIEW).filterIsInstance<TabsTrayItem.Tab>().toMutableStateList()
    }
    val selectedTabGroups = remember {
        tabs.take(SELECTED_TAB_COUNT_PREVIEW).filterIsInstance<TabsTrayItem.TabGroup>().toMutableStateList()
    }

    FirefoxTheme(theme = previewModel.theme) {
        TabLayout(
            tabs = tabs,
            selectedItemIndex = previewModel.value.selectedTabIndex,
            selectionMode =
                TabsTrayState.Mode.Select(
                    selectedTabs = selectedTabs.toSet(),
                    selectedTabGroups = selectedTabGroups.toSet(),
                ),
            itemInteractionHandler = NoOpItemInteractionHandler,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            dragAndDropEnabled = false,
            displayTabGroupOnboarding = false,
            onTabClose = {},
            onItemClick = { tab ->
                when (tab) {
                    is TabsTrayItem.Tab ->
                        if (selectedTabs.contains(tab)) {
                            selectedTabs.remove(tab)
                        } else {
                            selectedTabs.add(tab)
                        }

                    is TabsTrayItem.TabGroup ->
                        if (selectedTabGroups.contains(tab)) {
                            selectedTabGroups.remove(tab)
                        } else {
                            selectedTabGroups.add(tab)
                        }
                }
            },
            onItemLongClick = {},
            onEditTabGroupClick = {},
            onCloseTabGroupClick = {},
            onShareTabGroupClick = {},
            onDeleteTabGroupClick = {},
            onUngroupTabGroupClick = {},
            onTabGroupOnboardingDismiss = {},
            focusEnabled = true,
            liveReorderEnabled = false,
            onGroupEntranceAnimationPlayed = {},
            dragProcessingState = TabsTrayState.DragProcessingState.UNINITIALIZED,
        )
    }
}

private fun generateFakeTabsList(
    tabCount: Int = 10,
    isPrivate: Boolean = false,
    tabGroupIndices: List<Int> = emptyList(),
    selectedTabIndex: Int = -1,
): List<TabsTrayItem> {
    return List(tabCount) { index ->
        val isFocused = index == selectedTabIndex
        if (index in tabGroupIndices) {
            createTabGroup(
                title = "Group $index",
                theme = TabGroupTheme.Pink,
                tabs =
                    mutableListOf(
                        createTab(
                            id = "groupTab1",
                            url = "www.mozilla.com",
                            private = isPrivate,
                        ),
                        createTab(
                            id = "groupTab2",
                            url = "www.mozilla.com",
                            private = isPrivate,
                        ),
                        createTab(
                            id = "groupTab3",
                            url = "www.mozilla.com",
                            private = isPrivate,
                        ),
                        createTab(
                            id = "groupTab4",
                            url = "www.mozilla.com",
                            private = isPrivate,
                        ),
                    ),
                isFocused = isFocused,
            )
        } else {
            createTab(
                id = "tabId$index-$isPrivate",
                url = "www.mozilla.com",
                private = isPrivate,
                isFocused = isFocused,
            )
        }
    }
}

/**
 * The default horizontal content padding used by TabLayout. In some cases, such as when a tab layout is embedded inside
 * another view, we may wish to override this content padding.
 */
@Composable
@ReadOnlyComposable
private fun defaultTabLayoutContentPadding(): PaddingValues =
    PaddingValues(
        horizontal =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                TAB_LAYOUT_HORIZONTAL_LANDSCAPE_PADDING
            } else {
                FirefoxTheme.layout.space.static200
            },
        vertical = FirefoxTheme.layout.space.static300,
    )

private val TAB_LAYOUT_HORIZONTAL_LANDSCAPE_PADDING = 52.dp

/**
 * Because our TabLayout uses a passed in ContentPadding value that differs in portrait and landscape, and that is not
 * accessible inside the LazyGrid API, which only exposes the main axis padding, this Composable returns the computed
 * offset that is needed to adjust indicators that appear to the left or right of grid items.
 */
@Composable
@ReadOnlyComposable
private fun defaultCrossAxisStartPadding(): Float =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        with(LocalDensity.current) {
            (TAB_LAYOUT_HORIZONTAL_LANDSCAPE_PADDING - FirefoxTheme.layout.space.static200).toPx()
        }
    } else {
        0f
    }

/**
 * Draws a line in the 'gutters' between tab items to indicate to the user between which tabs they are attempting to
 * reorder.
 */
@Composable
private fun Modifier.drawVerticalReorderIndicator(gridInteractionState: GridInteractionState): Modifier {
    val crossAxisStartPadding = defaultCrossAxisStartPadding()
    val indicatorColor = MaterialTheme.colorScheme.tertiary
    val strokeWidth = FirefoxTheme.layout.border.heaviest
    return this.drawBehind({
        val rect = gridInteractionState.highlightedRect ?: return@drawBehind
        // This is a workaround correction at draw time stemming from the fact that
        // LazyGrid does not expose the cross axis padding as part of its API.
        val adjustedCenter = rect.center.x + crossAxisStartPadding
        drawLine(
            color = indicatorColor,
            start = Offset(x = adjustedCenter, y = rect.top),
            end = Offset(x = adjustedCenter, y = rect.bottom),
            strokeWidth = strokeWidth.toPx(),
        )
    })
}
