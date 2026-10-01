package com.example.clockfacewidget

import android.graphics.*
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object ClockRenderer {

    fun render(
        size: Int,
        face: ClockFace,
        showSeconds: Boolean,
        calendar: Calendar = Calendar.getInstance()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f
        val radius = size * 0.44f

        drawBackground(canvas, cx, cy, radius, face)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        val mainColor = when (face) {
            ClockFace.BLACK, ClockFace.SPACE -> Color.WHITE
            ClockFace.WHITE, ClockFace.ROMAN -> Color.BLACK
        }

        paint.color = Color.argb(180, mainColor.red(), mainColor.green(), mainColor.blue())
        paint.strokeWidth = size * 0.012f
        canvas.drawCircle(cx, cy, radius, paint)

        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        if (face == ClockFace.ROMAN) {
            drawRomanNumbers(canvas, cx, cy, radius * 0.82f, mainColor, size)
        } else if (face != ClockFace.SPACE) {
            drawArabicNumbers(canvas, cx, cy, radius * 0.82f, mainColor, size)
        } else {
            drawMarks(canvas, cx, cy, radius, mainColor, size)
        }

        val hourAngle = (hour + minute / 60f) * 30f - 90f
        val minuteAngle = (minute + second / 60f) * 6f - 90f
        val secondAngle = second * 6f - 90f

        drawHand(canvas, cx, cy, radius * 0.52f, hourAngle, mainColor, size * 0.035f)
        drawHand(canvas, cx, cy, radius * 0.73f, minuteAngle, mainColor, size * 0.022f)

        if (showSeconds) {
            drawHand(canvas, cx, cy, radius * 0.78f, secondAngle, Color.rgb(230, 70, 70), size * 0.010f)
        }

        paint.style = Paint.Style.FILL
        paint.color = mainColor
        canvas.drawCircle(cx, cy, size * 0.035f, paint)

        return bitmap
    }

    private fun drawBackground(canvas: Canvas, cx: Float, cy: Float, r: Float, face: ClockFace) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.FILL
        when (face) {
            ClockFace.BLACK -> {
                p.color = Color.BLACK
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), p)
            }
            ClockFace.WHITE, ClockFace.ROMAN -> {
                p.color = Color.WHITE
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), p)
            }
            ClockFace.SPACE -> {
                val shader = RadialGradient(
                    cx, cy, r * 1.5f,
                    intArrayOf(Color.rgb(8, 32, 70), Color.rgb(2, 8, 24), Color.BLACK),
                    null, Shader.TileMode.CLAMP
                )
                p.shader = shader
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), p)
                p.shader = null
                p.color = Color.WHITE
                val random = java.util.Random(42)
                repeat(70) {
                    val x = random.nextFloat() * canvas.width
                    val y = random.nextFloat() * canvas.height
                    val s = 0.7f + random.nextFloat() * 2.0f
                    canvas.drawCircle(x, y, s, p)
                }
            }
        }
    }

    private fun drawMarks(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int, size: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.strokeWidth = size * 0.012f
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90.0)
            val outer = r * 0.92f
            val inner = if (i % 5 == 0) r * 0.83f else r * 0.87f
            canvas.drawLine(
                cx + cos(a).toFloat() * inner,
                cy + sin(a).toFloat() * inner,
                cx + cos(a).toFloat() * outer,
                cy + sin(a).toFloat() * outer,
                p
            )
        }
    }

    private fun drawArabicNumbers(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int, size: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.textSize = size * 0.105f
        p.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        p.textAlign = Paint.Align.CENTER
        for (n in 1..12) {
            val a = Math.toRadians(n * 30.0 - 90.0)
            val x = cx + cos(a).toFloat() * r
            val y = cy + sin(a).toFloat() * r - (p.ascent() + p.descent()) / 2f
            canvas.drawText(n.toString(), x, y, p)
        }
    }

    private fun drawRomanNumbers(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int, size: Int) {
        val nums = arrayOf("XII","I","II","III","IV","V","VI","VII","VIII","IX","X","XI")
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.textSize = size * 0.075f
        p.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        p.textAlign = Paint.Align.CENTER
        for (i in nums.indices) {
            val a = Math.toRadians((i + 1) * 30.0 - 90.0)
            val x = cx + cos(a).toFloat() * r
            val y = cy + sin(a).toFloat() * r - (p.ascent() + p.descent()) / 2f
            canvas.drawText(nums[i], x, y, p)
        }
    }

    private fun drawHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        length: Float,
        angle: Float,
        color: Int,
        width: Float
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.strokeWidth = width
        p.strokeCap = Paint.Cap.ROUND
        val a = Math.toRadians(angle.toDouble())
        canvas.drawLine(
            cx, cy,
            cx + cos(a).toFloat() * length,
            cy + sin(a).toFloat() * length,
            p
        )
    }

    private fun Int.red() = Color.red(this)
    private fun Int.green() = Color.green(this)
    private fun Int.blue() = Color.blue(this)
}
