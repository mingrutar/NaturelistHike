package com.coderming.naturalisthike.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Implementation of App Widget functionality.
 */
public class NaturalistHikeWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = NaturalistHikeWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(LOG_TAG, String.format("+-+- onUpdate, #appWidgetIds=", appWidgetIds.length));
        // due to 5 sec proocessing time, we process it in async way
        context.startService(new Intent(context, NaturalistHikeWedgetService.class));
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Log.v(LOG_TAG, String.format("+-+- onAppWidgetOptionsChanged,appWidgetId=%d", appWidgetId));
        context.startService(new Intent(context, NaturalistHikeWedgetService.class));
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "+-+- onReceive,actionId=" + intent.getAction());
        super.onReceive(context, intent);
    }
}

