/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.search

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.ContentState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.awesomebar.AwesomeBar.Suggestion
import mozilla.components.concept.engine.EngineSession.LoadUrlFlags
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.GleanMetrics.Events
import org.mozilla.fenix.browser.browsingmode.BrowsingMode
import org.mozilla.fenix.browser.browsingmode.BrowsingModeManager
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.UseCases
import org.mozilla.fenix.components.appstate.AppState
import org.mozilla.fenix.components.appstate.search.SearchState
import org.mozilla.fenix.components.usecases.FenixBrowserUseCases
import org.mozilla.fenix.search.SearchFragmentAction.SuggestionClicked
import org.mozilla.fenix.utils.Settings
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchSuggestionTabRoutingTest {
    private val browserUseCases: FenixBrowserUseCases = mockk(relaxed = true)
    private val useCases: UseCases = mockk { every { fenixBrowserUseCases } returns browserUseCases }
    private val browsingModeManager: BrowsingModeManager = mockk()
    private val settings: Settings = mockk()

    @Before
    fun setup() {
        mockkObject(Events)
        every { Events.enteredUrl } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkObject(Events)
    }

    @Test
    fun `existing search source with no initial tab id reuses the source tab`() {
        assertSuggestionTabRouting(sourceTabId = "group-tab", initialTabId = null, expectedNewTab = false)
    }

    @Test
    fun `closed search source with stale initial tab id creates a tab`() {
        assertSuggestionTabRouting(sourceTabId = "closed-tab", initialTabId = "closed-tab", expectedNewTab = true)
    }

    @Test
    fun `missing search source creates a tab instead of overwriting an unrelated tab`() {
        assertSuggestionTabRouting(sourceTabId = null, initialTabId = "stale-tab", expectedNewTab = true)
    }

    @Test
    fun `homepage as new tab reuses the current tab`() {
        assertSuggestionTabRouting(
            sourceTabId = null,
            initialTabId = null,
            expectedNewTab = false,
            homepageAsNewTab = true,
        )
    }

    @Test
    fun `private search source reuses the private tab`() {
        assertSuggestionTabRouting(
            sourceTabId = "group-tab",
            initialTabId = null,
            expectedNewTab = false,
            isPrivate = true,
        )
    }

    private fun assertSuggestionTabRouting(
        sourceTabId: String?,
        initialTabId: String?,
        expectedNewTab: Boolean,
        homepageAsNewTab: Boolean = false,
        isPrivate: Boolean = false,
    ) {
        val currentTab =
            TabSessionState(id = "group-tab", content = ContentState(url = "about:home", private = isPrivate))
        val browserStore = BrowserStore(BrowserState(tabs = listOf(currentTab), selectedTabId = currentTab.id))
        val appStore = AppStore(AppState(searchState = SearchState.EMPTY.copy(sourceTabId = sourceTabId)))
        every { settings.enableHomepageAsNewTab } returns homepageAsNewTab
        every { browsingModeManager.mode } returns if (isPrivate) BrowsingMode.Private else BrowsingMode.Normal
        val middleware =
            FenixSearchMiddleware(
                fragment = mockk(),
                engine = mockk(),
                useCases = useCases,
                nimbusComponents = mockk(),
                settings = settings,
                appStore = appStore,
                browserStore = browserStore,
                toolbarStore = mockk(relaxed = true),
                navController = mockk(relaxed = true),
                browsingModeManager = browsingModeManager,
            )
        val store = SearchFragmentStore(SearchFragmentState.EMPTY.copy(tabId = initialTabId))
        val url = "https://example.com/suggestion"
        val flags = LoadUrlFlags.external()
        val urlSuggestion =
            Suggestion(
                provider = mockk(),
                onSuggestionClicked = {
                    middleware.loadUrlUseCase().invoke(url, flags, null, null)
                },
            )

        middleware(store, {}, SuggestionClicked(urlSuggestion))

        verify(exactly = 1) {
            browserUseCases.loadUrlOrSearch(
                searchTermOrURL = url,
                newTab = expectedNewTab,
                private = isPrivate,
                flags = flags,
            )
        }

        val searchSuggestion =
            Suggestion(
                provider = mockk(),
                onSuggestionClicked = {
                    middleware.searchUseCase(store).invoke("suggested search", null, null)
                },
            )
        middleware(store, {}, SuggestionClicked(searchSuggestion))

        verify(exactly = 1) {
            browserUseCases.loadUrlOrSearch(
                searchTermOrURL = "suggested search",
                newTab = expectedNewTab,
                private = isPrivate,
                forceSearch = true,
            )
        }
    }
}
