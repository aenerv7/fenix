/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabgroups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import mozilla.components.compose.base.annotation.FlexibleWindowLightDarkPreview
import mozilla.components.compose.base.button.IconButton
import mozilla.components.compose.base.snackbar.Snackbar
import org.mozilla.fenix.R
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.tabstray.controller.NoOpTabInteractionHandler
import org.mozilla.fenix.tabstray.controller.TabInteractionHandler
import org.mozilla.fenix.tabstray.data.TabGroupTheme
import org.mozilla.fenix.tabstray.data.TabsTrayItem
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.ui.tabitems.LOREM_IPSUM
import org.mozilla.fenix.tabstray.ui.tabitems.TabGroupMenuButton
import org.mozilla.fenix.tabstray.ui.tabpage.TabLayout
import org.mozilla.fenix.theme.FirefoxTheme
import mozilla.components.ui.icons.R as iconsR

/**
 * Renders an expanded view of a user's tab group.
 * @param group: [TabsTrayItem.TabGroup] item rendered by the card.
 * @param onItemClick Invoked when the user clicks on a [TabsTrayItem] in the group.
 * @param onTabClose Invoked when the user clicks to close a [TabsTrayItem.Tab] in the group.
 * @param onDeleteTabGroupClick Invoked when the user clicks on delete tab group.
 * @param onEditTabGroupClick Invoked when the user clicks to edit the [group].
 * @param onCloseTabGroupClick Invoked when the user clicks to close a tab group.
 * @param tabInteractionHandler Handler for tab interactions.
 * @param selectionMode The current tab selection mode.
 * @param onItemLongClick Invoked when the user long clicks a tab.
 * @param selectionBanner The multi-select toolbar shown in a fixed overlay above the expanded group.
 * @param selectionMenu The multi-select menu shown in a separate overlay above the toolbar.
 * @param snackbarHostState Snackbar state rendered above the multi-select toolbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTabGroup(
    group: TabsTrayItem.TabGroup,
    actions: ExpandedTabGroupActions,
    displayTabsInGrid: Boolean,
    tabInteractionHandler: TabInteractionHandler,
    selectionMode: TabsTrayState.Mode = TabsTrayState.Mode.Normal,
    onItemLongClick: (TabsTrayItem) -> Unit = {},
    selectionBanner: @Composable (Boolean, (Boolean) -> Unit) -> Unit = { _, _ -> },
    selectionMenu: @Composable (Boolean, (Boolean) -> Unit) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState? = null,
) {
    var selectionMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectionMode) {
        if (selectionMode !is TabsTrayState.Mode.Select) {
            selectionMenuExpanded = false
        }
    }

    Column(modifier = Modifier.testTag(TabsTrayTestTag.TAB_GROUP_BOTTOM_SHEET_ROOT)) {
        ViewTabGroupHeader(
            title = group.title,
            groupTheme = group.theme,
            groupTabsSize = group.tabs.size,
            actions = actions,
            modifier = Modifier.padding(
                start = FirefoxTheme.layout.space.dynamic200,
                end = FirefoxTheme.layout.space.dynamic200,
            ),
        )

        TabLayout(
            tabs = group.tabs,
            displayTabsInGrid = displayTabsInGrid,
            dragAndDropEnabled = false,
            reorderingEnabled = true,
            displayTabGroupOnboarding = false,
            liveReorderEnabled = true,
            selectedItemIndex = group.initialScrollIndex,
            selectionMode = selectionMode,
            tabInteractionHandler = tabInteractionHandler,
            modifier = Modifier.padding(
                start = FirefoxTheme.layout.space.dynamic200,
                end = FirefoxTheme.layout.space.dynamic200,
            ),
            onTabClose = actions.onTabClose,
            onItemClick = actions.onItemClick,
            onItemLongClick = onItemLongClick,
            onDeleteTabGroupClick = { }, // Ignore tab group deletes
            onEditTabGroupClick = { }, // Ignore tab group edits
            onCloseTabGroupClick = { }, // Ignore tab group closes
            onShareTabGroupClick = { }, // Ignore tab group shares
            onTabGroupOnboardingDismiss = { }, // Ignore onboarding dismissals - onboarding is not shown in this layout
            contentPadding = PaddingValues(0.dp), // TabLayout should not have its own content padding inside this view
            listHorizontalPadding = 0.dp, // The list layout should not add its own horizontal padding inside this view
            focusEnabled = true, // Drag and drop is not possible in this view, so focus should never be suppressed
        )
    }

    if (selectionMode is TabsTrayState.Mode.Select || snackbarHostState?.currentSnackbarData != null) {
        val snackbarData = snackbarHostState?.currentSnackbarData
        val density = LocalDensity.current
        val navigationBarBottomInset = WindowInsets.navigationBars.getBottom(density)

        Popup(
            onDismissRequest = { selectionMenuExpanded = false },
            popupPositionProvider = FixedBottomPopupPositionProvider(),
            properties = PopupProperties(
                focusable = selectionMenuExpanded,
                dismissOnBackPress = selectionMenuExpanded,
                dismissOnClickOutside = selectionMenuExpanded,
                clippingEnabled = false,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TabsTrayTestTag.TAB_GROUP_FIXED_OVERLAY),
            ) {
                snackbarHostState?.let { hostState ->
                    if (snackbarData != null) {
                        SnackbarHost(hostState = hostState) { snackbarData ->
                            Snackbar(snackbarData = snackbarData)
                        }
                    }
                }

                if (selectionMode is TabsTrayState.Mode.Select) {
                    selectionBanner(selectionMenuExpanded) { expanded ->
                        selectionMenuExpanded = expanded
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { navigationBarBottomInset.toDp() })
                        .background(MaterialTheme.colorScheme.surface),
                )
            }
        }
    }

    selectionMenu(
        selectionMode is TabsTrayState.Mode.Select && selectionMenuExpanded,
    ) { expanded ->
        selectionMenuExpanded = expanded
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTabGroup(
    group: TabsTrayItem.TabGroup,
    onItemClick: (TabsTrayItem) -> Unit,
    onTabClose: (TabsTrayItem.Tab) -> Unit,
    onDeleteTabGroupClick: () -> Unit,
    onEditTabGroupClick: () -> Unit,
    onCloseTabGroupClick: () -> Unit,
    tabInteractionHandler: TabInteractionHandler,
    selectionMode: TabsTrayState.Mode = TabsTrayState.Mode.Normal,
    onItemLongClick: (TabsTrayItem) -> Unit = {},
    selectionBanner: @Composable (Boolean, (Boolean) -> Unit) -> Unit = { _, _ -> },
    selectionMenu: @Composable (Boolean, (Boolean) -> Unit) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState? = null,
) {
    ExpandedTabGroup(
        group = group,
        actions = ExpandedTabGroupActions(
            onItemClick = onItemClick,
            onTabClose = onTabClose,
            onDeleteTabGroupClick = onDeleteTabGroupClick,
            onEditTabGroupClick = onEditTabGroupClick,
            onCloseTabGroupClick = onCloseTabGroupClick,
            onAddNewTabClick = null,
            onShareTabGroupClick = {},
        ),
        displayTabsInGrid = true,
        tabInteractionHandler = tabInteractionHandler,
        selectionMode = selectionMode,
        onItemLongClick = onItemLongClick,
        selectionBanner = selectionBanner,
        selectionMenu = selectionMenu,
        snackbarHostState = snackbarHostState,
    )
}

internal class FixedBottomPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = ((windowSize.width - popupContentSize.width) / 2).coerceAtLeast(0),
        y = (windowSize.height - popupContentSize.height).coerceAtLeast(0),
    )
}

@Composable
private fun ViewTabGroupHeader(
    title: String,
    groupTabsSize: Int,
    groupTheme: TabGroupTheme,
    actions: ExpandedTabGroupActions,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = FirefoxTheme.layout.space.static150,
                bottom = FirefoxTheme.layout.space.static200,
            )
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val headerContentDescription = pluralStringResource(
            id = R.plurals.expanded_tab_group_header_description,
            count = groupTabsSize,
            title,
            groupTabsSize,
            groupTheme.contentLabel,
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    heading()
                    contentDescription = headerContentDescription
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabGroupThemeDot(groupTheme)

            Spacer(modifier = Modifier.width(FirefoxTheme.layout.space.static100))

            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = FirefoxTheme.typography.headline7,
            )
        }

        Spacer(
            modifier = Modifier.width(
                FirefoxTheme.layout.space.static200 +
                    FirefoxTheme.layout.space.static25,
            ),
        )

        val onAddNewTabClick = actions.onAddNewTabClick
        if (onAddNewTabClick != null) {
            AddTabToGroupButton(
                onClick = onAddNewTabClick,
            )

            Spacer(modifier = Modifier.width(FirefoxTheme.layout.space.static100))
        }

        TabGroupMenuButton(
            includeCloseOption = true,
            includeUngroupOption = true,
            onDeleteTabGroupClick = actions.onDeleteTabGroupClick,
            onEditTabGroupClick = actions.onEditTabGroupClick,
            onCloseTabGroupClick = actions.onCloseTabGroupClick,
            onShareTabGroupClick = actions.onShareTabGroupClick,
            onUngroupTabGroupClick = {},
        )
    }
}

@Composable
private fun AddTabToGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        contentDescription = stringResource(id = R.string.add_tab),
        modifier = modifier.testTag(TabsTrayTestTag.BOTTOM_SHEET_ADD_TAB_BUTTON),
    ) {
        Icon(
            painter = painterResource(id = iconsR.drawable.mozac_ic_plus_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@FlexibleWindowLightDarkPreview
@Composable
private fun ExpandedTabGroupPreview(
    @PreviewParameter(ExpandedTabGroupPreviewProvider::class)
    previewState: ExpandedTabGroupPreviewState,
) {
    FirefoxTheme {
        Surface {
            ExpandedTabGroup(
                group = previewState.group,
                actions = ExpandedTabGroupActions(
                    onItemClick = {},
                    onTabClose = {},
                    onDeleteTabGroupClick = {},
                    onEditTabGroupClick = {},
                    onCloseTabGroupClick = {},
                    onAddNewTabClick = {},
                    onShareTabGroupClick = {},
                ),
                displayTabsInGrid = previewState.displayTabsInGrid,
                tabInteractionHandler = NoOpTabInteractionHandler,
            )
        }
    }
}

private fun generateFakeTabsList(
    tabCount: Int = 10,
): MutableList<TabsTrayItem.Tab> = MutableList(tabCount) { index ->
    createTab(
        id = "tab$index",
        title = "Tab $index",
        url = "www.mozilla.com",
        private = false,
    )
}

private data class ExpandedTabGroupPreviewState(
    val group: TabsTrayItem.TabGroup,
    val selectedTabId: String? = null,
    val displayTabsInGrid: Boolean = true,
)

private class ExpandedTabGroupPreviewProvider :
    PreviewParameterProvider<ExpandedTabGroupPreviewState> {
    val data = listOf(
        Pair(
            "1 Tab",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = "Tab Group",
                    tabs = generateFakeTabsList(tabCount = 1),
                ),
            ),
        ),
        Pair(
            "2 Tabs",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = "Tab Group",
                    tabs = generateFakeTabsList(tabCount = 2),
                ),
            ),
        ),
        Pair(
            "3 Tabs",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = "Tab Group",
                    tabs = generateFakeTabsList(tabCount = 3),
                ),
            ),
        ),
        Pair(
            "4 Tabs",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = "Tab Group",
                    tabs = generateFakeTabsList(),
                ),
            ),
        ),
        Pair(
            "Selected tab",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = "Tab Group",
                    tabs = generateFakeTabsList(),
                ),
                selectedTabId = "tabid0",
            ),
        ),
        Pair(
            "Large title",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = LOREM_IPSUM,
                    tabs = generateFakeTabsList(),
                ),
                selectedTabId = "tabid0",
            ),
        ),
        Pair(
            "List view",
            ExpandedTabGroupPreviewState(
                group = createTabGroup(
                    title = "Tab Group",
                    tabs = generateFakeTabsList(),
                ),
                displayTabsInGrid = false,
            ),
        ),
    )
    override val values: Sequence<ExpandedTabGroupPreviewState>
        get() = data.map { it.second }.asSequence()

    override fun getDisplayName(index: Int): String {
        return data[index].first
    }
}

/**
 * User interactions handled by the [ExpandedTabGroup] view.
 *
 * @property onItemClick Invoked when the user clicks on a [TabsTrayItem] in the group.
 * @property onTabClose Invoked when the user clicks to close a [TabsTrayItem.Tab] in the group.
 * @property onDeleteTabGroupClick Invoked when the user clicks on delete tab group.
 * @property onEditTabGroupClick Invoked when the user clicks to edit the group.
 * @property onCloseTabGroupClick Invoked when the user clicks to close a tab group.
 * @property onAddNewTabClick Invoked when the user clicks to add a new tab to the group. When null,
 * the add-tab button is hidden.
 * @property onShareTabGroupClick Invoked when the user clicks to share the group.
 */
data class ExpandedTabGroupActions(
    val onItemClick: (TabsTrayItem) -> Unit,
    val onTabClose: (TabsTrayItem.Tab) -> Unit,
    val onDeleteTabGroupClick: () -> Unit,
    val onEditTabGroupClick: () -> Unit,
    val onCloseTabGroupClick: () -> Unit,
    val onAddNewTabClick: (() -> Unit)?,
    val onShareTabGroupClick: () -> Unit,
)
