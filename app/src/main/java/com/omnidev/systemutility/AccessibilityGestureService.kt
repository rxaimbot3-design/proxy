package com.omnidev.systemutility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class AccessibilityGestureService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Accessibility events can be monitored here if needed
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    fun executeSimulatedDrag(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)

        val strokeDuration = duration.coerceAtLeast(100)
        val stroke = GestureDescription.StrokeDescription(path, 0, strokeDuration)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(stroke)
        val gestureDescription = gestureBuilder.build()

        dispatchGesture(gestureDescription, null, null)
    }

    fun executeSimulatedDragAsync(startX: Float, startY: Float, endX: Float, endY: Float,
                                  duration: Long, callback: (success: Boolean) -> Unit) {
        mainHandler.post {
            try {
                executeSimulatedDrag(startX, startY, endX, endY, duration)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun executeSimulatedTap(x: Float, y: Float, duration: Long = 100) {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, duration.coerceAtLeast(50))
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(stroke)
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    fun executeSimulatedLongPress(x: Float, y: Float, duration: Long = 1000) {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, duration.coerceAtLeast(500))
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(stroke)
        dispatchGesture(gestureBuilder.build(), null, null)
    }
}