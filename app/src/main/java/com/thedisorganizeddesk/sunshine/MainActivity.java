package com.thedisorganizeddesk.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.URI;
import java.net.URL;


public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG =MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(LOG_TAG,"Activity Created");
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
        Log.v(LOG_TAG,"Activity Started");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG,"Activity Paused");
    }

    @Override
    public void onStop(){
        super.onPause();
        Log.v(LOG_TAG,"Activity Stopped");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(LOG_TAG,"Activity Resumed");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOG_TAG,"Activity Destroyed");
    }


}
