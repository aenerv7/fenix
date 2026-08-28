/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.gecko

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mozilla.components.concept.engine.EngineSession
import org.junit.Before
import org.junit.Test
import org.mozilla.fenix.helpers.TestHelper
import kotlin.test.assertNotNull

class CrashPullDelegateTest {
    private lateinit var context: Context
    private lateinit var mockPolicy: EngineSession.TrackingProtectionPolicy
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    @Before
    fun setUp() {
        context = TestHelper.appContext
        mockPolicy = mockk<EngineSession.TrackingProtectionPolicy>()
    }

    @Test
    fun test_crash_pull_delegate_exists() {
        // scope.launch required to run in the correct thread for GeckoRuntime
        // but the test needs to wait on its completion to ensure assert is
        // verified, hence runBlocking.
        //
        // We cannot use runTestOnMain here because it looks not to be available.
        runBlocking {
            scope.launch {
                val runtime = GeckoProvider.getOrCreateRuntime(context, mockPolicy)
                assertNotNull(runtime.crashPullDelegate)
                runtime.crashPullDelegate?.onCrashPull(arrayOf("1", "2"))
            }.join()
        }
    }
}
