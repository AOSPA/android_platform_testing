/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm.flicker.service

import com.android.server.wm.flicker.service.assertors.AssertionResult
import com.android.server.wm.flicker.service.assertors.TransitionAsserter
import com.android.server.wm.flicker.service.config.FlickerServiceConfig
import com.android.server.wm.traces.common.layers.LayersTrace
import com.android.server.wm.traces.common.transition.Transition
import com.android.server.wm.traces.common.transition.TransitionsTrace
import com.android.server.wm.traces.common.windowmanager.WindowManagerTrace

/**
 * Invokes the configured assertors and summarizes the results.
 */
class AssertionEngine(
    private val config: FlickerServiceConfig,
    private val logger: (String) -> Unit
) {
    fun analyze(
        wmTrace: WindowManagerTrace,
        layersTrace: LayersTrace,
        transitionsTrace: TransitionsTrace
    ): List<AssertionResult> {
        logger.invoke("AssertionEngine#analyze")

        val assertionResults = mutableListOf<AssertionResult>()
        for (transition in transitionsTrace.entries) {
            if (transition.isIncomplete) {
                // We don't want to run assertions on incomplete transitions
                logger.invoke("Skipping running assertions on incomplete transition $transition")
                continue
            }

            val (transitionWmTrace, transitionLayersTrace) =
                splitTraces(transition, wmTrace, layersTrace)

            val assertionsToCheck = config.assertionsForTransition(transition)
            logger.invoke("${assertionsToCheck.size} assertions to check for $transition")

            val result = TransitionAsserter(assertionsToCheck, logger)
                .analyze(transition, transitionWmTrace, transitionLayersTrace)
            assertionResults.addAll(result)
        }

        return assertionResults
    }

    /**
     * Splits a [WindowManagerTrace] and a [LayersTrace] by a [Transition].
     *
     * @param tag a list with all [TransitionTag]s
     * @param wmTrace Window Manager trace
     * @param layersTrace Surface Flinger trace
     * @return a list with [WindowManagerTrace] blocks
     */
    fun splitTraces(
        transition: Transition,
        wmTrace: WindowManagerTrace,
        layersTrace: LayersTrace
    ): Pair<WindowManagerTrace, LayersTrace> {
        // TODO (b/230462538): Use better synchronization mechanisms between traces
        val filteredWmTrace = wmTrace.filter(transition.start, transition.end)
        val filteredLayersTrace = layersTrace.filter(transition.start, transition.end)
        return Pair(filteredWmTrace, filteredLayersTrace)
    }
}
