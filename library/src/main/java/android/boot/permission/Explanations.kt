package android.boot.permission

import android.app.Activity

interface PermissionExplanation {
    fun onExplain(activity: Activity, onContinue: () -> Unit, onCancel: () -> Unit)
}


interface PermanentlyDeniedExplanation {
    fun onExplain(activity: Activity, onContinue: () -> Unit, onCancel: () -> Unit)
}


object PermissionSetup {
    internal val runtimeExplainMap = mutableMapOf<List<String>, PermissionExplanation>()
    internal val permanentlyDeniedExplanationMap =
        mutableMapOf<List<String>, PermanentlyDeniedExplanation>()

    fun installRuntimeExplanation(
        permissions: List<String>,
        explanation: PermissionExplanation
    ) {
        runtimeExplainMap[permissions] = explanation
    }

    fun installPermanentlyDeniedExplanation(
        permissions: List<String>,
        permissionExplanation: PermanentlyDeniedExplanation
    ) {
        permanentlyDeniedExplanationMap[permissions] = permissionExplanation
    }
}

