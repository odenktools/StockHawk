package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

import static com.udacity.stockhawk.sync.QuoteSyncJob.ACTION_DATA_UPDATED;


public class QuoteIntentService extends IntentService {

    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());
    }

    /**
     * Starts this service to perform WaterPlants action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlants(Context context) {
        Intent intent = new Intent(context, QuoteSyncJob.class);
        intent.setAction(ACTION_DATA_UPDATED);
        context.startService(intent);
    }
}
