package com.omnidev.systemutility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ShizukuExecutionModule {

    private val executor: ExecutorService = Executors.newFixedThreadPool(4)
    private val mainHandler = Handler(Looper.getMainLooper())

    const string SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const string ACTION_BIND_SHIZUKU = "moe.shizuku.privileged.api.START_BIND"

    var isBound = false
        private set

    fun checkBindingStatus(context: Context): Boolean {
        return isBound
    }

    fun requestWirelessDebuggingPermission(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openShizukuSettings(context: Context) {
        val intent = Intent("moe.shizuku.privileged.api.START_BIND").apply {
            packageName = "moe.shizuku.privileged.api"
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun executeShellCommandAsync(command: String, callback: (success: Boolean, output: String) -> Unit) {
        executor.execute {
            var success = false
            var output = ""

            try {
                val process = Runtime.getRuntime().exec(command)
                val inputStream = process.inputStream.bufferedReader()
                val errorStream = process.errorStream.bufferedReader()

                val outputLines = inputStream.readLines().joinToString("\n")
                val errorLines = errorStream.readLines().joinToString("\n")

                process.waitFor()

                if (process.exitValue() == 0) {
                    success = true
                    output = if (outputLines.isNotEmpty()) outputLines else "Command executed successfully."
                } else {
                    success = false
                    output = if (errorLines.isNotEmpty()) errorLines else "Command failed with exit code ${process.exitValue()}."
                }

                inputStream.close()
                errorStream.close()
            } catch (e: Exception) {
                success = false
                output = "Exception: ${e.message}"
            }

            mainHandler.post {
                callback(success, output)
            }
        }
    }

    fun executeSwipeGesture(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Long,
                            callback: (success: Boolean, output: String) -> Unit) {
        val command = "input swipe $startX $startY $endX $endY $durationMs"
        executeShellCommandAsync(command, callback)
    }

    fun executeTapGesture(x: Int, y: Int, callback: (success: Boolean, output: String) -> Unit) {
        val command = "input tap $x $y"
        executeShellCommandAsync(command, callback)
    }

    fun executeTextInput(text: String, callback: (success: Boolean, output: String) -> Unit) {
        val escaped = text.replace(" ", "%s")
        val command = "input text $escaped"
        executeShellCommandAsync(command, callback)
    }

    fun shutdown() {
        executor.shutdown()
        isBound = false
    }
}