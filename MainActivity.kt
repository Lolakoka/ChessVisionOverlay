package com.lu4sti.chessvision

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isWhite = true
    private lateinit var projectionManager: MediaProjectionManager

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, ChessVisionService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("DATA", result.data)
                putExtra("IS_WHITE", isWhite)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            moveTaskToBack(true)
        } else {
            Toast.makeText(this, "Нужно разрешение на захват экрана", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "ChessVision Overlay"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        val rbWhite = RadioButton(this).apply {
            text = "Играю Белыми"
            isChecked = true
            setOnClickListener { isWhite = true }
        }

        val rbBlack = RadioButton(this).apply {
            text = "Играю Чёрными"
            setOnClickListener { isWhite = false }
        }

        radioGroup.addView(rbWhite)
        radioGroup.addView(rbBlack)

        val btnStart = Button(this).apply {
            text = "Запустить оверлей"
            setOnClickListener { checkPermissionsAndStart() }
        }

        val btnStop = Button(this).apply {
            text = "Остановить"
            setOnClickListener {
                stopService(Intent(this@MainActivity, ChessVisionService::class.java))
            }
        }

        layout.addView(title)
        layout.addView(radioGroup)
        layout.addView(btnStart)
        layout.addView(btnStop)

        setContentView(layout)
    }

    private fun checkPermissionsAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Разреши показ поверх других окон", Toast.LENGTH_LONG).show()
            return
        }
        screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
    }
}
