/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.iconpicker

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.mozilla.fenix.R

/**
 * Enum that represents app launcher icons available for the user to set as an alternative launcher icon. It is based
 * off <activity-alias> entries declared in the AndroidManifest, with addition of title and subtitle for representing
 * the icon name in the UI.
 *
 * The aliasSuffix is the ending part of the <activity-alias>. They are used to construct full component names for
 * switching the launcher icon at runtime.
 *
 * Example:
 * - "AppRabbitDark" → android:name="${applicationId}.AppRabbitDark"
 *
 * @property aliasSuffix A suffix portion of the `android:name` attribute in the manifest.
 * @property iconForegroundId The foreground drawable resource used in icon previews.
 * @property iconBackground The background layer used in icon previews, which can be a solid color or drawable.
 * @property titleId A string resource describing the icon in the icon picker screen.
 * @property subtitleId An optional string resource used as a secondary label.
 */
enum class AppIcon(
    val aliasSuffix: String,
    @param:DrawableRes val iconForegroundId: Int = R.drawable.ic_launcher_rabbit_foreground_scaled,
    val iconBackground: IconBackground = IconBackground.Color(colorResId = R.color.ic_launcher_background),
    @param:StringRes val titleId: Int,
    @param:StringRes val subtitleId: Int? = null,
) {
    AppDefault(
        aliasSuffix = "App",
        titleId = R.string.alternative_app_icon_option_default,
    ),
    AppRabbitDark(
        aliasSuffix = "AppRabbitDark",
        iconBackground = IconBackground.Color(colorResId = R.color.ic_launcher_background_dark),
        titleId = R.string.alternative_app_icon_option_dark,
    );

    /** [AppIcon] helper object */
    companion object {
        /**
         * Returns the [AppIcon] associated with the given string.
         *
         * @param aliasSuffix The suffix from android:name in the manifest (e.g. "AppSolidLight"). Full definition
         *   example from the manifest: android:name="${applicationId}.AppSolidLight"
         */
        fun fromString(aliasSuffix: String): AppIcon = entries.find { it.aliasSuffix == aliasSuffix } ?: AppDefault
    }
}

/**
 * Represents the background layer of an app icon mipmap assigned to a `<activity-alias>`.
 *
 * It allows passing both `@DrawableRes` and `@ColorRes`, as mipmap files support both `drawable` and `color` parameters
 * for `<background android:drawable>`
 */
sealed class IconBackground {
    /**
     * A solid color background.
     *
     * @property colorResId The color resource ID to use.
     */
    data class Color(@param:ColorRes val colorResId: Int) : IconBackground()

    /**
     * A drawable background.
     *
     * @property drawableResId The drawable resource ID to use.
     */
    data class Drawable(@param:DrawableRes val drawableResId: Int) : IconBackground()
}
