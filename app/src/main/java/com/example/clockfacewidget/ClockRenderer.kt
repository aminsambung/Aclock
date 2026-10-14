package com.example.clockfacewidget

import android.graphics.*
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

object ClockRenderer {

    fun render(
        size: Int,
        type: ClockType,
        analogTheme: AnalogTheme,
        digitalTheme: DigitalTheme,
        showSeconds: Boolean,
        calendar: Calendar = Calendar.getInstance()
    ): Bitmap {
        return if (type == ClockType.ANALOG) {
            renderAnalog(size, analogTheme, showSeconds, calendar)
        } else {
            // Jika tipe digital, cek apakah temanya LCD_RETRO
            if (digitalTheme == DigitalTheme.LCD_RETRO) {
                renderLcdRetro(size, calendar)
            } else {
                renderDigital(size, digitalTheme, calendar)
            }
        }
    }

    // ==================== JAM ANALOG (Sama seperti sebelumnya) ====================
    private fun renderAnalog(
        size: Int,
        theme: AnalogTheme,
        showSeconds: Boolean,
        calendar: Calendar
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f
        val radius = size * 0.38f

        val mainColor = when (theme) {
            AnalogTheme.BLACK, AnalogTheme.SPACE -> Color.WHITE
            AnalogTheme.WHITE, AnalogTheme.ROMAN -> Color.BLACK
        }

        if (theme == AnalogTheme.SPACE) {
            drawSpaceBackground(canvas)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.argb(180, mainColor.red(), mainColor.green(), mainColor.blue())
        paint.strokeWidth = size * 0.012f
        canvas.drawCircle(cx, cy, radius, paint)

        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        if (theme == AnalogTheme.ROMAN) {
            drawRomanNumbers(canvas, cx, cy, radius * 0.82f, mainColor, size)
        } else if (theme != AnalogTheme.SPACE) {
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

        drawDate(canvas, size, cy + radius * 1.35f, mainColor, calendar)
        return bitmap
    }

    // ==================== JAM DIGITAL BIASA ====================
    private fun renderDigital(size: Int, theme: DigitalTheme, calendar: Calendar): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val mainColor = when (theme) {
            DigitalTheme.BIG_WHITE -> Color.WHITE
            DigitalTheme.BIG_BLACK -> Color.BLACK
            else -> Color.WHITE
        }

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timePaint.color = mainColor
        timePaint.textAlign = Paint.Align.CENTER
        timePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeText = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        timePaint.textSize = size * 0.32f
        val textBounds = Rect()
        timePaint.getTextBounds(timeText, 0, timeText.length, textBounds)
        val textY = (size / 2f) - (textBounds.exactCenterY())
        
        canvas.drawText(timeText, size / 2f, textY, timePaint)
        drawDate(canvas, size, size / 2f + textBounds.height() * 1.5f, mainColor, calendar)
        return bitmap
    }

    // ==================== JAM LCD RETRO (BARU) ====================
    private fun renderLcdRetro(size: Int, calendar: Calendar): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f

        // 1. Gambar Frame Luar (Casing Jam) - Warna Gelap
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        framePaint.color = Color.rgb(40, 40, 40) // Dark Grey
        framePaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.48f, framePaint)

        // 2. Gambar Layar LCD - Warna Terang
        val screenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        screenPaint.color = Color.rgb(200, 210, 205) // Light Grey/Greenish
        screenPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.42f, screenPaint)

        // 3. Gambar Teks Jam Digital (Gaya LCD)
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timePaint.color = Color.rgb(50, 60, 60) // Dark Grey text
        timePaint.textAlign = Paint.Align.CENTER
        // Gunakan font Monospace agar terlihat seperti LCD
        timePaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeText = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        timePaint.textSize = size * 0.28f
        val timeY = cy - size * 0.05f
        canvas.drawText(timeText, cx, timeY, timePaint)

        // 4. Gambar Garis Pemisah
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linePaint.color = Color.rgb(50, 60, 60)
        linePaint.strokeWidth = size * 0.005f
        val lineY = cy + size * 0.10f
        canvas.drawLine(cx - size * 0.30f, lineY, cx + size * 0.30f, lineY, linePaint)

        // 5. Gambar Hari dan Tanggal (Contoh: MON | JAN)
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.color = Color.rgb(50, 60, 60)
        datePaint.textAlign = Paint.Align.CENTER
        datePaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        datePaint.textSize = size * 0.08f

        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)
        
        // Gambar Hari di kiri dan Bulan di kanan
        val dateY = lineY + size * 0.12f
        canvas.drawText(dayOfWeek?.uppercase() ?: "", cx - size * 0.15f, dateY, datePaint)
        
        // Gambar garis pemisah vertikal kecil
        canvas.drawLine(cx, lineY + size*0.02f, cx, lineY + size*0.10f, linePaint)
        
        canvas.drawText(month?.uppercase() ?: "", cx + size * 0.15f, dateY, datePaint)

        return bitmap
    }

    // ==================== FUNGSI BANTU ====================
    private fun drawDate(canvas: Canvas, size: Int, y: Float, color: Int, calendar: Calendar) {
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.color = color
        datePaint.textSize = size * 0.075f
        datePaint.textAlign = Paint.Align.CENTER
        datePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale("id", "ID"))
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale("id", "ID"))
        val year = calendar.get(Calendar.YEAR)

        val dateText = "${dayOfWeek?.uppercase()}, $dayOfMonth ${month?.uppercase()} $year"
        canvas.drawText(dateText, size / 2f, y, datePaint)
    }

    private fun drawSpaceBackground(canvas: Canvas) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.WHITE
        val random = java.util.Random(42)
        repeat(70) {
            val x = random.nextFloat() * canvas.width
            val y = random.nextFloat() * canvas.height
            val s = 0.7f + random.nextFloat() * 2.0f
            canvas.drawCircle(x, y, s, p)
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
            canvas.drawLine(cx + cos(a).toFloat() * inner, cy + sin(a).toFloat() * inner, cx + cos(a).toFloat() * outer, cy + sin(a).toFloat() * outer, p)
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

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, length: Float, angle: Float, color: Int, width: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.strokeWidth = width
        p.strokeCap = Paint.Cap.ROUND
        val a = Math.toRadians(angle.toDouble())
        canvas.drawLine(cx, cy, cx + cos(a).toFloat() * length, cy + sin(a).toFloat() * length, p)
    }

    private fun Int.red() = Color.red(this)
    private fun Int.green() = Color.green(this)
    private fun Int.blue() = Color.blue(this)
}
