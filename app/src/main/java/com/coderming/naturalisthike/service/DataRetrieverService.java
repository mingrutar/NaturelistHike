package com.coderming.naturalisthike.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.coderming.naturalisthike.data_retriever.PlantDataFetcher;
import com.coderming.naturalisthike.data_retriever.TripDataFetcher;
import com.coderming.naturalisthike.utils.Constants;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DataRetrieverService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FETCH_TRIP = "com.coderming.naturalisthike.service.action.fetchtrip";
    private static final String ACTION_FETCH_PLANTS = "com.coderming.naturalisthike.service.action.fetchplants";

    public DataRetrieverService() {
        super("DataRetrieverService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startFetchTripData(Context context) {
        Intent intent = new Intent(context, DataRetrieverService.class);
        intent.setAction(ACTION_FETCH_TRIP);
        context.startService(intent);
    }
    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startFetchPlantData(Context context, double lan, double lon) {
        Intent intent = new Intent(context, DataRetrieverService.class);

        intent.setAction(ACTION_FETCH_PLANTS);
        intent.putExtra(Constants.TAG_TRAILHEAD_LAT, lan);
        intent.putExtra(Constants.TAG_TRAILHEAD_LON, lon);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_TRIP.equals(action)) {
                TripDataFetcher.instance().fetchData(getApplicationContext());
            } else if (ACTION_FETCH_PLANTS.equals(action)) {
                double lan = intent.getDoubleExtra(Constants.TAG_TRAILHEAD_LAT, -1.0);
                double lon = intent.getDoubleExtra(Constants.TAG_TRAILHEAD_LON, -1.0);
                PlantDataFetcher.instance().fetchData(getApplicationContext(), lan, lon);
            }
        }
    }
}
