/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabgroups

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mozilla.components.support.utils.FakeDateTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabgroups.fakes.FakeTabGroupRepository
import org.mozilla.fenix.tabgroups.storage.data.TabGroup
import org.mozilla.fenix.tabgroups.storage.data.TabGroupData
import org.mozilla.fenix.tabstray.data.TabGroupTheme

@RunWith(AndroidJUnit4::class)
class TabGroupLinkUseCasesTest {

    @Test
    fun `isTabInGroup returns true for a grouped tab`() = runTest {
        val group = createTabGroup()
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(
                tabGroups = listOf(group),
                tabGroupAssignments = mapOf(PARENT_TAB_ID to group.id),
            ),
        )

        assertTrue(TabGroupLinkUseCases(repository).isTabInGroup(PARENT_TAB_ID))
    }

    @Test
    fun `isTabInGroup returns false for an ungrouped tab`() = runTest {
        assertFalse(TabGroupLinkUseCases(FakeTabGroupRepository()).isTabInGroup(PARENT_TAB_ID))
    }

    @Test
    fun `addTabToParentGroup adds the child tab to the parent's group`() = runTest {
        val group = createTabGroup()
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(
                tabGroups = listOf(group),
                tabGroupAssignments = mapOf(PARENT_TAB_ID to group.id),
            ),
        )
        val useCases = TabGroupLinkUseCases(repository)

        useCases.addTabToParentGroup(PARENT_TAB_ID, CHILD_TAB_ID)

        assertEquals(group.id, repository.tabGroupDataFlow.first().tabGroupAssignments[CHILD_TAB_ID])
    }

    @Test
    fun `addTabToParentGroup does nothing when the parent is not grouped`() = runTest {
        val repository = FakeTabGroupRepository()
        val useCases = TabGroupLinkUseCases(repository)

        useCases.addTabToParentGroup(PARENT_TAB_ID, CHILD_TAB_ID)

        assertFalse(repository.tabGroupDataFlow.first().tabGroupAssignments.containsKey(CHILD_TAB_ID))
    }

    @Test
    fun `addTabToGroupOrCreateGroup adds the child tab to the parent's existing group`() = runTest {
        val group = createTabGroup()
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(
                tabGroups = listOf(group),
                tabGroupAssignments = mapOf(PARENT_TAB_ID to group.id),
            ),
        )
        val useCases = TabGroupLinkUseCases(repository)

        useCases.addTabToGroupOrCreateGroup(PARENT_TAB_ID, CHILD_TAB_ID) { "Group $it" }

        val tabGroupData = repository.tabGroupDataFlow.first()
        assertEquals(listOf(group), tabGroupData.tabGroups)
        assertEquals(group.id, tabGroupData.tabGroupAssignments[CHILD_TAB_ID])
    }

    @Test
    fun `addTabToGroupOrCreateGroup creates a localized group for ungrouped tabs`() = runTest {
        val existingGroup = createTabGroup(
            id = "existing-group",
            theme = TabGroupTheme.Green,
            lastModified = 20L,
        )
        val repository = FakeTabGroupRepository(
            initialTabGroupData = TabGroupData(tabGroups = listOf(existingGroup)),
        )
        val useCases = TabGroupLinkUseCases(
            tabGroupRepository = repository,
            dateTimeProvider = FakeDateTimeProvider(currentTime = 42L),
        )

        useCases.addTabToGroupOrCreateGroup(PARENT_TAB_ID, CHILD_TAB_ID) { "Localized group $it" }

        val tabGroupData = repository.tabGroupDataFlow.first()
        val newGroup = tabGroupData.tabGroups.last()
        assertEquals("Localized group 2", newGroup.title)
        assertEquals(TabGroupTheme.Grey.name, newGroup.theme)
        assertEquals(42L, newGroup.lastModified)
        assertEquals(newGroup.id, tabGroupData.tabGroupAssignments[PARENT_TAB_ID])
        assertEquals(newGroup.id, tabGroupData.tabGroupAssignments[CHILD_TAB_ID])
    }

    private fun createTabGroup(
        id: String = "group-id",
        theme: TabGroupTheme = TabGroupTheme.Yellow,
        lastModified: Long = 10L,
    ) = TabGroup(
        id = id,
        title = "Group",
        theme = theme.name,
        lastModified = lastModified,
    )

    private companion object {
        const val PARENT_TAB_ID = "parent-tab"
        const val CHILD_TAB_ID = "child-tab"
    }
}
