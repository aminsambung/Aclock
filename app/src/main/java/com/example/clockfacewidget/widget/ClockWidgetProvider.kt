package com.example.clockfacewidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.clockfacewidget.*
import java.util.Calendar

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedule(context)
    }

    override fun onDisabled(context: Context) {
        cancel(context)
        super.onDisabled(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        update(context, appWidgetManager, appWidgetIds)
        schedule(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_CLOCK) {
            updateAll(context)
            schedule(context)
        }
    }

    companion object {
        const val ACTION_UPDATE_CLOCK = "com.example.clockfacewidget.UPDATE_CLOCK"
        private const val REQUEST_CODE = 2211

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClockWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) update(context, manager, ids)
        }

        private fun update(
            context: Context,
            manager: AppWidgetManager,
            ids: IntArray
        ) {
            val type = ClockSettings.type(context)
            val analogTheme = ClockSettings.analogTheme(context)
            val digitalTheme = ClockSettings.digitalTheme(context)
            val seconds = ClockSettings.seconds(context)
            // PERBAIKAN: Baca pengaturan alarm
            val alarmEnabled = ClockSettings.isAlarmEnabled(context)
            val alarmHour = ClockSettings.alarmHour(context)
            val alarmMinute = ClockSettings.alarmMinute(context)
            val now = Calendar.getInstance()

            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_clock)
                val size = 800
                // PERBAIKAN: Kirim parameter alarm ke ClockRenderer
                val bitmap = ClockRenderer.render(
                    context = context, 
                    size = 700,
                    type = type,
                    analogTheme = analogTheme,
                    digitalTheme = digitalTheme,
                    showSeconds = seconds,
                    alarmEnabled = alarmEnabled,
                    alarmHour = alarmHour,
                    alarmMinute = alarmMinute,
                    calendar = now
                )
                views.setImageViewBitmap(R.id.clockImage, bitmap)

                val launch = PendingIntent.getActivity(
                    context,
                    id,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widgetRoot, launch)
                manager.updateAppWidget(id, views)
            }
        }

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_CLOCK
            }
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val now = System.currentTimeMillis()
            val nextMinute = now - (now % 60_000L) + 60_000L

            alarmManager.setRepeating(
                AlarmManager.RTC,
                nextMinute,
                60_000L,
                pending
            )
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_CLOCK
            }
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
        }
    }
}
