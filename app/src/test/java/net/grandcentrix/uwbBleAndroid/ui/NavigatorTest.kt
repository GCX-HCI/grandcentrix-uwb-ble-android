package net.grandcentrix.uwbBleAndroid.ui

import io.mockk.mockk
import net.grandcentrix.api.ble.model.GcxUwbDevice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NavigatorTest {

    private val gcxUwbDevice: GcxUwbDevice = mockk()

    @Test
    fun `Given initial start of the app, then CONNECT screen should be visible`() {
        val navigator = Navigator()

        assertEquals(Screen.Connect to null, navigator.currentScreen.value)
    }

    @Test
    fun `Given initial start of the app, when navigating to RANGING, then RANGING screen should be visible`() {
        val navigator = Navigator()

        navigator.navigateTo(Screen.Ranging, gcxUwbDevice)

        assertEquals(Screen.Ranging to gcxUwbDevice, navigator.currentScreen.value)
    }
}
