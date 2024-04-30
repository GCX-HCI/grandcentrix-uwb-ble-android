package net.grandcentrix.uwbBleAndroid.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NavigatorTest {

    @Test
    fun `Given initial start of the app, then CONNECT screen should be visible`() {
        val navigator = Navigator()

        assertEquals(Screen.Connect, navigator.currentScreen.value)
    }

    @Test
    fun `Given initial start of the app, when navigating to RANGING, then RANGING screen should be visible`() {
        val navigator = Navigator()

        navigator.navigateTo(Screen.Ranging)

        assertEquals(Screen.Ranging, navigator.currentScreen.value)
    }
}
