/*
 * Copyright 2016.  Dmitry Malkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Dmitry Malkovich on 11.05.16.
 * Used to populate the Stock Widget's remote ListView.
 * Modified and Updated By : Rahyan Ramadhani on 28.08.2017
 */
public class StockWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;
    private Context mContext;
    int mWidgetId;

    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    public StockWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);


        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

    }

    @Override
    public void onCreate() {
        // Nothing to do
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_list_content);
        if (mCursor.moveToPosition(position)) {
            rv.setTextViewText(R.id.stock_symbol,
                    mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
            rv.setTextViewText(R.id.bid_price,
                    mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_PRICE)));

            float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (rawAbsoluteChange < 0) {
                rv.setTextColor(R.id.stock_change, ContextCompat.getColor(mContext, R.color.material_red_700));
            }

            if (PrefUtils.getDisplayMode(mContext)
                    .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                rv.setTextViewText(R.id.stock_change, change);

            } else {
                rv.setTextViewText(R.id.stock_change, percentage);
            }
        }
        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

}
