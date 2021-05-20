package com.example.photoeditor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


class Cube : View {

    // Кисть для рисования
    private var paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        isDither =
            true // Dithering affects how colors with higher-precision than the device are down-sampled
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = 8F // default: Hairline-width (really thin)
    }

    private var nodes = arrayOf(
        arrayOf(-200.0f, -200.0f, -200.0f),
        arrayOf(-200.0f, -200.0f, 200.0f),
        arrayOf(-200.0f, 200.0f, -200.0f),
        arrayOf(-200.0f, 200.0f, 200.0f),
        arrayOf(200.0f, -200.0f, -200.0f),
        arrayOf(200.0f, -200.0f, 200.0f),
        arrayOf(200.0f, 200.0f, -200.0f),
        arrayOf(200.0f, 200.0f, 200.0f),
    )

    private var points = arrayOf(
        arrayOf(0.0f, -100.0f, 200.0f),
        arrayOf(0.0f, 100.0f, 200.0f),

        arrayOf(200.0f, -100.0f, -35.0f),
        arrayOf(200.0f, 100.0f, -35.0f),
        arrayOf(200.0f, -100.0f, 35.0f),
        arrayOf(200.0f, 100.0f, 35.0f),

        arrayOf(-35.0f, -100.0f, -200.0f),
        arrayOf(-35.0f, 100.0f, -200.0f),
        arrayOf(0.0f, -100.0f, -200.0f),
        arrayOf(0.0f, 100.0f, -200.0f),
        arrayOf(35.0f, -100.0f, -200.0f),
        arrayOf(35.0f, 100.0f, -200.0f),

        arrayOf(-200.0f, 100.0f, 50.0f),
        arrayOf(-200.0f, -100.0f, 50.0f),
        arrayOf(-200.0f, -100.0f, 30.0f),
        arrayOf(-200.0f, 100.0f, 0.0f),
        arrayOf(-200.0f, 100.0f, 0.0f),
        arrayOf(-200.0f, -100.0f, -30.0f),

        arrayOf(-35.0f, -200.0f, 100.0f),
        arrayOf(0.0f, -200.0f, -100.0f),
        arrayOf(0.0f, -200.0f, -100.0f),
        arrayOf(35.0f, -200.0f, 100.0f),

        arrayOf(-35.0f, 200.0f, 100.0f),
        arrayOf(-0.0f, 200.0f, -100.0f),
        arrayOf(-0.0f, 200.0f, -100.0f),
        arrayOf(35.0f, 200.0f, 100.0f),
        arrayOf(-50.0f, 200.0f, -100.0f),
        arrayOf(-50.0f, 200.0f, 100.0f),

        )

    private var edgesPoints = arrayOf(
        arrayOf(0, 1),
        arrayOf(2, 3),
        arrayOf(4, 5),
        arrayOf(6, 7),
        arrayOf(8, 9),
        arrayOf(10, 11),
        arrayOf(12, 13),
        arrayOf(14, 15),
        arrayOf(16, 17),
        arrayOf(18, 19),
        arrayOf(20, 21),
        arrayOf(22, 23),
        arrayOf(24, 25),
        arrayOf(26, 27)
    )
    private var edges = arrayOf(
        arrayOf(0, 1),
        arrayOf(1, 3),
        arrayOf(3, 2),
        arrayOf(2, 0),
        arrayOf(4, 5),
        arrayOf(5, 7),
        arrayOf(7, 6),
        arrayOf(6, 4),
        arrayOf(0, 4),
        arrayOf(1, 5),
        arrayOf(2, 6),
        arrayOf(3, 7),
    )

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        updateCanvas(canvas)
    }

    private fun updateCanvas(canvas: Canvas) {

        //центрирование
        canvas.translate((canvas.width / 2).toFloat(), (canvas.height / 2).toFloat())

        // Draw edges
        for (e in edges.indices) {
            val n0 = edges[e][0]
            val n1 = edges[e][1]
            val node0 = nodes[n0]
            val node1 = nodes[n1]
            canvas.drawLine(
                node0[0], node0[1],
                node1[0], node1[1], paint
            )
        }

        // Draw numbers
        for (e in edgesPoints.indices) {
            val n0 = edgesPoints[e][0]
            val n1 = edgesPoints[e][1]
            val node0 = points[n0]
            val node1 = points[n1]
            canvas.drawLine(
                node0[0], node0[1],
                node1[0], node1[1], paint
            )
        }
    }

    // Rotate shape around the z-axis
    fun rotateZ3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (element in nodes) {
            val x = element[0]
            val y = element[1]
            element[0] = (x * cosTheta - y * sinTheta)
            element[1] = (y * cosTheta + x * sinTheta)
        }
        for (element in points) {
            val x = element[0]
            val y = element[1]
            element[0] = (x * cosTheta - y * sinTheta)
            element[1] = (y * cosTheta + x * sinTheta)
        }
        invalidate()
    }

    // Rotate shape around the x-axis
    private fun rotateX3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (element in nodes) {
            val y = element[1]
            val z = element[2]
            element[1] = (y * cosTheta - z * sinTheta)
            element[2] = (z * cosTheta + y * sinTheta)
        }
        for (element in points) {
            val y = element[1]
            val z = element[2]
            element[1] = (y * cosTheta - z * sinTheta)
            element[2] = (z * cosTheta + y * sinTheta)
        }
    }

    // Rotate shape around the y-axis
    private fun rotateY3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (element in nodes) {
            val x = element[0]
            val z = element[2]
            element[0] = (x * cosTheta + z * sinTheta)
            element[2] = (z * cosTheta - x * sinTheta)
        }
        for (element in points) {
            val x = element[0]
            val z = element[2]
            element[0] = (x * cosTheta + z * sinTheta)
            element[2] = (z * cosTheta - x * sinTheta)
        }
    }


    private var old = object {
        var x = 0.0f
        var y = 0.0f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                old.x = event.x
                old.y = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                rotateX3D((old.y - event.y) / 2500)
                rotateY3D((event.x - old.x) / 2500)
            }

        }
        invalidate()
        return true
    }

}
