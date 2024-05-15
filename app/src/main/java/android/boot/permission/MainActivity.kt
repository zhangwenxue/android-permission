package android.boot.permission

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PermissionSetup.installRuntimeExplanation(
            listOf("android.permission.RECORD_AUDIO"),
            object : PermissionExplanation {
                override fun onExplain(
                    activity: Activity,
                    onContinue: () -> Unit,
                    onCancel: () -> Unit
                ) {
                    MaterialAlertDialogBuilder(activity)
                        .setCancelable(false)
                        .setTitle("申请权限").setMessage("")
                        .setPositiveButton(
                            "确定"
                        ) { dialog, _ ->
                            dialog.dismiss()
                            onContinue()
                        }.setNegativeButton("取消") { dialog, _ ->
                            dialog.dismiss()
                            onCancel()
                        }.show()
                }
            })
        setContentView(R.layout.activity_main)
        findViewById<View>(android.boot.permission.R.id.clickMe).setOnClickListener {
            withPermission(
                "android.permission.RECORD_AUDIO",
            ) {
                guideToSettingsOnPermanentlyDeny = true
                onAllGranted {
                    Toast.makeText(this@MainActivity, "用户授权", Toast.LENGTH_SHORT).show()
                }

                onPermanentlyDeny {
                    Toast.makeText(this@MainActivity, "取消并不再提醒", Toast.LENGTH_SHORT).show()
                }

                onDeny {
                    Toast.makeText(this@MainActivity, "用户取消", Toast.LENGTH_SHORT).show()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}