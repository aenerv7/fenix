/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.browser.compose.legacy

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.compose.base.theme.Theme
import mozilla.components.compose.base.utils.LocalUnderTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.tabstray.browser.compose.ListReorderState
import org.mozilla.fenix.tabstray.browser.compose.ReorderableDragItemContainer
import org.mozilla.fenix.tabstray.browser.compose.createListReorderState
import org.mozilla.fenix.tabstray.browser.compose.detectListPressAndDrag
import org.mozilla.fenix.theme.FirefoxTheme

// Number of tab items rendered by the harness list.
private const val ITEM_COUNT = 20

// The harness list viewport height, in dp.
private const val VIEWPORT_HEIGHT_DP = 400

// Every harness tab item is this tall, in dp.
private const val ITEM_HEIGHT_DP = 80

// How far the drag moves per dispatched drag event, in px.
private const val DRAG_STEP_PX = 20f

// Number of drag events dispatched, so that the drag covers two and a half rows without reaching the viewport edge.
private const val DRAG_STEPS = 10

// The index of the item the drag starts on.
private const val DRAGGED_INDEX = 0

/**
 * Verifies where a dragged tab item renders in the list while the list reorders underneath it.
 *
 * A reorder moves the dragged item to another slot, and a drag past the viewport edge scrolls the list, so the
 * translation has to keep following the item rather than the slot its composition was created with.
 */
@RunWith(AndroidJUnit4::class)
class ReorderableListDragTest {
    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var listState: LazyListState
    private lateinit var reorderState: ListReorderState
    private lateinit var items: SnapshotStateList<String>

    @Test
    fun `GIVEN a long press drag WHEN the pointer moves over other tabs THEN the dragged tab follows the pointer`() {
        setHarness()
        val key = items[DRAGGED_INDEX]
        val startTop = itemBounds(key).top

        drag(steps = DRAG_STEPS)

        assertEquals(startTop + DRAG_STEPS * DRAG_STEP_PX, itemBounds(key).top, 1f)
    }

    private fun setHarness() {
        composeTestRule.setContent {
            Harness(
                onState = { state, reorder ->
                    listState = state
                    reorderState = reorder
                }
            )
        }
        composeTestRule.waitForIdle()
    }

    @Composable
    private fun Harness(onState: (LazyListState, ListReorderState) -> Unit) {
        CompositionLocalProvider(LocalUnderTest provides true) {
            FirefoxTheme(theme = Theme.Light) {
                Surface {
                    val state = rememberLazyListState()
                    items = remember { mutableStateListOf(*Array(ITEM_COUNT) { tag(it) }) }
                    val reorder =
                        createListReorderState(
                            listState = state,
                            onMove = { source, target ->
                                items.move(source.key as String, target.key as String)
                            },
                            ignoredItems = emptyList(),
                        )
                    onState(state, reorder)

                    Box(
                        modifier =
                            Modifier.size(width = 200.dp, height = VIEWPORT_HEIGHT_DP.dp)
                                .detectListPressAndDrag(
                                    listState = state,
                                    reorderState = reorder,
                                    shouldLongPressToDrag = true,
                                )
                    ) {
                        LazyColumn(state = state) {
                            items.forEach { key ->
                                item(key = key) {
                                    ReorderableDragItemContainer(
                                        state = reorder,
                                        key = key,
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth().height(ITEM_HEIGHT_DP.dp).testTag(key))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Starts a long press drag on [DRAGGED_INDEX] and moves it by [steps] drag events. */
    private fun drag(steps: Int) {
        val item = listState.layoutInfo.visibleItemsInfo.first { it.index == DRAGGED_INDEX }
        composeTestRule.runOnIdle {
            reorderState.onTouchSlopPassed(
                offset = item.offset + item.size / 2f,
                shouldLongPress = false,
            )
        }
        repeat(steps) {
            composeTestRule.runOnIdle {
                reorderState.onDrag(offset = DRAG_STEP_PX)
            }
            composeTestRule.waitForIdle()
        }
    }

    private fun itemBounds(key: String): Rect = composeTestRule.onNodeWithTag(key).fetchSemanticsNode().boundsInRoot

    private companion object {
        fun tag(index: Int) = "item-$index"
    }
}

/** Moves the item identified by [source] next to the one identified by [target]. */
private fun SnapshotStateList<String>.move(source: String, target: String) {
    val from = indexOf(source)
    val to = indexOf(target)
    if (from == -1 || to == -1 || from == to) {
        return
    }
    val item = removeAt(from)
    add(to, item)
}
