/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabgroups.middleware

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.createTab
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabgroups.fakes.FakeTabGroupRepository
import org.mozilla.fenix.tabgroups.storage.data.TabGroupData
import org.mozilla.fenix.tabgroups.storage.redux.middleware.TabGroupMiddleware

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TabGroupMiddlewareTest {

    @Test
    fun `WHEN all normal tabs are deleted by the user THEN close all tab groups`() = runTest {
        var closedAllTabGroups = false
        val middleware = TabGroupMiddleware(
            tabGroupRepository = FakeTabGroupRepository(
                closeAllTabGroups = {
                    closedAllTabGroups = true
                },
            ),
            scope = this,
        )

        middleware.processAction(TabListAction.RemoveAllNormalTabsAction)

        advanceUntilIdle()

        assertTrue(closedAllTabGroups)
    }

    @Test
    fun `WHEN all tabs are deleted by the user THEN close all tab groups`() = runTest {
        var closedAllTabGroups = false
        val middleware = TabGroupMiddleware(
            tabGroupRepository = FakeTabGroupRepository(
                closeAllTabGroups = {
                    closedAllTabGroups = true
                },
            ),
            scope = this,
        )

        middleware.processAction(TabListAction.RemoveAllNormalTabsAction)

        advanceUntilIdle()

        assertTrue(closedAllTabGroups)
    }

    @Test
    fun `WHEN the user closes a grouped tab THEN remove the corresponding tab group assignment`() = runTest {
        val expectedTabId = "1"
        var closedTabId = ""
        val middleware = TabGroupMiddleware(
            tabGroupRepository = FakeTabGroupRepository(
                deleteTabGroupAssignmentById = {
                    closedTabId = it
                },
            ),
            scope = this,
        )

        middleware.processAction(TabListAction.RemoveTabAction(tabId = expectedTabId))

        advanceUntilIdle()

        assertEquals(expectedTabId, closedTabId)
    }

    @Test
    fun `WHEN the user closes multiple grouped tabs THEN remove the corresponding tab group assignments`() = runTest {
        val expectedTabIds = List(size = 10) { "$it" }
        var closedTabIds = emptyList<String>()
        val middleware = TabGroupMiddleware(
            tabGroupRepository = FakeTabGroupRepository(
                deleteTabGroupAssignmentsById = {
                    closedTabIds = it
                },
            ),
            scope = this,
        )

        middleware.processAction(TabListAction.RemoveTabsAction(tabIds = expectedTabIds))

        advanceUntilIdle()

        assertEquals(expectedTabIds, closedTabIds)
    }

    @Test
    fun `WHEN a normal child tab is added THEN inherit the parent's tab group`() = runTest {
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(
                tabGroupAssignments = mapOf(PARENT_TAB_ID to GROUP_ID),
            ),
        )
        val middleware = TabGroupMiddleware(
            tabGroupRepository = repository,
            scope = this,
        )

        middleware.processAction(
            TabListAction.AddTabAction(
                createTab(
                    url = "https://example.com",
                    id = CHILD_TAB_ID,
                    parentId = PARENT_TAB_ID,
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(GROUP_ID, repository.tabGroupDataFlow.first().tabGroupAssignments[CHILD_TAB_ID])
    }

    @Test
    fun `WHEN a private child tab is added THEN do not inherit the parent's tab group`() = runTest {
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(
                tabGroupAssignments = mapOf(PARENT_TAB_ID to GROUP_ID),
            ),
        )
        val middleware = TabGroupMiddleware(
            tabGroupRepository = repository,
            scope = this,
        )

        middleware.processAction(
            TabListAction.AddTabAction(
                createTab(
                    url = "https://example.com",
                    id = CHILD_TAB_ID,
                    parentId = PARENT_TAB_ID,
                    private = true,
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(null, repository.tabGroupDataFlow.first().tabGroupAssignments[CHILD_TAB_ID])
    }

    @Test
    fun `WHEN tab grouping is disabled THEN do not inherit the parent's tab group`() = runTest {
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(
                tabGroupAssignments = mapOf(PARENT_TAB_ID to GROUP_ID),
            ),
        )
        val middleware = TabGroupMiddleware(
            tabGroupRepository = repository,
            isTabGroupingEnabled = { false },
            scope = this,
        )

        middleware.processAction(
            TabListAction.AddTabAction(
                createTab(
                    url = "https://example.com",
                    id = CHILD_TAB_ID,
                    parentId = PARENT_TAB_ID,
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(null, repository.tabGroupDataFlow.first().tabGroupAssignments[CHILD_TAB_ID])
    }

    private companion object {
        const val PARENT_TAB_ID = "parent-tab"
        const val CHILD_TAB_ID = "child-tab"
        const val GROUP_ID = "group-id"
    }
}
