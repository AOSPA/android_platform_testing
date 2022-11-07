package com.android.server.wm.flicker.monitor

import android.util.EventLog
import com.android.server.wm.flicker.newTestResultWriter
import com.android.server.wm.flicker.traces.eventlog.FocusEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contains [EventLogMonitor] tests. To run this test: {@code atest
 * FlickerLibTest:EventLogMonitorTest}
 */
class EventLogMonitorTest {
    @Test
    fun canCaptureFocusEventLogs() {
        val monitor = EventLogMonitor()
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 111 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus leaving 222 com.google.android.apps.nexuslauncher/" +
                "com.google.android.apps.nexuslauncher.NexusLauncherActivity (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 333 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        monitor.start()
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus leaving 4749f88 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 7c01447 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        monitor.stop()
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 2aa30cd com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )

        val writer = newTestResultWriter()
        monitor.setResult(writer)
        val result = writer.write()

        assertEquals(2, result.eventLog?.size)
        assertEquals(
            "4749f88 com.android.phone/com.android.phone.settings.fdn.FdnSetting (server)",
            result.eventLog?.get(0)?.window
        )
        assertEquals(FocusEvent.Focus.LOST, result.eventLog?.get(0)?.focus)
        assertEquals(
            "7c01447 com.android.phone/com.android.phone.settings.fdn.FdnSetting (server)",
            result.eventLog?.get(1)?.window
        )
        assertEquals(FocusEvent.Focus.GAINED, result.eventLog?.get(1)?.focus)
        assertTrue(
            (result.eventLog?.get(0)?.timestamp
                ?: error("missing timestamp")) <=
                (result.eventLog?.get(1)?.timestamp ?: error("missing timestamp"))
        )
        assertEquals(result.eventLog?.get(0)?.reason, "test")
    }

    @Test
    fun onlyCapturesLastTransition() {
        val monitor = EventLogMonitor()
        monitor.start()
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus leaving 11111 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 22222 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        monitor.stop()

        monitor.start()
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus leaving 479f88 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 7c01447 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        monitor.stop()

        val writer = newTestResultWriter()
        monitor.setResult(writer)
        val result = writer.write()

        assertEquals(2, result.eventLog?.size)
        assertEquals(
            "479f88 com.android.phone/com.android.phone.settings.fdn.FdnSetting (server)",
            result.eventLog?.get(0)?.window
        )
        assertEquals(FocusEvent.Focus.LOST, result.eventLog?.get(0)?.focus)
        assertEquals(
            "7c01447 com.android.phone/com.android.phone.settings.fdn.FdnSetting (server)",
            result.eventLog?.get(1)?.window
        )
        assertEquals(FocusEvent.Focus.GAINED, result.eventLog?.get(1)?.focus)
        assertTrue(
            (result.eventLog?.get(0)?.timestamp
                ?: error("missing timestamp")) <=
                (result.eventLog?.get(1)?.timestamp ?: error("missing timestamp"))
        )
    }

    @Test
    fun ignoreFocusRequestLogs() {
        val monitor = EventLogMonitor()
        monitor.start()
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus leaving 4749f88 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus request 111 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        EventLog.writeEvent(
            INPUT_FOCUS_TAG /* input_focus */,
            "Focus entering 7c01447 com.android.phone/" +
                "com.android.phone.settings.fdn.FdnSetting (server)",
            "reason=test"
        )
        monitor.stop()

        val writer = newTestResultWriter()
        monitor.setResult(writer)
        val result = writer.write()

        assertEquals(2, result.eventLog?.size)
        assertEquals(
            "4749f88 com.android.phone/com.android.phone.settings.fdn.FdnSetting (server)",
            result.eventLog?.get(0)?.window
        )
        assertEquals(FocusEvent.Focus.LOST, result.eventLog?.get(0)?.focus)
        assertEquals(
            "7c01447 com.android.phone/com.android.phone.settings.fdn.FdnSetting (server)",
            result.eventLog?.get(1)?.window
        )
        assertEquals(FocusEvent.Focus.GAINED, result.eventLog?.get(1)?.focus)
        assertTrue(
            (result.eventLog?.get(0)?.timestamp
                ?: error("missing timestamp")) <=
                (result.eventLog?.get(1)?.timestamp ?: error("missing timestamp"))
        )
        assertEquals(result.eventLog?.get(0)?.reason, "test")
    }

    private companion object {
        const val INPUT_FOCUS_TAG = 62001
    }
}
