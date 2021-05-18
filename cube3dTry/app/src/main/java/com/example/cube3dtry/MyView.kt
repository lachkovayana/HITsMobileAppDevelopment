package com.example.cube3dtry

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MyView : View {

    private var backColor = Color.WHITE

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

    private var paints = arrayOf(
        arrayOf(0.0f, -100.0f, -200.0f),
        arrayOf(0.0f, 100.0f, -200.0f),

        arrayOf(200.0f, -100.0f, -35.0f),
        arrayOf(200.0f, 100.0f, -35.0f),
        arrayOf(200.0f, -100.0f, 35.0f),
        arrayOf(200.0f, 100.0f, 35.0f),

        arrayOf(-35.0f, -100.0f, 200.0f),
        arrayOf(-35.0f, 100.0f, 200.0f),
        arrayOf(0.0f, -100.0f, 200.0f),
        arrayOf(0.0f, 100.0f, 200.0f),
        arrayOf(35.0f, -100.0f, 200.0f),
        arrayOf(35.0f, 100.0f, 200.0f),

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

    private var edgesPaints = arrayOf(
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
        canvas.drawColor(backColor)
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

        // Draw nodes
        for (element in nodes) {
            canvas.drawPoint(element[0], element[1], paint)
        }

        //Draw numbers
        for (e in edgesPaints.indices) {
            val n0 = edgesPaints[e][0]
            val n1 = edgesPaints[e][1]
            val node0 = paints[n0]
            val node1 = paints[n1]
            canvas.drawLine(
                node0[0], node0[1],
                node1[0], node1[1], paint
            )
        }
    }

    // Rotate shape around the z-axis
    private fun rotateZ3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (element in nodes) {
            val x = element[0]
            val y = element[1]
            element[0] = (x * cosTheta - y * sinTheta)
            element[1] = (y * cosTheta + x * sinTheta)
        }
    }

    private fun rotateX3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (element in nodes) {
            val y = element[1]
            val z = element[2]
            element[1] = (y * cosTheta - z * sinTheta)
            element[2] = (z * cosTheta + y * sinTheta)
        }
        for (element in paints) {
            val y = element[1]
            val z = element[2]
            element[1] = (y * cosTheta - z * sinTheta)
            element[2] = (z * cosTheta + y * sinTheta)
        }
    }

    private fun rotateY3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (element in nodes) {
            val x = element[0]
            val z = element[2]
            element[0] = (x * cosTheta + z * sinTheta)
            element[2] = (z * cosTheta - x * sinTheta)
        }
        for (element in paints) {
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
                rotateX3D((old.y - event.y) / 3000)
                rotateY3D((event.x - old.x) / 3000)
            }
        }
        invalidate()
        return true
    }


//    private var path = Path()
//    private lateinit var extraCanvas: Canvas
//    private var motionTouchEventX = 0f
//    private var motionTouchEventY = 0f
//
//    private var currentX = 0f
//    private var currentY = 0f
//
//    private fun touchStart() {
//        path.reset()
//        path.moveTo(motionTouchEventX, motionTouchEventY)
//        currentX = motionTouchEventX
//        currentY = motionTouchEventY
//    }
//
//    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
//
//    private fun touchMove() {
//        val dx = abs(motionTouchEventX - currentX)
//        val dy = abs(motionTouchEventY - currentY)
//        if (dx >= touchTolerance || dy >= touchTolerance) {
//            // QuadTo() adds a quadratic bezier from the last point,
//            // approaching control point (x1,y1), and ending at (x2,y2).
//            path.quadTo(
//                currentX,
//                currentY,
//                (motionTouchEventX + currentX) / 2,
//                (motionTouchEventY + currentY) / 2
//            )
//            currentX = motionTouchEventX
//            currentY = motionTouchEventY
//            // Draw the path in the extra bitmap to cache it.
//            extraCanvas.drawPath(path, paint)
//        }
//        invalidate()
//    }
//
//    private fun touchUp() {
//        // Reset the path so it doesn't get drawn again.
//        path.reset()
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        motionTouchEventX = event.x
//        motionTouchEventY = event.y
//
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> touchStart()
//            MotionEvent.ACTION_MOVE -> touchMove()
//            MotionEvent.ACTION_UP -> touchUp()
//        }
//        return true
//    }
}
