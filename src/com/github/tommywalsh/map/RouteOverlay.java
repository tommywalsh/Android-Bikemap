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

import android.util.Log;

public class RouteOverlay extends Overlay
{

    public void draw(Canvas canvas, MapView mv, boolean shadow) {

	// Try calulating the path once, then using offset each draw for speed?

	super.draw(canvas, mv, shadow);

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

	    if (!m_path.isEmpty()) {
		Paint mPaint = new Paint();
		mPaint.setDither(true);
		mPaint.setARGB(126,0,126,255);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(5);
		
		canvas.drawPath(m_path, mPaint);
	    }
	}
    }

    public RouteOverlay() {
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
		    Integer i = new Integer(lat);
		}
	    }
	} catch (java.io.FileNotFoundException e) {
	}
    }

    Path m_path;
    GeoPoint m_lastCenter;
    Vector<GeoPoint> m_route = new Vector<GeoPoint>();
}