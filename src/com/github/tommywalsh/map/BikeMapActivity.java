package com.github.tommywalsh.map;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.location.Criteria;
import android.content.Context;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Paint;

import android.util.Log;

import java.util.TreeSet;

public class BikeMapActivity extends MapActivity implements LocationListener
{
    Location m_oldLoc;
    public void onLocationChanged(Location loc) {
	if (loc != null && !loc.equals(m_oldLoc)) {
	    m_oldLoc = loc;
	    m_location = new GeoPoint((int)(loc.getLatitude() * 1E6),
			      (int)(loc.getLongitude() * 1E6));
	    m_controller.animateTo(m_location);
	}
    }
    public void onProviderDisabled(String provider) {
    }
    public void onProviderEnabled(String provider) {
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    public class LocationIndicator extends Overlay {
	
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	    super.draw(canvas, mapView, shadow, when);
	    
	    if (m_location != null) {
		Point screenCoords = new Point();

		Paint paint = new Paint();		
		mapView.getProjection().toPixels(m_location, screenCoords);
		paint.setStrokeWidth(1);
		if (shadow) {
		    paint.setARGB(255,0,0,255);
		    paint.setStyle(Paint.Style.FILL_AND_STROKE);
		} else {		    
		    paint.setARGB(255,0,255,0);
		    paint.setStyle(Paint.Style.STROKE);
		}
		canvas.drawCircle(screenCoords.x, screenCoords.y, 5, paint);

		}
	    return true;
	}
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	
	m_locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
	
	MapView mapview = (MapView) findViewById(R.id.mapview);
	mapview.setBuiltInZoomControls(true);
	mapview.getOverlays().add(new LocationIndicator());
	mapview.getOverlays().add(m_route);
	
	m_controller = mapview.getController();

	m_controller.setZoom(16); // 16 seems best for biking speeds
	m_controller.setCenter(new GeoPoint(42378778, -71095667)); // Union Square
    }

    @Override protected boolean isRouteDisplayed() {
	return false;
    }

    MapController m_controller;
    LocationManager m_locManager;
    GeoPoint m_location;
    RouteOverlay m_route = new RouteOverlay();
}


