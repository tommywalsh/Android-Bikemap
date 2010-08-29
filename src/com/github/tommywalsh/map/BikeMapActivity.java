package com.github.tommywalsh.map;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import android.os.Bundle;

public class BikeMapActivity extends MapActivity
{
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	
	MapView mapview = (MapView) findViewById(R.id.mapview);
	mapview.setBuiltInZoomControls(true);
    }

    @Override protected boolean isRouteDisplayed() {
	return false;
    }
}


