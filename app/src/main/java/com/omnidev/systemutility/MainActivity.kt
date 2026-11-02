package com.omnidev.systemutility

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.omnidev.systemutility.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updateShizukuStatus()
    }

    private fun setupClickListeners() {
        binding.btnStartFloating.setOnClickListener { startFloatingController() }
        binding.btnStopFloating.setOnClickListener { stopFloatingController() }
        binding.btnOpenAccessibility.setOnClickListener { openAccessibilitySettings() }
        binding.btnShizukuStatus.setOnClickListener { updateShizukuStatus() }
        binding.btnRequestShizuku.setOnClickListener { requestShizukuPermission() }
        binding.btnExecuteDrag.setOnClickListener { executeSimulatedDragDemo() }
    }

    private fun startFloatingController() {
        if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
            return
        }
        val intent = Intent(this, SecureFloatingControllerService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingController() {
        val intent = Intent(this, SecureFloatingControllerService::class.java).apply {
            action = SecureFloatingControllerService.ACTION_STOP_FOREGROUND
        }
        startService(intent)
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_rationale_overlay))
            .setMessage("Grant the 'Display over other apps' permission to enable the floating controller.")
            .setPositiveButton("Grant Permission") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun requestShizukuPermission() {
        ShizukuExecutionModule.openShizukuSettings(this)
    }

    private fun updateShizukuStatus() {
        val bound = ShizukuExecutionModule.checkBindingStatus(this)
        val text = if (bound) getString(R.string.status_shizuku_bound) else getString(R.string.status_shizuku_unbound)
        binding.tvStatus.text = text
    }

    private fun executeSimulatedDragDemo() {
        val service = AccessibilityGestureService()
        service.executeSimulatedDrag(300f, 500f, 700f, 500f, 500L)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        updateShizukuStatus()
    }
}