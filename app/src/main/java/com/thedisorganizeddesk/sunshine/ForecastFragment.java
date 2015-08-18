package com.thedisorganizeddesk.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.thedisorganizeddesk.sunshine.data.WeatherContract;
import com.thedisorganizeddesk.sunshine.sync.SunshineSyncAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    String LOG_TAG=ForecastFragment.class.getSimpleName();
    public final static String EXTRA_MESSAGE = "com.thedisorganizeddesk.sunshine.MESSAGE";
    private ForecastAdapter adapter;
    // Will contain the raw JSON response as a string.
    private String forecastJsonStr = null;
    private final int URL_LOADER =0;
    private int currentPosition;
    private ListView listView;

    private static final String[] FORECAST_COLUMNS = {
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
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
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
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    @Override
    public Loader<Cursor> onCreateLoader(int loader_id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());


        switch (loader_id){
            case URL_LOADER:
                return new CursorLoader(
                        getActivity(),
                        weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        sortOrder
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        //check for empty cursor
        if(data.getCount()<=0){
            //check for the network state
            ConnectivityManager cm =
                    (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if(!isConnected){
                //change the empty text view to something more helpful
                TextView emptyTextView=(TextView) getActivity().findViewById(android.R.id.empty);
                emptyTextView.setText("Network connection not available");
            }
        }
        if(currentPosition!=-1){
            //listView.setSelection(currentPosition);
            listView.smoothScrollToPosition(currentPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(URL_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {


        // The CursorAdapter will take data from our cursor and populate the ListView
        adapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView;
        rootView = inflater.inflate(R.layout.fragment_main,container,false);
        listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(adapter);
        listView.setEmptyView(rootView.findViewById(android.R.id.empty));

        if(savedInstanceState!=null){
            int position=savedInstanceState.getInt("CurrentPosition",-1);
            if(position !=-1){
                currentPosition=position;
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.

                //adding current position
                currentPosition=position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
                }
            }
        });

     return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CurrentPosition", currentPosition);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(URL_LOADER, null, this);
    }

    public void setTodayLayout(boolean value){
        if(adapter!=null){
            adapter.setmSpecialTodayView(value);
        }
    }
}
