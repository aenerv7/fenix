/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.browser.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.compose.base.utils.LocalUnderTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabstray.browser.compose.interactable.InteractableDragItemContainer
import org.mozilla.fenix.tabstray.browser.compose.interactable.InteractionState
import org.mozilla.fenix.tabstray.browser.compose.interactable.ListInteractionState
import org.mozilla.fenix.tabstray.browser.compose.interactable.createListInteractionState
import org.mozilla.fenix.tabstray.browser.compose.interactable.detectListPressAndDrag
import org.mozilla.fenix.tabstray.controller.NoOpTabInteractionHandler
import org.mozilla.fenix.tabstray.ui.tabitems.tabItemListInteractionAnimation
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme

// Number of tab items rendered by the harness list.
private const val ITEM_COUNT = 20

// The harness list viewport height, in dp.
private const val VIEWPORT_HEIGHT_DP = 400

// Every harness tab item is this tall, in dp.
private const val ITEM_HEIGHT_DP = 80

private const val ITEM_HEIGHT_PX = ITEM_HEIGHT_DP.toFloat()

// How far the dragged item is moved away from its layout position, in px.
private const val DRAG_DISTANCE = 50f

// A frame's worth of scroll, small enough that a dragged item leaves the viewport gradually.
private const val SCROLL_STEP_PX = 40f

// How many frames the list is scrolled while the drag is active.
private const val SCROLL_STEPS = 12

// The dragged item's index in the harness list.
private const val DRAGGED_INDEX = 10

/**
 * Verifies how a dragged tab item renders in the list while the layout moves underneath it.
 *
 * A dragged item that leaves the viewport is kept composed by its item pin and keeps drawing at the layout offset it
 * had when it left. The drag translation is what holds it at the pointer, so it must keep subtracting that offset, the
 * way the grid does with its cached layout coordinates.
 */
@RunWith(AndroidJUnit4::class)
class InteractableListDragTest {
    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var listState: LazyListState
    private lateinit var interactionState: ListInteractionState

    @Test
    fun `GIVEN an item is being dragged WHEN it is dragged THEN it follows the pointer`() {
        setHarness()
        val initialBounds = itemBounds(index = 0)

        drag(index = 0)

        assertEquals(DRAG_DISTANCE, itemBounds(index = 0).center.y - initialBounds.center.y, 1f)
    }

    @Test
    fun `GIVEN an item is being dragged WHEN the list scrolls it out of view THEN it keeps drawing at the drag position`() {
        setHarness()
        // Scroll the list forward so the dragged item has room to leave the viewport at the bottom.
        scrollBy(delta = 8 * ITEM_HEIGHT_PX)
        drag(index = DRAGGED_INDEX)

        // Scroll backwards, one frame at a time, until the dragged item's layout offset passes the bottom edge
        // of the viewport. The item is then drawn at that last layout offset plus the translation, detached from
        // the layout. A translation that drops the offset draws the item about one viewport below the pointer,
        // where the user no longer sees the dragged card.
        var lastOffset = 0f
        repeat(SCROLL_STEPS) {
            scrollBy(delta = -SCROLL_STEP_PX)
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull { info -> info.index == DRAGGED_INDEX }
                ?.let { info -> lastOffset = info.offset.toFloat() }
        }

        val draggedItem = interactionState.draggedItem as InteractionState.List.Active
        val dragPosition = draggedItem.initialOffset + draggedItem.cumulatedOffset
        val drawnPosition = lastOffset + interactionState.computeItemOffset(DRAGGED_INDEX)

        assertEquals(dragPosition, drawnPosition, 1f)
    }

    private fun setHarness() {
        composeTestRule.setContent {
            Harness(
                onState = { state, interaction ->
                    listState = state
                    interactionState = interaction
                }
            )
        }
        composeTestRule.waitForIdle()
    }

    @Composable
    private fun Harness(onState: (LazyListState, ListInteractionState) -> Unit) {
        CompositionLocalProvider(LocalUnderTest provides true) {
            FirefoxTheme(theme = Theme.Light) {
                Surface {
                    val state = rememberLazyListState()
                    val interaction =
                        createListInteractionState(
                            listState = state,
                            ignoredItems = emptySet(),
                            liveReorderEnabled = false,
                            tabInteractionHandler = NoOpTabInteractionHandler,
                        )
                    onState(state, interaction)

                    Box(
                        modifier =
                            Modifier.size(width = 200.dp, height = VIEWPORT_HEIGHT_DP.dp)
                                .detectListPressAndDrag(
                                    listState = state,
                                    interactionState = interaction,
                                    shouldLongPressToDrag = true,
                                )
                    ) {
                        LazyColumn(state = state) {
                            items(count = ITEM_COUNT, key = { tag(it) }) { index ->
                                // The dragged item pins itself, as the tabs tray list does, so it is not
                                // disposed when it is scrolled out of the viewport.
                                val pinnableContainer = LocalPinnableContainer.current
                                val isDragged = interaction.draggedItem.key == tag(index)
                                DisposableEffect(isDragged) {
                                    val handle = if (isDragged) pinnableContainer?.pin() else null
                                    onDispose { handle?.release() }
                                }

                                InteractableDragItemContainer(
                                    state = interaction,
                                    key = tag(index),
                                    position = index,
                                ) { tabInteractionState ->
                                    Box(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                                .height(ITEM_HEIGHT_DP.dp)
                                                .tabItemListInteractionAnimation(tabInteractionState)
                                                .testTag(tag(index))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Starts a drag on the item at [index] and moves it by [DRAG_DISTANCE]. */
    private fun drag(index: Int) {
        val item = listState.layoutInfo.visibleItemsInfo.first { it.index == index }
        composeTestRule.runOnIdle {
            interactionState.onTouchSlopPassed(offset = item.offset + item.size / 2f, shouldLongPress = false)
            interactionState.onDrag(offset = DRAG_DISTANCE, preserveSelectMode = false)
        }
        composeTestRule.waitForIdle()
    }

    private fun scrollBy(delta: Float) {
        listState.dispatchRawDelta(delta)
        composeTestRule.waitForIdle()
    }

    private fun itemBounds(index: Int): Rect =
        composeTestRule.onNodeWithTag(tag(index)).fetchSemanticsNode().boundsInRoot

    private companion object {
        fun tag(index: Int) = "item-$index"
    }
}
