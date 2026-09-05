/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import mozilla.components.browser.state.state.BrowserState
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.tabstray.controller.TabManagerController
import org.mozilla.fenix.tabstray.data.createTab
import org.mozilla.fenix.tabstray.data.createTabGroup
import org.mozilla.fenix.tabstray.navigation.TabManagerNavDestination
import org.mozilla.fenix.tabstray.redux.action.TabsTrayAction
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState
import org.mozilla.fenix.tabstray.redux.state.TabsTrayState.Mode
import org.mozilla.fenix.tabstray.redux.store.TabsTrayStore
import org.mozilla.fenix.tabstray.ui.TabManagementFragment
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter

@RunWith(RobolectricTestRunner::class)
class TabManagementBackTest {
    private val context: Context = mockk(relaxed = true)
    private val fragment = spyk(TabManagementFragment())
    private val controller: TabManagerController = mockk(relaxed = true)
    private val store: TabsTrayStore = mockk(relaxed = true)
    private val tab = createTab("https://example.com")
    private val group = createTabGroup(tabs = listOf(tab))
    private val destination = TabManagerNavDestination.ExpandedTabGroup(group)
    private val groupState = TabsTrayState(
        backStack = listOf(TabManagerNavDestination.Root, destination),
    )

    @Before
    fun setup() {
        every { fragment.context } returns context
        every { context.components.core.store.state } returns BrowserState(selectedTabId = tab.id)
        fragment.tabsTrayStore = store
        ReflectionHelpers.setField(fragment, "tabManagerController", controller)
    }

    @Test
    fun `Back in focused group navigates even when tray selected tab is stale`() {
        back(groupState.copy(selectedTabId = "stale"))
        verify(exactly = 1) { controller.handleNavigationRequested() }
        verify(exactly = 0) { store.dispatch(any()) }
    }

    @Test
    fun `Back in another group collapses without opening a browser tab`() {
        every { context.components.core.store.state } returns BrowserState(selectedTabId = "outside")
        back(groupState)
        verify(exactly = 1) { store.dispatch(TabsTrayAction.NavigateBackInvoked) }
        verify(exactly = 0) { controller.handleNavigationRequested() }
    }

    @Test
    fun `Back uses latest group membership rather than destination snapshot`() {
        back(groupState.copy(tabGroupState = TabsTrayState.TabGroupState(groups = listOf(group.copy(tabs = emptyList())))))
        verify(exactly = 1) { store.dispatch(TabsTrayAction.NavigateBackInvoked) }
        verify(exactly = 0) { controller.handleNavigationRequested() }
    }

    @Test
    fun `Back clears grouped selection without collapsing or navigating`() {
        back(groupState.copy(mode = Mode.Select(setOf(tab), tabGroupId = group.id)))
        assertOnlySelectionCleared()
    }

    @Test
    fun `Back clears root tab selection without navigating`() {
        back(TabsTrayState(mode = Mode.Select(setOf(tab))))
        assertOnlySelectionCleared()
    }

    @Test
    fun `Back clears root group selection without navigating`() {
        back(TabsTrayState(mode = Mode.Select(selectedTabGroups = setOf(group))))
        assertOnlySelectionCleared()
    }

    @Test
    fun `Back in empty group selection mode navigates to focused tab`() {
        back(groupState.copy(mode = Mode.Select(tabGroupId = group.id)))
        verify(exactly = 1) { controller.handleNavigationRequested() }
        verify(exactly = 0) { store.dispatch(any()) }
    }

    @Test
    fun `Back at root navigates to focused browser tab`() {
        back(TabsTrayState())
        verify(exactly = 1) { controller.handleNavigationRequested() }
        verify(exactly = 0) { store.dispatch(any()) }
    }

    private fun assertOnlySelectionCleared() {
        verify(exactly = 1) { store.dispatch(TabsTrayAction.ExitSelectMode) }
        verify(exactly = 0) { store.dispatch(TabsTrayAction.NavigateBackInvoked) }
        verify(exactly = 0) { controller.handleNavigationRequested() }
    }

    private fun back(state: TabsTrayState) {
        ReflectionHelpers.callInstanceMethod<Unit>(
            fragment,
            "handleBack",
            ClassParameter.from(TabsTrayState::class.java, state),
        )
    }
}
