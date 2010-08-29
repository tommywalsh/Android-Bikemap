package com.github.tommywalsh.map;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import android.os.Bundle;

public class BikeMapActivity extends MapActivity
{
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	
	MapView mapview = (MapView) findViewById(R.id.mapview);
	mapview.setBuiltInZoomControls(true);
	m_controller = mapview.getController();

	m_controller.setCenter(new GeoPoint(42378778, -71095667));
	m_controller.setZoom(16); // 16 seems best for biking speeds
    }

    @Override protected boolean isRouteDisplayed() {
	return false;
    }

    MapController m_controller;
    //    LocationManager m_locmanager;
    
}


