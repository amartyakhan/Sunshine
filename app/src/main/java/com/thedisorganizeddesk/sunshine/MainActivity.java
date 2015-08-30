package com.thedisorganizeddesk.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.thedisorganizeddesk.sunshine.sync.SunshineSyncAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{
    private final String LOG_TAG =MainActivity.class.getSimpleName();
    //private final String FORECASTFRAGMENT_TAG = "FFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    /**
     * Substitute you own project number here. This project number comes
     * from the Google Developers Console.
     */
    static final String PROJECT_NUMBER = "508125533828";

    private GoogleCloudMessaging mGcm;
    private String mLocation;
    private boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mLocation=Utility.getSettingLocation(this);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            //om two-pane mode, set the specialtodayview to false
            //getting the forecast fragment

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailedWeatherActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        ForecastFragment forecastFragment= ((ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast));
        forecastFragment.setTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);

        // These two lines are working around an android bug:
        // https://code.google.com/p/android/issues/detail?id=6641
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("@string/pref_enable_nofitication_key", prefs.getBoolean("@string/pref_enable_nofitication_key", true)).commit();

        // If Google Play Services is not available, some features, such as GCM-powered weather
        // alerts, will not be available.
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(this);
            String regId = getRegistrationId(this);

            /*if (PROJECT_NUMBER.equals("Your project number")) {
                new AlertDialog.Builder(this)
                        .setTitle("Needs Project Number")
                        .setMessage("GCM will not function in Sunshine until you set the Project Number to the one from the Google Developers Console.")
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
            }*/
            if (regId.isEmpty()) {
                registerInBackground(this);
            }
        } else {
            Log.i(LOG_TAG, "No valid Google Play Services APK. Weather alerts will be disabled.");
            // Store regID as null
            storeRegistrationId(this, null);
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        //Log.v(LOG_TAG,"Activity Started");
    }

    @Override
    public void onPause(){
        super.onPause();
        //Log.v(LOG_TAG,"Activity Paused");
    }

    @Override
    public void onStop(){
        super.onPause();
        //Log.v(LOG_TAG,"Activity Stopped");
    }

    @Override
    public void onResume(){
        super.onResume();
        //Log.v(LOG_TAG,"Activity Resumed");
        String location = Utility.getSettingLocation(this);

        //TODO handle the first run settings
        if(location ==null){
            Utility.setDefaultLocation(this);
            location=Utility.getSettingLocation(this);
        }
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailedWeatherActivityFragment df = (DetailedWeatherActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;

        }

        // If Google Play Services is not available, some features, such as GCM-powered weather
        // alerts, will not be available.
        if (!checkPlayServices()) {
            // Store regID as null
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //Log.v(LOG_TAG,"Activity Destroyed");
    }


    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailedWeatherActivityFragment.DETAIL_URI, contentUri);

            DetailedWeatherActivityFragment fragment = new DetailedWeatherActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailedWeatherActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(LOG_TAG, "GCM Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOG_TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // Sunshine persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you. Just make sure
        // that it is private!
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen. WHAT DID YOU DO?!?!
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String msg = "";
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regId = mGcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    //sendRegistrationIdToBackend();
                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // TODO: If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                Log.v(LOG_TAG,msg);
                return null;
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

}
