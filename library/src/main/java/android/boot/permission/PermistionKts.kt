package android.boot.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class PermissionScope(
    private val host: Context,
    private val permissions: Array<out String>,
    private val openSettingsOnDeniedPermanently: Boolean = true,
    private val explains: Map<String, String> = emptyMap()
) {
    private var allGranted: (permissions: List<String>) -> Unit = {}
    private var denied: (permissions: List<String>) -> Unit = {}
    private var permanentlyDeny: (permissions: List<String>) -> Unit = {}

    private val multiPermissionListener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            if (report?.areAllPermissionsGranted() == true) {
                allGranted(report.grantedPermissionResponses?.map { it.permissionName }
                    ?: emptyList())
                return
            }

            if (report?.isAnyPermissionPermanentlyDenied == true) {
                // permission is denied permanently, we will show user a dialog message.
                if (openSettingsOnDeniedPermanently) showSettingDialog() else permanentlyDeny(
                    emptyList()
                )
                return
            }

            if (!report?.deniedPermissionResponses.isNullOrEmpty()) {
                denied(report?.deniedPermissionResponses?.map { it.permissionName } ?: emptyList())
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            requests: MutableList<PermissionRequest>?,
            token: PermissionToken?
        ) {
            token?.continuePermissionRequest();
        }
    }


    fun onAllGranted(action: (permissions: List<String>) -> Unit = {}) {
        allGranted = action
    }

    fun onDeny(action: (permissions: List<String>) -> Unit = { }) {
        denied = action
    }

    fun onPermanentlyDeny(action: (permissions: List<String>) -> Unit = {}) {
        permanentlyDeny = action
    }

    internal fun request() {
        Dexter.withContext(host)
            .withPermissions(permissions.toList())
            .withListener(multiPermissionListener)
            .check()
    }

    private fun showSettingDialog(
        title: String? = null,
        permissions: List<String> = emptyList()
    ) {
        MaterialAlertDialogBuilder(host).setTitle(title ?: "请手动授予部分权限").setMessage("")
            .setPositiveButton(
                "去设置"
            ) { dialog, _ ->
                dialog.dismiss()
                openSettings()
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun openSettings() {
        runCatching {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).run {
                setData(Uri.fromParts("package", host.packageName, null))
                if (host !is Activity) {
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                host.startActivity(this)
            }
        }
    }
}


fun Context.withPermission(
    vararg permissions: String,
    openSettingsOnDeniedPermanently: Boolean = true,
    block: PermissionScope.() -> Unit
) {
    PermissionScope(this, permissions, openSettingsOnDeniedPermanently).apply(block).request()
}