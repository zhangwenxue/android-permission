package android.boot.permission

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        findViewById<View>(android.boot.permission.R.id.clickMe).setOnClickListener {
            withPermission(
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_MEDIA_IMAGES"
            ) {
                onAllGranted {
                    Toast.makeText(this@MainActivity, "all granted!", Toast.LENGTH_SHORT).show()
                }

                onPermanentlyDeny {
                    Toast.makeText(this@MainActivity, "onPermanentlyDeny!", Toast.LENGTH_SHORT)
                        .show()
                }

                onDeny {
                    Toast.makeText(this@MainActivity, "denied!", Toast.LENGTH_SHORT).show()
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