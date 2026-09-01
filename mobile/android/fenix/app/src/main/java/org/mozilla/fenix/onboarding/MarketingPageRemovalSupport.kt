/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.onboarding

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.fenix.onboarding.view.OnboardingPageUiData
import org.mozilla.fenix.utils.Settings
import kotlin.coroutines.CoroutineContext

/**
 * Handles removing the marketing page from onboarding if certain conditions are met
 *
 * @param prefKey the pref key identifier for the "should show marketing page" pref
 * @param pagesToDisplay the mutable list of onboarding pages we display
 * @param settings settings class that holds shared preferences
 * @param mainContext the coroutine context for UI
 * @param ioContext the coroutine context for IO
 * @param lifecycleOwner the lifecycle owner
 */
class MarketingPageRemovalSupport(
    private val prefKey: String,
    private val pagesToDisplay: MutableList<OnboardingPageUiData>,
    private val settings: Settings,
    private val mainContext: CoroutineContext = Dispatchers.Main,
    private val ioContext: CoroutineContext = Dispatchers.IO,
    private val lifecycleOwner: LifecycleOwner,
) : LifecycleAwareFeature {

    var currentPageIndex: Int = 0

    private var job: Job? = null

    override fun start() {
        job = lifecycleOwner.lifecycleScope.launch(ioContext) {
            settings.preferences.flowScopedBooleanPreference(
                lifecycleOwner,
                mainContext,
                prefKey,
                settings.shouldShowMarketingOnboarding,
            )
                .distinctUntilChanged()
                .collect { shouldShowMarketingOnboarding ->
                    if (!shouldShowMarketingOnboarding) {
                        pagesToDisplay.removeIfPageNotReached(currentPageIndex)
                    }
                }
        }
    }

    override fun stop() {
        job?.cancel()
    }
}

internal fun MutableList<OnboardingPageUiData>.removeIfPageNotReached(index: Int) {
    val marketingIndex = indexOfFirst { it.type == OnboardingPageUiData.Type.MARKETING_DATA }

    if (index < marketingIndex) {
        removeAt(marketingIndex)
    }
}
