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
        alarmEnabled: Boolean,
        alarmHour: Int,
        alarmMinute: Int,
        calendar: Calendar = Calendar.getInstance()
    ): Bitmap {
        return if (type == ClockType.ANALOG) {
            renderAnalog(size, analogTheme, showSeconds, alarmEnabled, alarmHour, alarmMinute, calendar)
        } else {
            when (digitalTheme) {
                DigitalTheme.LCD_RETRO -> renderLcdRetro(size, alarmEnabled, alarmHour, alarmMinute, calendar)
                DigitalTheme.SPORTY_DIGITAL -> renderSportyDigital(size, alarmEnabled, alarmHour, alarmMinute, calendar)
                else -> renderDigital(size, digitalTheme, alarmEnabled, alarmHour, alarmMinute, calendar)
            }
        }
    }

    // ==================== JAM ANALOG ====================
    private fun renderAnalog(
        size: Int,
        theme: AnalogTheme,
        showSeconds: Boolean,
        alarmEnabled: Boolean,
        alarmHour: Int,
        alarmMinute: Int,
        calendar: Calendar
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f
        val radius = size * 0.32f

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

        drawHand(canvas, cx, cy, radius * 0.52f, hourAngle, mainColor, size * 0.030f)
        drawHand(canvas, cx, cy, radius * 0.73f, minuteAngle, mainColor, size * 0.020f)
        if (showSeconds) {
            drawHand(canvas, cx, cy, radius * 0.78f, secondAngle, Color.rgb(230, 70, 70), size * 0.010f)
        }

        paint.style = Paint.Style.FILL
        paint.color = mainColor
        canvas.drawCircle(cx, cy, size * 0.030f, paint)

        if (alarmEnabled) {
            drawAlarm(canvas, size, cx, cy - radius * 1.4f, mainColor, alarmHour, alarmMinute)
        }

        drawDate(canvas, size, cy + radius * 1.5f, mainColor, calendar)
        return bitmap
    }

    // ==================== JAM DIGITAL BIASA ====================
    private fun renderDigital(
        size: Int,
        theme: DigitalTheme,
        alarmEnabled: Boolean,
        alarmHour: Int,
        alarmMinute: Int,
        calendar: Calendar
    ): Bitmap {
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

        timePaint.textSize = size * 0.28f
        val textBounds = Rect()
        timePaint.getTextBounds(timeText, 0, timeText.length, textBounds)
        val textY = (size / 2f) - (textBounds.exactCenterY())
        
        canvas.drawText(timeText, size / 2f, textY, timePaint)

        if (alarmEnabled) {
            drawAlarm(canvas, size, size / 2f, textY - textBounds.height(), mainColor, alarmHour, alarmMinute)
        }

        drawDate(canvas, size, size / 2f + textBounds.height() * 1.5f, mainColor, calendar)
        return bitmap
    }

    // ==================== JAM LCD RETRO ====================
    private fun renderLcdRetro(
        size: Int,
        alarmEnabled: Boolean,
        alarmHour: Int,
        alarmMinute: Int,
        calendar: Calendar
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        framePaint.color = Color.rgb(40, 40, 40)
        framePaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.42f, framePaint)

        val screenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        screenPaint.color = Color.rgb(200, 210, 205)
        screenPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.36f, screenPaint)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timePaint.color = Color.rgb(50, 60, 60)
        timePaint.textAlign = Paint.Align.CENTER
        timePaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeText = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        timePaint.textSize = size * 0.22f
        val timeY = cy - size * 0.05f
        canvas.drawText(timeText, cx, timeY, timePaint)

        if (alarmEnabled) {
            drawAlarm(canvas, size, cx, cy - size * 0.22f, Color.rgb(50, 60, 60), alarmHour, alarmMinute)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linePaint.color = Color.rgb(50, 60, 60)
        linePaint.strokeWidth = size * 0.005f
        val lineY = cy + size * 0.08f
        canvas.drawLine(cx - size * 0.25f, lineY, cx + size * 0.25f, lineY, linePaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.color = Color.rgb(50, 60, 60)
        datePaint.textAlign = Paint.Align.CENTER
        datePaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        datePaint.textSize = size * 0.07f

        val localeID = Locale("id", "ID")
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, localeID)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, localeID)
        
        val dateTextLeft = dayOfWeek?.uppercase() ?: ""
        val dateTextRight = "$dayOfMonth ${month?.uppercase()}"
        
        val dateY = lineY + size * 0.10f
        canvas.drawText(dateTextLeft, cx - size * 0.15f, dateY, datePaint)
        canvas.drawLine(cx, lineY + size*0.02f, cx, lineY + size*0.08f, linePaint)
        canvas.drawText(dateTextRight, cx + size * 0.15f, dateY, datePaint)

        return bitmap
    }

    // ==================== JAM SPORTY DIGITAL (BARU) ====================
    private fun renderSportyDigital(
        size: Int,
        alarmEnabled: Boolean,
        alarmHour: Int,
        alarmMinute: Int,
        calendar: Calendar
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f
        val radius = size * 0.46f

        // 1. Background Gelap (Lingkaran Hitam)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.color = Color.rgb(20, 20, 20)
        bgPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // 2. Bezel Ticks (Garis-garis penanda seperti smartwatch)
        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tickPaint.color = Color.rgb(80, 80, 80)
        tickPaint.strokeWidth = size * 0.008f
        tickPaint.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90.0)
            val inner = radius * 0.85f
            val outer = if (i % 5 == 0) radius * 0.95f else radius * 0.90f
            tickPaint.color = if (i % 5 == 0) Color.WHITE else Color.rgb(80, 80, 80)
            canvas.drawLine(
                cx + cos(a).toFloat() * inner, cy + sin(a).toFloat() * inner,
                cx + cos(a).toFloat() * outer, cy + sin(a).toFloat() * outer,
                tickPaint
            )
        }

        // 3. Teks Jam Digital (Dua Warna)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val hourText = String.format(Locale.getDefault(), "%02d", hour)
        val minuteText = String.format(Locale.getDefault(), "%02d", minute)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timePaint.textAlign = Paint.Align.CENTER
        timePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        timePaint.textSize = size * 0.22f

        // Gambar Jam (Putih)
        timePaint.color = Color.WHITE
        canvas.drawText(hourText, cx, cy - size * 0.02f, timePaint)
        
        // Gambar Menit (Cyan/Teal)
        timePaint.color = Color.rgb(0, 200, 200)
        canvas.drawText(minuteText, cx, cy + size * 0.15f, timePaint)

        // Gambar Titik Dua (:)
        timePaint.color = Color.WHITE
        timePaint.textSize = size * 0.15f
        canvas.drawText(":", cx, cy + size * 0.05f, timePaint)

        // 4. Hari dan Tanggal di Bawah
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.color = Color.LTGRAY
        datePaint.textAlign = Paint.Align.CENTER
        datePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        datePaint.textSize = size * 0.06f

        val localeID = Locale("id", "ID")
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, localeID)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, localeID)
        val dateText = "${dayOfWeek?.uppercase()}, $dayOfMonth ${month?.uppercase()}"
        canvas.drawText(dateText, cx, cy + size * 0.25f, datePaint)

        // 5. Alarm
        if (alarmEnabled) {
            drawAlarm(canvas, size, cx, cy - size * 0.25f, Color.rgb(255, 100, 100), alarmHour, alarmMinute)
        }

        return bitmap
    }

    // ==================== FUNGSI BANTU ====================
    private fun drawDate(canvas: Canvas, size: Int, y: Float, color: Int, calendar: Calendar) {
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.color = color
        datePaint.textSize = size * 0.07f
        datePaint.textAlign = Paint.Align.CENTER
        datePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val localeID = Locale("id", "ID")
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, localeID)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, localeID)
        val year = calendar.get(Calendar.YEAR)

        val dateText = "${dayOfWeek?.uppercase()}, $dayOfMonth ${month?.uppercase()} $year"
        canvas.drawText(dateText, size / 2f, y, datePaint)
    }

    private fun drawAlarm(canvas: Canvas, size: Int, x: Float, y: Float, color: Int, hour: Int, minute: Int) {
        val alarmPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        alarmPaint.color = color
        alarmPaint.textAlign = Paint.Align.CENTER
        alarmPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        alarmPaint.textSize = size * 0.06f
        
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val alarmText = String.format(Locale.getDefault(), "⏰ %02d:%02d %s", displayHour, minute, amPm)
        
        canvas.drawText(alarmText, x, y, alarmPaint)
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
        p.textSize = size * 0.090f
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
        p.textSize = size * 0.065f
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
