package com.omnidev.systemutility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.omnidev.systemutility.R

class FloatingOverlayView(context: Context) : LinearLayout(context) {

    private var onDragRequested: (() -> Unit)? = null
    private var onCloseRequested: (() -> Unit)? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.floating_overlay, this, true)

        setOrientation(VERTICAL)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        val btnDrag: Button = findViewById(R.id.btnOverlayDrag)
        val btnClose: Button = findViewById(R.id.btnOverlayClose)

        btnDrag.setOnClickListener {
            onDragRequested?.invoke()
        }

        btnClose.setOnClickListener {
            onCloseRequested?.invoke()
        }
    }

    fun setOnDragRequested(listener: () -> Unit) {
        onDragRequested = listener
    }

    fun setOnCloseRequested(listener: () -> Unit) {
        onCloseRequested = listener
    }
}