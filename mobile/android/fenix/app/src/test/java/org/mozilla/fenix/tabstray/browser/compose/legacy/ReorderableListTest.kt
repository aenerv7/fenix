/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.browser.compose.legacy

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntSize
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import mozilla.components.compose.draggable.ItemInteractionHandler
import mozilla.components.compose.draggable.NoOpItemInteractionHandler
import org.junit.Test
import org.mozilla.fenix.tabstray.browser.compose.ListReorderState

class ReorderableListTest {
    private val testDispatcher = StandardTestDispatcher()
    private val scope = TestScope(testDispatcher)

    @Test
    fun `GIVEN an item is dragged down onto another THEN onMove is called with placeAfter true`() {
        val handler = mockk<ItemInteractionHandler>(relaxed = true)
        val dragItemOffset = 10
        val targetItemOffset = 30
        val reorderState =
            fakeListReorderState(
                mockListState(
                    mockItems =
                        listOf(
                            mockk<LazyListItemInfo> {
                                every { key } returns "key1"
                                every { index } returns 1
                                every { size } returns 10
                                every { offset } returns dragItemOffset
                            },
                            mockk<LazyListItemInfo> {
                                every { key } returns "key2"
                                every { index } returns 2
                                every { size } returns 10
                                every { offset } returns targetItemOffset
                            },
                        )
                ),
                handler = handler,
            )

        reorderState.onTouchSlopPassed(offset = dragItemOffset.toFloat(), shouldLongPress = true)
        reorderState.onDrag(offset = 20f) // 20 down

        verify { handler.onMove("key1", "key2", true) }
    }

    @Test
    fun `GIVEN an item is dragged up onto another THEN onMove is called with placeAfter false`() {
        val handler = mockk<ItemInteractionHandler>(relaxed = true)
        val dragItemOffset = 30
        val targetItemOffset = 10
        val reorderState =
            fakeListReorderState(
                mockListState(
                    mockItems =
                        listOf(
                            mockk<LazyListItemInfo> {
                                every { key } returns "key1"
                                every { index } returns 1
                                every { size } returns 10
                                every { offset } returns dragItemOffset
                            },
                            mockk<LazyListItemInfo> {
                                every { key } returns "key2"
                                every { index } returns 2
                                every { size } returns 10
                                every { offset } returns targetItemOffset
                            },
                        )
                ),
                handler = handler,
            )

        reorderState.onTouchSlopPassed(dragItemOffset.toFloat(), true)
        reorderState.onDrag(offset = -20f) // 20 up

        verify { handler.onMove("key1", "key2", true) }
    }

    @Test
    fun `GIVEN a long press drag WHEN the pointer keeps moving THEN the exit long press callback is only invoked once`() {
        val handler = mockk<ItemInteractionHandler>(relaxed = true)
        val dragItemOffset = 10
        val reorderState =
            fakeListReorderState(
                mockListState(
                    mockItems =
                        listOf(
                            mockk<LazyListItemInfo> {
                                every { key } returns "key1"
                                every { index } returns 1
                                every { size } returns 10
                                every { offset } returns dragItemOffset
                            }
                        )
                ),
                handler = handler,
            )

        reorderState.onTouchSlopPassed(offset = dragItemOffset.toFloat(), shouldLongPress = true)
        reorderState.onDrag(offset = 20f)
        reorderState.onDrag(offset = 20f)
        reorderState.onDrag(offset = 20f)

        verify(exactly = 1) { handler.onDragStart(sourceKey = "key1", preserveSelectMode = true) }
    }

    @Test
    fun `GIVEN a drag is in progress and onDragInterrupted is called THEN the previous key is saved and the state is reset`() {
        val reorderState = fakeListReorderState(listState = mockListState(mockItems = emptyList()))
        val draggingItemKey = reorderState.draggingItemKey

        reorderState.onTouchSlopPassed(offset = 0f, shouldLongPress = false)
        reorderState.onDragInterrupted()

        assertEquals(expected = draggingItemKey, actual = reorderState.previousKeyOfDraggedItem)
        assertNull(reorderState.draggingItemKey)
    }

    @Test
    fun `GIVEN the coordinates are a frame old WHEN the item is still laid out THEN the offset follows the layout slot`() {
        val reorderState =
            fakeListReorderState(
                mockListState(mockItems = listOf(mockListItem(key = "key1", index = 0, size = 10, offset = 80)))
            )
        reorderState.updateListCoordinates(mockCoordinates(position = 160f))
        reorderState.onTouchSlopPassed(offset = 85f, shouldLongPress = false)

        // A reorder moved the item to slot 80, but the coordinates callback still reports the slot it left.
        reorderState.onDraggedItemPositioned(mockk(relaxed = true))
        reorderState.onDrag(offset = -20f)

        // 80 (start slot) - 20 (drag) - 80 (live slot); the stale 160 would give -100.
        assertEquals(-20f, reorderState.computeItemOffset(), 0.01f)
    }

    @Test
    fun `GIVEN the item has left the layout WHEN the drag continues THEN the offset uses the last coordinates`() {
        var visibleItems: List<LazyListItemInfo> = listOf(mockListItem(key = "key1", index = 0, size = 10, offset = 80))
        val reorderState =
            fakeListReorderState(
                mockk {
                    every { firstVisibleItemIndex } returns 0
                    every { isScrollInProgress } returns false
                    every { layoutInfo } returns
                        mockk {
                            every { visibleItemsInfo } answers { visibleItems }
                            every { orientation } returns Orientation.Vertical
                            every { viewportSize } returns IntSize(10, 100)
                            every { viewportStartOffset } returns 0
                            every { viewportEndOffset } returns 100
                            every { totalItemsCount } returns 1
                            every { mainAxisItemSpacing } returns 10
                            every { beforeContentPadding } returns 0
                        }
                }
            )
        reorderState.updateListCoordinates(mockCoordinates(position = 120f))
        reorderState.onTouchSlopPassed(offset = 85f, shouldLongPress = false)
        reorderState.onDraggedItemPositioned(mockk(relaxed = true))

        visibleItems = emptyList()
        reorderState.onDrag(offset = -20f)

        // The item is pinned at slot 120 and keeps drawing there, so the coordinates stay the reference.
        assertEquals(-60f, reorderState.computeItemOffset(), 0.01f)
    }

    private fun mockCoordinates(position: Float): LayoutCoordinates = mockk {
        every { localPositionOf(any(), any()) } returns Offset(0f, position)
    }

    @Test
    fun `WHEN the dragged item is removed THEN resetForItemChange clears the drag pin`() {
        val reorderState = fakeListReorderState(mockListState(mockItems = listOf(mockListItem(key = "key1"))))

        reorderState.onTouchSlopPassed(offset = 0f, shouldLongPress = false)
        reorderState.resetForItemChange()

        assertNull(reorderState.draggingItemKey)
    }

    @Test
    fun `WHEN the item set changes and the dragged item survives THEN the drag pin is cleared`() {
        val reorderState = fakeListReorderState(mockListState(mockItems = listOf(mockListItem(key = "key1"))))

        reorderState.onTouchSlopPassed(offset = 0f, shouldLongPress = false)
        // "key1" survives, but another tab was grouped away, so the drag geometry is stale. Without a
        // reset the dragged item stays pinned at its old screen position while the rest of the list reflows.
        reorderState.resetForItemChange()

        assertNull(reorderState.draggingItemKey)
    }

    @Test
    fun `WHEN the item set changes and the previously dragged item survives THEN the interaction is reset`() {
        val reorderState = fakeListReorderState(mockListState(mockItems = listOf(mockListItem(key = "key1"))))

        reorderState.onTouchSlopPassed(offset = 0f, shouldLongPress = false)
        reorderState.onDragInterrupted()
        reorderState.resetForItemChange()

        assertNull(reorderState.previousKeyOfDraggedItem)
    }

    private fun mockListItem(
        key: String,
        index: Int = 0,
        size: Int = 10,
        offset: Int = 0,
    ): LazyListItemInfo = mockk {
        every { this@mockk.key } returns key
        every { this@mockk.index } returns index
        every { this@mockk.size } returns size
        every { this@mockk.offset } returns offset
    }

    private fun mockListState(
        mockItems: List<LazyListItemInfo> = emptyList(),
        firstVisibleIndex: Int = 0,
        totalItems: Int = mockItems.size,
    ): LazyListState {
        return mockk<LazyListState> {
            every { firstVisibleItemIndex } returns firstVisibleIndex
            every { layoutInfo } returns
                mockk {
                    every { visibleItemsInfo } returns mockItems
                    every { orientation } returns Orientation.Vertical
                    every { viewportSize } returns IntSize(10, 10)
                    every { firstVisibleItemIndex } returns firstVisibleIndex
                    every { totalItemsCount } returns totalItems
                    every { mainAxisItemSpacing } returns 10
                    every { beforeContentPadding } returns 10
                    every { viewportStartOffset } returns 0
                    every { viewportEndOffset } returns 10
                }
            every { isScrollInProgress } returns false
        }
    }

    private fun fakeListReorderState(
        listState: LazyListState,
        handler: ItemInteractionHandler = NoOpItemInteractionHandler,
    ): ListReorderState {
        return ListReorderState(
            listState = listState,
            scope = scope,
            touchSlop = 0f,
            ignoredItems = emptyList(),
            onLongPress = { _ -> },
            hapticFeedback =
                mockk<HapticFeedback> {
                    every { performHapticFeedback(any()) } just Runs
                },
            onMove = { initialTab, newTab ->
                handler.onMove(
                    sourceKey = initialTab.key as String,
                    targetKey = newTab.key as String,
                    placeAfter = initialTab.index < newTab.index,
                )
            },
            onExitLongPress = { sourceKey ->
                handler.onDragStart(sourceKey = sourceKey as String, preserveSelectMode = true)
            },
        )
    }
}
