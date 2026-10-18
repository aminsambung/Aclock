package com.example.clockfacewidget

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.net.TrafficStats
import android.os.BatteryManager
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

object ClockRenderer {

    fun render(
        context: Context, // <-- TAMBAHAN: Context untuk baca sistem
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
        // Cek jika tema Aesthetic Card dipilih
        if (type == ClockType.ANALOG && analogTheme == AnalogTheme.AESTHETIC_CARD) {
            return renderAestheticCard(context, size, calendar)
        }

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

    // ==================== JAM AESTHETIC CARD ====================
    private fun renderAestheticCard(
        context: Context,
        size: Int,
        calendar: Calendar
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Kartu (Warna Merah)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.color = Color.rgb(210, 50, 50) // Warna merah ala SkyFlip
        bgPaint.style = Paint.Style.FILL
        val rect = RectF(size * 0.02f, size * 0.25f, size * 0.98f, size * 0.75f)
        canvas.drawRoundRect(rect, size * 0.1f, size * 0.1f, bgPaint)

        // 2. Lingkaran Jam di Kiri
        val cxClock = size * 0.25f
        val cyClock = size * 0.5f
        val radiusClock = size * 0.18f
        
        val clockBg = Paint(Paint.ANTI_ALIAS_FLAG)
        clockBg.color = Color.WHITE
        clockBg.style = Paint.Style.FILL
        canvas.drawCircle(cxClock, cyClock, radiusClock, clockBg)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = Color.rgb(180, 30, 30)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = size * 0.02f
        canvas.drawCircle(cxClock, cyClock, radiusClock, borderPaint)

        // Gambar jarum jam
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val hourAngle = (hour + minute / 60f) * 30f - 90f
        val minuteAngle = (minute + second / 60f) * 6f - 90f

        drawHand(canvas, cxClock, cyClock, radiusClock * 0.6f, hourAngle, Color.BLACK, size * 0.02f)
        drawHand(canvas, cxClock, cyClock, radiusClock * 0.8f, minuteAngle, Color.BLACK, size * 0.015f)
        
        val centerDot = Paint(Paint.ANTI_ALIAS_FLAG)
        centerDot.color = Color.RED
        canvas.drawCircle(cxClock, cyClock, size * 0.015f, centerDot)

        // 3. Teks Hari dan Tanggal di Kanan Atas
        val textX = size * 0.65f
        val localeID = Locale("id", "ID")
        
        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dayPaint.color = Color.WHITE
        dayPaint.textAlign = Paint.Align.CENTER
        dayPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        dayPaint.textSize = size * 0.06f
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, localeID)
        canvas.drawText(dayOfWeek?.capitalize() ?: "", textX, size * 0.35f, dayPaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.color = Color.WHITE
        datePaint.textAlign = Paint.Align.CENTER
        datePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        datePaint.textSize = size * 0.10f
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, localeID)
        val dateText = "$dayOfMonth ${month?.capitalize()}"
        canvas.drawText(dateText, textX, size * 0.46f, datePaint)

        // 4. Data Sistem (Baterai, RAM, Data Internet)
        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        infoPaint.color = Color.WHITE
        infoPaint.textAlign = Paint.Align.CENTER
        infoPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        infoPaint.textSize = size * 0.05f

        val batteryTemp = getBatteryTemp(context)
        val ramUsed = getRamUsed(context)
        val dataUsed = getDataUsed(context)

        // Posisi Y untuk 3 item
        val infoY = size * 0.60f
        val col1 = size * 0.40f
        val col2 = size * 0.65f
        val col3 = size * 0.90f

        canvas.drawText("🌡️ ${batteryTemp}°C", col1, infoY, infoPaint)
        canvas.drawText("🧠 ${ramUsed}", col2, infoY, infoPaint)
        canvas.drawText("📶 ${dataUsed}", col3, infoY, infoPaint)

        return bitmap
    }

    // ==================== FUNGSI BANTU SISTEM ====================
    private fun getBatteryTemp(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return String.format(Locale.getDefault(), "%.1f", temp / 10.0f)
    }

    private fun getRamUsed(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val usedMem = memoryInfo.totalMem - memoryInfo.availMem
        val usedGB = usedMem / (1024.0 * 1024.0 * 1024.0)
        return String.format(Locale.getDefault(), "%.1fG", usedGB)
    }

    private fun getDataUsed(context: Context): String {
        // Mengambil total data yang terpakai (Mobile + Wifi) sejak HP menyala
        val mobileRx = TrafficStats.getMobileRxBytes()
        val mobileTx = TrafficStats.getMobileTxBytes()
        val totalRx = TrafficStats.getTotalRxBytes()
        val totalTx = TrafficStats.getTotalTxBytes()
        
        val totalMobile = if (mobileRx == TrafficStats.UNSUPPORTED.toLong()) 0L else mobileRx + mobileTx
        val totalWifi = (totalRx + totalTx) - totalMobile
        
        val totalBytes = totalMobile + totalWifi
        val usedMB = totalBytes / (1024.0 * 1024.0)
        
        return if (usedMB > 1024) {
            String.format(Locale.getDefault(), "%.1fG", usedMB / 1024)
        } else {
            String.format(Locale.getDefault(), "%.0fM", usedMB)
        }
    }

    // ... (Fungsi renderAnalog, renderDigital, renderLcdRetro, renderSportyDigital, drawDate, drawAlarm, drawHand, dll TETAP SAMA seperti sebelumnya, tidak perlu diubah)
    
    // (Salin sisa fungsi pendukung dari kode sebelumnya ke sini)
    // ...
}
