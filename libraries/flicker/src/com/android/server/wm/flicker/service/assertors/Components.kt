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

package com.android.server.wm.flicker.service.assertors

import android.content.ComponentName
import com.android.server.wm.flicker.service.IScenarioInstance
import com.android.server.wm.traces.common.ComponentNameMatcher
import com.android.server.wm.traces.common.IComponentMatcher
import com.android.server.wm.traces.common.transition.Transition
import com.android.server.wm.traces.common.transition.Transition.Companion.Type.CLOSE
import com.android.server.wm.traces.common.transition.Transition.Companion.Type.OPEN
import com.android.server.wm.traces.common.transition.Transition.Companion.Type.TO_BACK
import com.android.server.wm.traces.common.transition.Transition.Companion.Type.TO_FRONT

data class ComponentBuilder(
    val name: String,
    val build: (scenarioInstance: IScenarioInstance) -> IComponentMatcher
) {
    override fun equals(other: Any?): Boolean {
        return other is ComponentBuilder && name == other.name && build == other.build
    }

    override fun hashCode(): Int {
        return name.hashCode() * 39 + build.hashCode()
    }
}

object Components {

    val NAV_BAR = ComponentBuilder("Navbar") { ComponentNameMatcher.NAV_BAR }
    val STATUS_BAR = ComponentBuilder("StatusBar") { ComponentNameMatcher.STATUS_BAR }
    val LAUNCHER = ComponentBuilder("Launcher") { ComponentNameMatcher.LAUNCHER }

    val OPENING_APP =
        ComponentBuilder("OPENING_APP") { scenarioInstance: IScenarioInstance ->
            openingAppFrom(
                scenarioInstance.associatedTransition ?: error("Missing associated transition")
            )
        }
    val CLOSING_APP =
        ComponentBuilder("CLOSING_APP") { scenarioInstance: IScenarioInstance ->
            closingAppFrom(
                scenarioInstance.associatedTransition ?: error("Missing associated transition")
            )
        }

    val EMPTY = ComponentBuilder("") { ComponentNameMatcher("", "") }

    // TODO: Extract out common code between two functions below
    private fun openingAppFrom(transition: Transition): IComponentMatcher {
        val openingWindows =
            transition.changes.filter {
                it.transitMode == OPEN ||
                    it.transitMode == TO_FRONT &&
                        !it.windowName.matches("Splash Screen ([a-z]|\\.)+".toRegex())
            }

        val windowNames = openingWindows.map { it.windowName }.distinct()
        if (windowNames.size > 1) {
            error(
                "Was not expecting more than one opening windowNames got " +
                    windowNames.joinToString()
            )
        }
        if (windowNames.isEmpty()) {
            error("No opening windows for $transition...")
        }

        // TODO: (b/231974873) use windowId instead of window name to match instead
        val openWindowName = openingWindows.first().windowName
        val component =
            ComponentName.unflattenFromString(openWindowName)
                ?: error("Failed to unflatten $openWindowName")

        return ComponentNameMatcher(component.packageName, component.className)
    }

    private fun closingAppFrom(transition: Transition): IComponentMatcher {
        val closingWindows =
            transition.changes.filter { it.transitMode == CLOSE || it.transitMode == TO_BACK }

        val windowNames = closingWindows.map { it.windowName }.distinct()
        if (windowNames.size > 1) {
            error(
                "Was not expecting more than one closing windowNames got " +
                    windowNames.joinToString()
            )
        }
        if (windowNames.isEmpty()) {
            error("No closing windows for $transition...")
        }

        // TODO: (b/231974873) use windowId instead of window name to match instead
        val closeWindowName = closingWindows.first().windowName
        val component =
            ComponentName.unflattenFromString(closeWindowName)
                ?: error("Failed to unflatten $closeWindowName")

        return ComponentNameMatcher(component.packageName, component.className)
    }

    val byType: Map<String, ComponentBuilder> =
        mapOf("OPENING_APP" to OPENING_APP, "CLOSING_APP" to CLOSING_APP)
}
