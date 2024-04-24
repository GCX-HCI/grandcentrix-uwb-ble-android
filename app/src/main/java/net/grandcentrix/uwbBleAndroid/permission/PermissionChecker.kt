package net.grandcentrix.uwbBleAndroid.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionChecker(private val context: Context) {
    private fun hasPermission(permission: String): Boolean = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

    fun hasPermissions(permissions: List<String>): Boolean {
        permissions.forEach {
            if (!hasPermission(it)) {
                return@hasPermissions false
            }
        }
        return true
    }
}
