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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Context;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Paint;

import java.util.TreeSet;


// This activity handles the Map application.
public class BikeMapActivity extends MapActivity implements LocationListener
{

    Location m_oldLoc;
    public void onLocationChanged(Location loc) {
	// Always make sure our current position is centered on the screen
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



    // Draw a little circle at our current position
    public class LocationIndicator extends Overlay {
	
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	    super.draw(canvas, mapView, shadow, when);
	    
	    if (m_location != null) {
		Point screenCoords = new Point();

		Paint paint = new Paint();		
		mapView.getProjection().toPixels(m_location, screenCoords);
		paint.setStrokeWidth(3);
		if (shadow) {
		    paint.setARGB(255,0,0,255);
		    paint.setStyle(Paint.Style.FILL_AND_STROKE);
		} else {		    
		    paint.setARGB(255,0,255,0);
		    paint.setStyle(Paint.Style.STROKE);
		}
		canvas.drawCircle(screenCoords.x, screenCoords.y, 10, paint);

		}
	    return true;
	}
    }



    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	
	m_locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	
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

    @Override protected void onStart() {
	super.onStart();
	m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
    }

    @Override protected void onStop() {
	// Conserve battery by not asking for location updates when we're not visible
	super.onStop();
	m_locManager.removeUpdates(this);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.quit:
	    this.finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }
    
    MapController m_controller;
    LocationManager m_locManager;
    GeoPoint m_location;
    RouteOverlay m_route = new RouteOverlay();

}


