package com.coderming.naturalisthike.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.coderming.naturalisthike.R;

import butterknife.BindView;

/**
 * Implementation of App Widget functionality.
 */
public class NaturalistHikeWidget extends AppWidgetProvider {
    private static final String LOG_TAG = NaturalistHikeWidget.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        context.startService(new Intent(context, NaturalistHikeWedgetService.class));

        CharSequence widgetNameText = context.getString(R.string.appwidget_text);
        CharSequence widgetNameText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.naturalist_hike_widget);
        views.setTextViewText(R.id.widget_name_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, NaturalistHikeWedgetService.class));

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

