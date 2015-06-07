package com.thedisorganizeddesk.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;


public class DetailedWeatherActivity extends ActionBarActivity {
    public final static String EXTRA_MESSAGE = "com.thedisorganizeddesk.sunshine.MESSAGE";
    ShareActionProvider mShareActionProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_weather);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_weather, menu);

        /** Getting the actionprovider associated with the menu item whose id is share */
        MenuItem menuItem=menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);



        return super.onCreateOptionsMenu(menu);
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
        if (id == R.id.action_view_map) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String postalCode= sharedPref.getString(getString(R.string.pref_location_key), "");
            String loc= "geo:0,0?q="+postalCode;
            Uri geoLocation= Uri.parse(loc);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);
            //making sure there is at least one app to handle the intent before launching it
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            return true;
        }
        if (id == R.id.action_share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            //getting the text to share
            String shareString;
            Intent parentIntent=getIntent();
            shareString="Weather: " + getIntent().getStringExtra(EXTRA_MESSAGE) + "#SunshineApp";
            shareIntent.putExtra(Intent.EXTRA_TEXT,shareString);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(shareIntent);
            }

            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
