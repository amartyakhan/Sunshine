package com.thedisorganizeddesk.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailedWeatherActivityFragment extends Fragment {
    public final static String EXTRA_MESSAGE = "com.thedisorganizeddesk.sunshine.MESSAGE";

    public DetailedWeatherActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent=getActivity().getIntent();
        String detail = intent.getStringExtra(EXTRA_MESSAGE);


        View v=inflater.inflate(R.layout.fragment_detailed_weather, container, false);
        TextView tv=(TextView) v.findViewById(R.id.weatherDetails);
        tv.setText(detail);
        return v;
    }
}
