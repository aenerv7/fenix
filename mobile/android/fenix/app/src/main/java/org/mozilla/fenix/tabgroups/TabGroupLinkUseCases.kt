/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabgroups

import kotlinx.coroutines.flow.first
import mozilla.components.support.utils.DateTimeProvider
import mozilla.components.support.utils.DefaultDateTimeProvider
import org.mozilla.fenix.tabgroups.storage.data.TabGroup
import org.mozilla.fenix.tabgroups.storage.repository.TabGroupRepository
import org.mozilla.fenix.tabstray.data.TabGroupTheme

class TabGroupLinkUseCases(
    private val tabGroupRepository: TabGroupRepository,
    private val dateTimeProvider: DateTimeProvider = DefaultDateTimeProvider(),
) {
    suspend fun isTabInGroup(tabId: String): Boolean =
        tabGroupRepository.tabGroupDataFlow.first().tabGroupAssignments.containsKey(tabId)

    suspend fun addTabToParentGroup(parentTabId: String, tabId: String) {
        val groupId = tabGroupRepository.tabGroupDataFlow
            .first()
            .tabGroupAssignments[parentTabId]
            ?: return

        tabGroupRepository.addTabGroupAssignment(
            tabId = tabId,
            tabGroupId = groupId,
        )
    }

    suspend fun addTabToGroupOrCreateGroup(
        parentTabId: String,
        tabId: String,
        newGroupTitle: (Int) -> String,
    ) {
        val tabGroupData = tabGroupRepository.tabGroupDataFlow.first()
        val groupId = tabGroupData.tabGroupAssignments[parentTabId]

        if (groupId != null) {
            tabGroupRepository.addTabGroupAssignment(
                tabId = tabId,
                tabGroupId = groupId,
            )
            return
        }

        val lastTheme = tabGroupData.tabGroups
            .maxByOrNull { it.lastModified }
            ?.theme
            ?.toTabGroupTheme()
        val newTheme = lastTheme?.next() ?: TabGroupTheme.default
        val tabGroup = TabGroup(
            title = newGroupTitle(tabGroupData.tabGroups.size + 1),
            theme = newTheme.name,
            lastModified = dateTimeProvider.currentTimeMillis(),
        )

        tabGroupRepository.createTabGroupWithTabs(
            tabGroup = tabGroup,
            tabIds = listOf(parentTabId, tabId),
        )
    }

    private fun String.toTabGroupTheme(): TabGroupTheme =
        runCatching { TabGroupTheme.valueOf(this) }.getOrDefault(TabGroupTheme.default)
}
