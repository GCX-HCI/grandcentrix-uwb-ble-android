package net.grandcentrix.uwbBleAndroid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PermissionCheckerTest {

    private val context: Context = mockk()

    @BeforeEach
    fun setUp() {
        mockkStatic(ContextCompat::class)
    }

    @Test
    fun `Given permission is granted, when hasPermission is called, then returns true`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED
        val permissionChecker = PermissionChecker(
            context
        )

        assertTrue(
            permissionChecker.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    @Test
    fun `Given permission is denied, when hasPermission is called, then returns false`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_DENIED
        val permissionChecker = PermissionChecker(
            context
        )

        assertFalse(
            permissionChecker.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    @Test
    fun `Given permissions are granted, when hasPermissions is called, then returns true`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED
        val permissionChecker = PermissionChecker(
            context
        )

        assertTrue(
            permissionChecker.hasPermissions(
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        )
    }

    @Test
    fun `Given permissions are denied, when hasPermissions is called, then returns false`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_DENIED
        val permissionChecker = PermissionChecker(
            context
        )

        assertFalse(
            permissionChecker.hasPermissions(
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        )
    }

    @Test
    fun `Given permissions are granted besides one is denied when hasPermissions is called, then returns false`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returnsMany listOf(
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_DENIED
        )

        val permissionChecker = PermissionChecker(
            context
        )

        assertFalse(
            permissionChecker.hasPermissions(
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        )
    }
}
