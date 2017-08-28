package com.udacity.stockhawk.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import timber.log.Timber;

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mSymbol = "";
    private static final int CURSOR_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID_FOR_LINE_CHART = 2;

    @BindView(R.id.stock_name)
    TextView mNameView;

    @BindView(R.id.stock_symbol)
    TextView mSymbolView;

    @BindView(R.id.stock_bidprice)
    TextView mEbitdaView;

    @BindView(R.id.stock_chart)
    LineChartView mChart;

    @BindView(R.id.stock_change)
    TextView mChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stock_detail);
        ButterKnife.bind(this);
        savedInstanceState = this.getIntent().getExtras();
        this.mSymbol = savedInstanceState.getString("symbol");
        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_LINE_CHART, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == CURSOR_LOADER_ID) {
            /*resolver.query(Contract.Quote.URI,
                    null,
                    Contract.Quote.COLUMN_SYMBOL + " = \"" + mSymbol + "\"",
                    Contract.Quote.QUOTE_COLUMNS, null);*/
            return new CursorLoader(StockDetailActivity.this,
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS,
                    Contract.Quote.COLUMN_SYMBOL + " = \"" + mSymbol + "\"",
                    null, null);

        } else if (id == CURSOR_LOADER_ID_FOR_LINE_CHART) {

            String sortOrder = Contract.Quote._ID + " ASC";

            return new CursorLoader(StockDetailActivity.this,
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null, sortOrder);

        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == CURSOR_LOADER_ID && data != null && data.moveToFirst()) {

            String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            mSymbolView.setText(symbol);

            float ebitda = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            float percentageChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");

            DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");

            mEbitdaView.setText(dollarFormat.format(ebitda));
            String percentage = percentageFormat.format(percentageChange / 100);
            mChange.setText(percentage);

        } else if (loader.getId() == CURSOR_LOADER_ID_FOR_LINE_CHART && data != null) {
            data.moveToFirst();
            Timber.d("PRICES %s", data);
            updateChart(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void updateChart(Cursor data) {

        List<AxisValue> axisValuesX = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        int counter = -1;
        do {
            counter++;

            String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));

            Timber.d("CHART_SYMBOLS %s", symbol);

            String date = data.getString(data.getColumnIndex(
                    Contract.Quote.COLUMN_DATE));

            String bidPrice = data.getString(data.getColumnIndex(
                    Contract.Quote.COLUMN_PRICE));

            // We have to show chart in right order.
            int x = data.getCount() - 1 - counter;

            // Point for line chart (date, price).
            PointValue pointValue = new PointValue(x, Float.valueOf(bidPrice));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            // Set labels for x-axis (we have to reduce its number to avoid overlapping text).
            if (counter != 0 && counter % (data.getCount() / 3) == 0) {
                AxisValue axisValueX = new AxisValue(x);
                axisValueX.setLabel(date);
                axisValuesX.add(axisValueX);
            }

        } while (data.moveToNext());

        // Prepare data for chart
        Line line = new Line(pointValues).setColor(Color.WHITE).setCubic(false);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        // Init x-axis
        Axis axisX = new Axis(axisValuesX);
        axisX.setHasLines(true);
        axisX.setMaxLabelChars(4);
        lineChartData.setAxisXBottom(axisX);

        // Init y-axis
        Axis axisY = new Axis();
        axisY.setAutoGenerated(true);
        axisY.setHasLines(true);
        axisY.setMaxLabelChars(4);
        lineChartData.setAxisYLeft(axisY);

        // Update chart with new data.
        mChart.setInteractive(false);
        mChart.setLineChartData(lineChartData);

        // Show chart
        mChart.setVisibility(View.VISIBLE);
        //mTabContent.setVisibility(View.VISIBLE);
    }
}
