package android.boot.permission

import android.app.Activity
import android.boot.permission.PermissionSetup.runtimeExplainMap
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
    private val host: Activity,
    private val permissions: Array<out String>
) {

    private var allGranted: (permissions: List<String>) -> Unit = {}
    private var denied: (permissions: List<String>) -> Unit = { }
    private var permanentlyDeny: (permissions: List<String>) -> Unit = { }

    private val multiPermissionListener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

            if (report?.areAllPermissionsGranted() == true) {
                allGranted(report.grantedPermissionResponses?.map { it.permissionName }
                    ?: emptyList())
                return
            }

            if (report?.isAnyPermissionPermanentlyDenied == true) {
                // permission is denied permanently, we will show user a dialog message.
                val deniedPermissions =
                    report.deniedPermissionResponses?.filter { it.isPermanentlyDenied }
                        ?.map { it.permissionName } ?: emptyList()
                if (guideToSettingsOnPermanentlyDeny) {
                    permanentlyDeniedExplanation?.onExplain(host, onContinue = { openSettings() }) {
                        permanentlyDeny(deniedPermissions)
                    } ?: showSettingDialog(permissions = deniedPermissions) {
                        permanentlyDeny(deniedPermissions)
                    }

                } else permanentlyDeny(deniedPermissions)
                return
            }

            if (!report?.deniedPermissionResponses.isNullOrEmpty()) {
                denied(report?.deniedPermissionResponses?.map { it.permissionName }
                    ?: emptyList())
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            requests: MutableList<PermissionRequest>?,
            token: PermissionToken?
        ) {
            permissionExplanation ?: runtimeExplainMap[permissions.asList()]?.let {
                it.onExplain(
                    host,
                    onContinue = { token?.continuePermissionRequest() }) { token?.cancelPermissionRequest() }
            } ?: token?.continuePermissionRequest()
        }
    }

    var guideToSettingsOnPermanentlyDeny: Boolean = true

    var permissionExplanation: PermissionExplanation? = null
    var permanentlyDeniedExplanation: PermanentlyDeniedExplanation? = null

    fun onAllGranted(action: (permissions: List<String>) -> Unit = {}) {
        allGranted = action
    }

    fun onDeny(action: (permissions: List<String>) -> Unit = { }) {
        denied = action
    }

    fun onPermanentlyDeny(action: (permissions: List<String>) -> Unit = { }) {
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
        permissions: List<String> = emptyList(),
        onCancel: () -> Unit = {}
    ) {
        MaterialAlertDialogBuilder(host)
            .setCancelable(false)
            .setTitle(title ?: "您已拒绝必要权限,请手动授予").setMessage("")
            .setPositiveButton(
                "去设置"
            ) { dialog, _ ->
                dialog.dismiss()
                openSettings()
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }.show()
    }

    private fun openSettings() {
        runCatching {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).run {
                setData(Uri.fromParts("package", host.packageName, null))
                host.startActivity(this)
            }
        }
    }
}


fun Activity.withPermission(
    vararg permissions: String,
    block: PermissionScope.() -> Unit
) {
    PermissionScope(this, permissions).apply(block).request()
}