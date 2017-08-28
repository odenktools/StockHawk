package com.udacity.stockhawk.sync;


import android.content.Intent;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.ui.StockWidgetFactory;

/**
 * Created by Dmitry Malkovich on 11.05.16.
 * </p>
 * The service to be connected to for a remote adapter to request RemoteViews for StockWidget.
 * Modified and Updated By : Rahyan Ramadhani on 28.08.2017
 */
public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        //QuoteIntentService.startActionWaterPlants(this);
        QuoteSyncJob.syncImmediately(this);
        return new StockWidgetFactory(getApplicationContext(), intent);
    }
}
