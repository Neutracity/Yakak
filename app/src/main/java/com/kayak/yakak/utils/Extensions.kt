package com.kayak.yakak.utils

import android.content.Context
import androidx.core.content.ContextCompat
import org.osmdroid.views.overlay.Marker

fun Marker.setIconFromVector(context: Context, resId: Int, color: Int? = null) {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return
    color?.let { drawable.setTint(it) }
    this.icon = drawable
}