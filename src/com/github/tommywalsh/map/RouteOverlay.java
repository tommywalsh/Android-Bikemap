package com.github.tommywalsh.map;

import java.util.TreeSet;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Vector;
import java.util.Scanner;
import java.io.File;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.MapView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Path;
import android.graphics.Color;
import android.os.Handler;

import android.util.Log;

public class RouteOverlay extends Overlay
{

    public void draw(Canvas canvas, MapView mv, boolean shadow) {
	super.draw(canvas, mv, shadow);
	m_mapView = null;

	if (m_loaded) {

	    if (shadow) {
		
		GeoPoint ctr = mv.getMapCenter();
		int latSpan = mv.getLatitudeSpan();
		int lat1 = ctr.getLatitudeE6() - latSpan/2;
		int lat2 = lat1 + latSpan;
		int lngSpan = mv.getLongitudeSpan();
		int lng1 = ctr.getLongitudeE6() - lngSpan/2;
		int lng2 = lng1 + lngSpan;
		
		if (m_path == null || !ctr.equals(m_lastCenter)) {
		    
		    // We need to calculate a new path
		    m_path = new Path();
		    m_lastCenter = ctr;
		    
		    boolean drewLastPoint = false;
		    for (int i = 0; i < m_route.size(); i++) {
			GeoPoint gp = m_route.elementAt(i);
			int lat = gp.getLatitudeE6();
			int lng = gp.getLongitudeE6();
			Point thisPoint = new Point();
			Point otherPoint = new Point();
			
			
			if (lat >= lat1 && lat <= lat2 && lng >= lng1 && lng <= lng2) {
			    // this point is in view.  We will draw from/to here
			    mv.getProjection().toPixels(gp, thisPoint);
			    if (drewLastPoint) {
				// keep drawing
				m_path.lineTo(thisPoint.x, thisPoint.y); 
			    } else if (i != 0) {
				// draw from the last off-screen point
				mv.getProjection().toPixels(m_route.elementAt(i-1), otherPoint);
				m_path.moveTo(otherPoint.x, otherPoint.y);
				m_path.lineTo(thisPoint.x, thisPoint.y); 
			    } else {
				// start here
				m_path.moveTo(thisPoint.x, thisPoint.y);
			    }
			    drewLastPoint = true;
			} else {
			    // This point is not in view.  Draw to here if we're the first off-screen point
			    if (drewLastPoint) {
				mv.getProjection().toPixels(gp, thisPoint);
				m_path.lineTo(thisPoint.x, thisPoint.y);
			    }
			drewLastPoint = false;
			}
		    }
		}
	    }



	    if (!m_path.isEmpty()) {
		Paint paint = new Paint();
		paint.setDither(true);
		paint.setARGB(126,0,126,255);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(5);
		
		canvas.drawPath(m_path, paint);
	    }

	} else {
	    // We don't have any data yet.  Show a message.
	    m_mapView = mv;
	    Paint paint = new Paint();
	    if (shadow) {
		paint.setARGB(255,0,0,0);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(0,0,300,40, paint);		
	    } else {
		paint.setARGB(255,255,255,255);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawText("Loading Route Data...", 10, 20, paint);
	    }
	}
    }


    public RouteOverlay() {
	startIOProcess();
    }

    private void loadData() {
	try {
	    Scanner scanner = new Scanner(new File("/sdcard/mapdata/coords"));
	    int idx = 0;
	    while (scanner.hasNextDouble()) {
		int lng = (int)(scanner.nextDouble()*1E6);
		if (scanner.hasNextDouble()) {
		    int lat = (int)(scanner.nextDouble()*1E6);
		    if (scanner.hasNextDouble()) {
			scanner.nextDouble();
		    }		
		    GeoPoint thisPoint = new GeoPoint(lat,lng);
		    m_route.add(thisPoint);
		}
	    }
	} catch (java.io.FileNotFoundException e) {
	}
    }

    Path m_path;
    GeoPoint m_lastCenter;
    Vector<GeoPoint> m_route = new Vector<GeoPoint>();  // m_route is not safe to access unless m_loaded is true


    // It takes a *LONG* time to read in data from the sdcard.  Not yet sure why that is.
    // Until that performance is improved, do the IO in a separate thread, and postpone
    // any useful drawing until the IO is complete.  This lets the UI start and be responsive
    // right away.
    MapView m_mapView = null;
    boolean m_loaded = false;
    final Handler m_handler = new Handler();
    final Runnable m_IODoneUpdater = new Runnable() {
	    public void run() {
		m_loaded = true;
		m_mapView.invalidate();
	    }
	};
    private void startIOProcess() {
	Thread t = new Thread() {
		public void run() {
		    loadData();
		    m_handler.post(m_IODoneUpdater);
		}
	    };
	t.start();
    }
}
