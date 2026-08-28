/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.ui.banner

import mozilla.components.compose.base.menu.MenuItem
import mozilla.components.compose.base.text.Text
import org.junit.Test
import org.mozilla.fenix.R
import org.mozilla.fenix.tabstray.TabsTrayTestTag
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.navigation.TabManagerNavDestination
import org.mozilla.fenix.tabstray.navigation.TabManagerNavDestination.ExpandedTabGroup
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState.Mode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import mozilla.components.ui.icons.R as iconsR

class TabsTrayBannerTest {
    @Test
    fun `multiselect banner is hidden behind an expanded tab group`() {
        val state = TabsTrayState(
            mode = Mode.Select(),
            backStack = listOf(
                TabManagerNavDestination.Root,
                ExpandedTabGroup(group = createTabGroup()),
            ),
        )

        assertFalse(state.shouldShowMultiSelectBanner())
    }

    @Test
    fun `multiselect banner is shown at the root`() {
        val state = TabsTrayState(mode = Mode.Select())

        assertTrue(state.shouldShowMultiSelectBanner())
    }

    @Test
    fun `remove from tab group is the last multiselect menu item`() {
        var clicked = false

        val menuItems = generateMultiSelectBannerMenuItems(
            shouldShowInactiveButton = true,
            shouldShowAddToTabGroupButton = true,
            shouldShowRemoveFromTabGroupButton = true,
            onShareSelectedTabs = {},
            onSaveToCollectionsClick = {},
            onMakeSelectedTabsInactive = {},
            onAddToTabGroup = {},
            onRemoveFromTabGroup = { clicked = true },
        )

        val removeItem = assertIs<MenuItem.IconItem>(menuItems.last())
        assertEquals(Text.Resource(R.string.tab_manager_multiselect_menu_item_remove_from_tab_group), removeItem.text)
        assertEquals(iconsR.drawable.mozac_ic_tab_ungroup_24, removeItem.drawableRes)
        assertEquals(TabsTrayTestTag.REMOVE_FROM_TAB_GROUP, removeItem.testTag)

        removeItem.onClick()
        assertTrue(clicked)
    }

    @Test
    fun `remove from tab group is hidden outside a tab group`() {
        val menuItems = generateMultiSelectBannerMenuItems(
            shouldShowInactiveButton = true,
            shouldShowAddToTabGroupButton = true,
            shouldShowRemoveFromTabGroupButton = false,
            onShareSelectedTabs = {},
            onSaveToCollectionsClick = {},
            onMakeSelectedTabsInactive = {},
            onAddToTabGroup = {},
            onRemoveFromTabGroup = {},
        )

        assertTrue(menuItems.none { it is MenuItem.IconItem && it.testTag == TabsTrayTestTag.REMOVE_FROM_TAB_GROUP })
    }
}
