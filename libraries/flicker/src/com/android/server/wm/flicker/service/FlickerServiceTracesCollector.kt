/*
 * Copyright (C) 2022 The Android Open Source Project
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

import com.android.server.wm.flicker.monitor.LayersTraceMonitor
import com.android.server.wm.flicker.monitor.TraceMonitor
import com.android.server.wm.flicker.monitor.TransitionsTraceMonitor
import com.android.server.wm.flicker.monitor.WindowManagerTraceMonitor
import com.android.server.wm.flicker.service.ITracesCollector.Companion.Traces
import com.android.server.wm.traces.common.layers.LayersTrace
import com.android.server.wm.traces.common.transition.TransitionsTrace
import com.android.server.wm.traces.common.windowmanager.WindowManagerTrace
import com.android.server.wm.traces.parser.layers.LayersTraceParser
import com.android.server.wm.traces.parser.transition.TransitionsTraceParser
import com.android.server.wm.traces.parser.windowmanager.WindowManagerTraceParser
import java.nio.file.Files
import java.nio.file.Path

class FlickerServiceTracesCollector(val outputDir: Path) : ITracesCollector {

    private val traceMonitors = listOf<TraceMonitor>(
        WindowManagerTraceMonitor(outputDir),
        LayersTraceMonitor(outputDir),
        TransitionsTraceMonitor(outputDir)
    )

    private var wmTrace: WindowManagerTrace? = null
    private var layersTrace: LayersTrace? = null
    private var transitionsTrace: TransitionsTrace? = null

    override fun start() {
        reset()
        traceMonitors.forEach {
            it.start()
        }
    }

    override fun stop() {
        Files.createDirectories(outputDir)

        traceMonitors.forEach {
            it.stop()
        }

        wmTrace = getWindowManagerTraceFromFile(
            FlickerService.getFassFilePath(outputDir, "wm_trace")
        )
        layersTrace = getLayersTraceFromFile(
            FlickerService.getFassFilePath(outputDir, "layers_trace")
        )
        transitionsTrace = getTransitionsTraceFromFile(
            FlickerService.getFassFilePath(outputDir, "transition_trace")
        )
    }

    override fun getCollectedTraces(): Traces {
        val wmTrace = wmTrace ?: error("Make sure tracing was stopped before calling this")
        val layersTrace = layersTrace ?: error("Make sure tracing was stopped before calling this")
        val transitionsTrace = transitionsTrace
            ?: error("Make sure tracing was stopped before calling this")
        return Traces(wmTrace, layersTrace, transitionsTrace)
    }

    private fun reset() {
        wmTrace = null
        layersTrace = null
        transitionsTrace = null
        cleanupTraceFiles()
    }

    /**
     * Remove the WM trace and layers trace files collected from previous test runs if the
     * directory exists.
     */
    private fun cleanupTraceFiles() {
        if (Files.exists(outputDir)) {
            Files.list(outputDir).forEach { file ->
                if (!Files.isDirectory(file)) {
                    Files.delete(file)
                }
            }
        }
    }

    /**
     * Parse the window manager trace file.
     *
     * @param traceFilePath
     * @return parsed window manager trace.
     */
    private fun getWindowManagerTraceFromFile(traceFilePath: Path): WindowManagerTrace {
        val wmTraceByteArray: ByteArray = Files.readAllBytes(traceFilePath)
        return WindowManagerTraceParser.parseFromTrace(wmTraceByteArray)
    }

    /**
     * Parse the layers trace file.
     *
     * @param traceFilePath
     * @return parsed layers trace.
     */
    private fun getLayersTraceFromFile(traceFilePath: Path): LayersTrace {
        val layersTraceByteArray: ByteArray = Files.readAllBytes(traceFilePath)
        return LayersTraceParser.parseFromTrace(layersTraceByteArray)
    }

    /**
     * Parse the transitions trace file.
     *
     * @param traceFilePath
     * @return parsed transitions trace.
     */
    private fun getTransitionsTraceFromFile(traceFilePath: Path): TransitionsTrace {
        val transitionsTraceByteArray: ByteArray = Files.readAllBytes(traceFilePath)
        return TransitionsTraceParser.parseFromTrace(transitionsTraceByteArray)
    }

    companion object {
        val TEST_TAG = "fass"
    }
}
