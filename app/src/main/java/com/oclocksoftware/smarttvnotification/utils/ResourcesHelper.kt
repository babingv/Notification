package com.oclocksoftware.smarttvnotification.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build

/**
 * Created by babin on 12/19/2017.
 */

object ResourcesHelper {

    fun getColor(context: Context, colorResource: Int): Int {
        val color: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = context.resources.getColor(colorResource, null)
        } else {
            color = context.resources.getColor(colorResource)
        }
        return color
    }

    fun getDrawable(context: Context, drawableResource: Int): Drawable {
        val drawable: Drawable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawable = context.resources.getDrawable(drawableResource, null)
        } else {
            drawable = context.resources.getDrawable(drawableResource)
        }
        return drawable
    }

    fun getColorWithAplha(color: Int, ratio: Float): Int {
        var transColor = 0
        val alpha = Math.round(Color.alpha(color) * ratio)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        transColor = Color.argb(alpha, r, g, b)
        return transColor
    }
}
