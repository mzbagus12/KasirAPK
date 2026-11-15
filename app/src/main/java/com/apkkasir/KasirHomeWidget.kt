package com.apkkasir

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class KasirHomeWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        val openApp = Intent.makeMainActivity(
            android.content.ComponentName(context, MainActivity::class.java),
        )
        val action = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_IMMUTABLE)
        val views = RemoteViews(context.packageName, R.layout.kasir_home_widget)
        views.setOnClickPendingIntent(R.id.kasir_widget_open, action)
        manager.updateAppWidget(widgetIds, views)
    }
}
