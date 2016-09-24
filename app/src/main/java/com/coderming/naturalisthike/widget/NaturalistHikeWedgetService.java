package com.coderming.naturalisthike.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.data.DataConstants;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.ui.MainActivity;
import com.coderming.naturalisthike.utils.Utility;

import java.util.Calendar;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NaturalistHikeWedgetService extends IntentService implements DataConstants {
    private static final String LOG_TAG = NaturalistHikeWedgetService.class.getSimpleName();

    private static final String[] sQueryProjection = new String[] {TripContract.TripEntry.COLUMN_NAME,
            TripContract.TripEntry.COLUMN_HIKE_DATE };
    private static int COL_HIKE_NAME = 0;
    private static int COL_HIKE_DATE = 1;

    public NaturalistHikeWedgetService() {
        super("NaturalistHikeWedgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, NaturalistHikeWidgetProvider.class));
        Calendar now = Calendar.getInstance();
        Cursor cursor = getContentResolver().query(TripContract.TripEntry.CONTENT_URI, sQueryProjection,
                TripContract.TripEntry.COLUMN_HIKE_DATE + QueryGE, new String[]{Long.toString(now.getTimeInMillis())},
                null, null);
        if (cursor == null) {
            Log.w(LOG_TAG, String.format("+-+- #appWidgetId=%d, #date=null", appWidgetIds.length));
            return;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            Log.i(LOG_TAG, String.format("+-+- #appWidgetId=%d, #date=0", appWidgetIds.length));
            return;
        }
        Log.v(LOG_TAG, String.format("+-+- #appWidgetId=%d, #date=%d", appWidgetIds.length, cursor.getCount()));
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            int layoutId;
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.naturalist_hike_widget);

            String hikeName = cursor.getString(COL_HIKE_NAME);
            views.setTextViewText(R.id.widget_name_text, hikeName);
            long date = cursor.getLong(COL_HIKE_DATE);
            String dateStr = Utility.formatDateOnly(date);
            Log.v(LOG_TAG, String.format("+-+- appWidgetId=%d, hikeNmae=%s, date=%s", appWidgetId, hikeName, dateStr));
            views.setTextViewText(R.id.widget_date, dateStr);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            launchIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.nh_widget, pendingIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        cursor.close();
    }
}
