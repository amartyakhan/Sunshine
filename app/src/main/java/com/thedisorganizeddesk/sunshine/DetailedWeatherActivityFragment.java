package com.thedisorganizeddesk.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thedisorganizeddesk.sunshine.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailedWeatherActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = DetailedWeatherActivityFragment.class.getSimpleName();
    public final static String EXTRA_MESSAGE = "com.thedisorganizeddesk.sunshine.MESSAGE";
    private final int DETAILED_WEATHER_LOADER =1;
    private String mForecast;
    private ShareActionProvider mShareActionProvider;
    static final String DETAIL_URI = "URI";

    private Uri mUri;
    private static final String[] WEATHER_DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_HUMIDITY = 7;
    static final int COL_WIND_SPEED =8;
    static final int COL_DEGREE=9;
    static final int COL_PRESSURE = 10;

    public DetailedWeatherActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    WEATHER_DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }
        String dateString = Utility.getFormattedMonthDay(getActivity(),
                data.getLong(COL_WEATHER_DATE));
        String dayString = Utility.getDayName(getActivity(),data.getLong(COL_WEATHER_DATE));

        String weatherDescription =
                data.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        Float humidity = data.getFloat(COL_HUMIDITY);
        Float pressure = data.getFloat(COL_PRESSURE);
        String wind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WIND_SPEED), data.getFloat(COL_DEGREE));

        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);


        if(getView()!=null){
            ViewHolder viewHolder = (ViewHolder) getView().getTag();
            viewHolder.dateView.setText(dateString);
            viewHolder.dayView.setText(dayString);
            viewHolder.highTempView.setText(high);
            viewHolder.lowTempView.setText(low);
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));
            viewHolder.descriptionView.setText(weatherDescription);
            viewHolder.humidityView.setText(getString(R.string.format_humidity, humidity));
            viewHolder.pressureView.setText(getString(R.string.format_pressure, pressure));
            viewHolder.windView.setText(wind);
        }



        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            Log.v(LOG_TAG, "Setting share Intent in onLoadfininshed");
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(LOG_TAG, "In onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detailed_weather_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecast != null && mShareActionProvider !=null) {
            Log.v(LOG_TAG, "Setting share Intent in onCreateOptionsMenu");
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "In onOptionsItemSelected");
        if(item.getItemId()==R.id.action_share) {
            startActivity(createShareForecastIntent());
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        Log.v(LOG_TAG, "In createShareForecastIntent");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + "#SunshineAPP");
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAILED_WEATHER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailedWeatherActivityFragment.DETAIL_URI);
        }
        View view=inflater.inflate(R.layout.fragment_detailed_weather, container, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAILED_WEATHER_LOADER, null, this);
        }
    }

    /**
     * Cache of the children views for a detailed Activity view
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView dayView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;



        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon_imageview);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            dayView= (TextView) view.findViewById(R.id.detail_day_textview);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            humidityView= (TextView) view.findViewById(R.id.detail_humidity_textview);
            windView=(TextView) view.findViewById(R.id.detail_wind_textview);
            pressureView= (TextView) view.findViewById(R.id.detail_pressure_textview);
        }
    }


}
