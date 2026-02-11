package com.soaharisonstebane.mneme.appView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var spikeWidth = 9f
    private var gap = 6f

    private var screenWidth = 0f
    private var screenHeight = 400f

    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(12, 10, 9)
    }

    fun addAmplitude(amp: Float) {
        var norm = Math.min(amp.toInt()/7, 400).toFloat()
        amplitudes.add(norm)

        spikes.clear()
        var amps = amplitudes.takeLast(maxSpikes)
        for (i in amps.indices) {
            var left = screenWidth - i*(spikeWidth+gap)
            var top = screenHeight/2 - amps[i]/2
            var right = left + spikeWidth
            var bottom = top + amps[i]

            spikes.add(RectF(left, top, right, bottom))
        }


        invalidate() // To trigger the draw method
    }

    fun clear(): ArrayList<Float> {
        val amps = amplitudes.clone() as ArrayList<Float>
        amplitudes.clear()
        spikes.clear()
        invalidate()

        return amps
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()
        maxSpikes = (screenWidth / (spikeWidth+gap)).toInt()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        spikes.forEach {
            canvas.drawRoundRect(it, radius, radius, paint)
        }
    }

}