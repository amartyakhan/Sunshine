package com.thedisorganizeddesk.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;


public class DetailedWeatherActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.thedisorganizeddesk.sunshine.MESSAGE";
    ShareActionProvider mShareActionProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_weather);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailedWeatherActivityFragment.DETAIL_URI, getIntent().getData());

            DetailedWeatherActivityFragment fragment = new DetailedWeatherActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == R.id.action_view_map) {
//            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//            String postalCode= sharedPref.getString(getString(R.string.pref_location_key), "");
//            String loc= "geo:0,0?q="+postalCode;
//            Uri geoLocation= Uri.parse(loc);
//
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(geoLocation);
//            //making sure there is at least one app to handle the intent before launching it
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivity(intent);
//            }
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
