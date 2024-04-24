package net.grandcentrix.uwbBleAndroid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.text.TextUtils
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
        mockkStatic(Process::class)
        every { Process.myPid() } returns 0
        every { Process.myUid() } returns 0
    }

    @Test
    fun `hasPermission is granted when returns true`() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns false
        every {
            context.checkPermission(
                any(),
                any(),
                any()
            )
        } returns PackageManager.PERMISSION_GRANTED
        val permissionChecker = PermissionChecker(
            context
        )

        assertTrue(
            permissionChecker.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    @Test
    fun `hasPermission is denied when returns true`() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns false
        every {
            context.checkPermission(
                any(),
                any(),
                any()
            )
        } returns PackageManager.PERMISSION_DENIED
        val permissionChecker = PermissionChecker(
            context
        )

        assertFalse(
            permissionChecker.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    @Test
    fun `hasPermissions is granted when returns true`() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns false
        every {
            context.checkPermission(
                any(),
                any(),
                any()
            )
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
    fun `hasPermissions is denied when returns false`() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns false
        every {
            context.checkPermission(
                any(),
                any(),
                any()
            )
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
    fun `hasPermissions only one is denied when returns false`() {
        mockkStatic(TextUtils::class)
        every { TextUtils.equals(any(), any()) } returns false
        every { context.checkPermission(any(), any(), any()) } returnsMany listOf(
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
