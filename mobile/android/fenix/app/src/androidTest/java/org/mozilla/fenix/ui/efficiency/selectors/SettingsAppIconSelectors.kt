/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui.efficiency.selectors

import org.mozilla.fenix.R
import org.mozilla.fenix.helpers.DataGenerationHelper.getStringResource
import org.mozilla.fenix.ui.efficiency.helpers.Selector
import org.mozilla.fenix.ui.efficiency.helpers.SelectorContainer
import org.mozilla.fenix.ui.efficiency.helpers.SelectorGroup
import org.mozilla.fenix.ui.efficiency.helpers.SelectorStrategy

object SettingsAppIconSelectors : SelectorContainer {
    enum class Group : SelectorGroup {
        APP_ICON_ITEMS,
        CHANGE_ICON_DIALOG,
    }

    val TOOLBAR_TITLE =
        navigationToolbarTitle(
            title = getStringResource(R.string.preferences_app_icon),
            description = "App icon toolbar title",
        )

    val SOLID_COLORS_SECTION =
        Selector(
            strategy = SelectorStrategy.UIAUTOMATOR_WITH_TEXT,
            value = getStringResource(R.string.alternative_app_icon_group_solid_colors),
            description = "Solid colors app icon section header",
            groups = setOf(Group.APP_ICON_ITEMS),
        )
    val DEFAULT_ICON =
        Selector(
            strategy = SelectorStrategy.UIAUTOMATOR_WITH_TEXT,
            value = getStringResource(R.string.alternative_app_icon_option_default),
            description = "Default app icon option",
            groups = setOf(Group.APP_ICON_ITEMS),
        )
    val DARK_ICON =
        Selector(
            strategy = SelectorStrategy.COMPOSE_BY_TEXT,
            value = getStringResource(R.string.alternative_app_icon_option_dark),
            description = "Dark app icon option",
            groups = setOf(Group.APP_ICON_ITEMS),
        )

    val CHANGE_ICON_DIALOG_TITLE =
        Selector(
            strategy = SelectorStrategy.COMPOSE_BY_TEXT,
            value = getStringResource(R.string.restart_warning_dialog_title),
            description = "Change app icon dialog title",
            groups = setOf(Group.CHANGE_ICON_DIALOG),
        )
    val CHANGE_ICON_DIALOG_BODY =
        Selector(
            strategy = SelectorStrategy.COMPOSE_BY_TEXT,
            value = getStringResource(R.string.restart_warning_dialog_body_2),
            description = "Change app icon dialog body",
            groups = setOf(Group.CHANGE_ICON_DIALOG),
        )
    val CHANGE_ICON_DIALOG_CANCEL_BUTTON =
        Selector(
            strategy = SelectorStrategy.COMPOSE_BY_TEXT,
            value = getStringResource(R.string.restart_warning_dialog_button_negative),
            description = "Change app icon dialog Cancel button",
            groups = setOf(Group.CHANGE_ICON_DIALOG),
        )
    val CHANGE_ICON_DIALOG_CHANGE_BUTTON =
        Selector(
            strategy = SelectorStrategy.COMPOSE_BY_TEXT,
            value = getStringResource(R.string.restart_warning_dialog_button_positive_2),
            description = "Change app icon dialog Change button",
            groups = setOf(Group.CHANGE_ICON_DIALOG),
        )

    override val scrollTraversalOrder: Map<SelectorGroup, List<Selector>> =
        mapOf(
            Group.APP_ICON_ITEMS to
                listOf(
                    SOLID_COLORS_SECTION,
                    DEFAULT_ICON,
                    DARK_ICON,
                )
        )
}
