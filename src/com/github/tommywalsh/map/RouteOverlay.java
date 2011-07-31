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
import android.graphics.Rect;
import android.graphics.Path;
import android.graphics.Color;
import android.os.Handler;


import android.util.Log;
// This class handles drawing of a path on a map.

// The path is stored as KML file, and displayed as a semi-translucent overlay 
// over the map.

// It tries to be smart about recalculating screen positions for the points.
// That is, if you don't move, it shouldn't need to do any calculations.
// If you only move "a little", it shouldn't need to do much.


public class RouteOverlay extends Overlay
{

    public RouteOverlay() {
    }

    public void loadDataSet(String filename) {
	startIOProcess(filename);	
    }

    // The data about the route is stored as latitude/longitude pairs
    // This function's job is to convert these into on-screen pixel positions,
    // and store them in a 'Path' that the canvas can draw on screen.
    // It caches its calculations and tries not to do more work than necessary
    int m_lat;
    int m_lng;
    int m_lat1;
    int m_lat2;
    int m_lng1;
    int m_lng2;
    public void calculateNewPathIfNeeded(MapView mv)
    {
        if (m_legs.isEmpty()) return;

	boolean shouldCalculate = false;
	GeoPoint ctr = mv.getMapCenter();
	
	// If we don't yet have a path, that obviously means we need to calculate, and 
	// we should also cache the view limits for next time
        if (m_path == null) {
	    shouldCalculate = true;
	} else {
	    // We already have a path, which means we must have cached values
            // If we've simply moved a bit without changing the zoom level, and have
            // stayed on-screen, just shift pixels.
	    int lat = ctr.getLatitudeE6();
	    int lng = ctr.getLongitudeE6();
            if (lat < m_lat1 ||
                lat > m_lat2 ||
                lng < m_lng1 ||
                lng > m_lng2 ||
                mv.getLongitudeSpan() != (m_lng2 - m_lng1)) {
		shouldCalculate = true;
	    }
	}
	if (shouldCalculate) {
	    // Okay, we need to calculate a new path.  We'll make a path big enough to cover 9 screens.
	    // (One each to the North, NE, E, SE, S, SW, W, and NW, and of course the real screen)
	    // This allows us to wander all around the original screen all we want without having to recalculate
	    m_path = new Path();

	    // So, lets set our 'area of interest'...
            m_lat = ctr.getLatitudeE6();
            m_lng = ctr.getLongitudeE6();
	    int latSpan = mv.getLatitudeSpan();
	    int lngSpan = mv.getLongitudeSpan();
            m_lat1 = m_lat - 2*latSpan/3;
            m_lat2 = m_lat + 2*latSpan/3;
            m_lng1 = m_lng - 2*lngSpan/3;
            m_lng2 = m_lng + 2*lngSpan/3;


	    // Each tip may have one or more legs.  Try to draw them all
	    for (TripLeg leg : m_legs) {
		
		// We can be more efficient here by short-circuiting and not considering
		// legs that aren't even partially on-screen
		
		// Iterate over each point in the leg, and figure out if we need to add it
		// to the drawn path.  The analogy here is a pen.  
		boolean drewLastPoint = false;
		for (int i = 0; i < leg.size(); i++) {
		    GeoPoint gp = leg.elementAt(i);
		    int lat = gp.getLatitudeE6();
		    int lng = gp.getLongitudeE6();
		    Point thisPoint = new Point();
		    Point otherPoint = new Point();

                    if (lat >= m_lat1 &&
                        lat <= m_lat2 &&
                        lng >= m_lng1 &&
                        lng <= m_lng2) {

			// This point is in area of interest.  We will need to do some drawing
                        mv.getProjection().toPixels(gp, thisPoint);
			if (drewLastPoint) {
			    // Last point was also in area of interest... 
			    // The pen is on the paper.  Draw to the current point.
			    m_path.lineTo(thisPoint.x, thisPoint.y); 
			} else if (i != 0) {
			    // The last point was out of the area of intersest.
			    // Pick up the pen and put it there, then draw to the current point.
			    mv.getProjection().toPixels(leg.elementAt(i-1), otherPoint);
			    m_path.moveTo(otherPoint.x, otherPoint.y);
			    m_path.lineTo(thisPoint.x, thisPoint.y);
			} else {
			    // This is the first point in the leg.
			    // Pick up the pen and position it at this point.
			    m_path.moveTo(thisPoint.x, thisPoint.y);
			}
			drewLastPoint = true;
		    } else {
			// This point is not in the area of interest...
			if (drewLastPoint) {
			    // ... but the last point was.  Draw to this point to finish the path
			    mv.getProjection().toPixels(gp, thisPoint);
			    m_path.lineTo(thisPoint.x, thisPoint.y);
                        } else {
                            // Neither is in the area of interest.  But, their connecting line 
                            // might CROSS the area of interest
                            if (i != 0) {
                                GeoPoint lastgeo = leg.elementAt(i-1);
                                Point previous = new Point (lastgeo.getLongitudeE6(),
                                                            lastgeo.getLatitudeE6());
                                Point current = new Point(lng, lat);
                                if (Utils.doExteriorPointsCutRect(previous, current, 
                                                                  m_lng1, m_lat1,
                                                                  m_lng2, m_lat2)) {
                                    mv.getProjection().toPixels(gp, thisPoint);
                                    mv.getProjection().toPixels(lastgeo, otherPoint);
                                    m_path.moveTo(otherPoint.x, otherPoint.y);
                                    m_path.lineTo(thisPoint.x, thisPoint.y);
                                }
                            }
                        }
                        drewLastPoint = false;
                    }
                }
	    }
	} else {
	    // Here, we don't need to calculate a whole new path, but we might need to move the existing one a bit
	    Point lastPoint = new Point();
	    Point thisPoint = new Point();
	    mv.getProjection().toPixels(new GeoPoint(m_lat, m_lng), lastPoint);
	    mv.getProjection().toPixels(ctr, thisPoint);
	    float dx = lastPoint.x - thisPoint.x;
	    float dy = lastPoint.y - thisPoint.y;
	    if (dx != 0.0 || dy != 0.0) {
		m_path.offset(dx, dy);
	    }
            m_lat = ctr.getLatitudeE6();
            m_lng = ctr.getLongitudeE6();
	}
    }


    // Called periodically by the Android system when it wants us to redraw the path
    public void draw(Canvas canvas, MapView mv, boolean shadow) {
	super.draw(canvas, mv, shadow);


	if (m_loaded) {

	    // This is the normal case: all of our data is already loaded
	    // So, we need to draw the path to screen

	    // Draws are called twice, once for the "shadow" and once for the foreground
	    // Since we draw only one thing, we only need to draw during one of these
	    // Pick shadow, in case we ever add anything later that we want to appear
	    // on top of the path.
	    if (shadow) {		
		calculateNewPathIfNeeded(mv);

		// Now, we've got a fully-calculated path.  Put it on the screen (if it's not empty)
		if (m_path != null && !m_path.isEmpty()) {
		    Paint paint = new Paint();
		    paint.setDither(true);
		    paint.setARGB(126,0,126,255);
		    paint.setStyle(Paint.Style.STROKE);
		    paint.setStrokeJoin(Paint.Join.ROUND);
		    paint.setStrokeCap(Paint.Cap.ROUND);
		    paint.setStrokeWidth(5);		
		    canvas.drawPath(m_path, paint);
		}
	    }
	    
	} else {
	    // We get here if the data hasn't been loaded.  Just show a message.
	    
	    Paint paint = new Paint();
	    if (shadow) {
		// Draw a black rectangular background...
		paint.setARGB(255,0,0,0);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(0,0,300,40, paint);		
	    } else {
		// With white text in front.
		paint.setARGB(255,255,255,255);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawText("Loading Route Data...", 10, 20, paint);
	    }
	    
	    // And cache the map so we can force a redraw when we get the data loaded.
	    m_mapView = mv;
	}
    }




    GeoPoint m_lastCenter;    // Cache the last center point so we don't bother redrawing the path unless it changes
    Path m_path;              // Also cache the path for potential reuse next draw cycle


    // We do our IO operations in another thread.  It can take in a while to read in a whole bike trip's
    // worth of position data.  Don't hang the UI thread during this time! 

    // If we draw before we load our data, cache the mapview so we can force it to redraw    
    MapView m_mapView = null;        

    // ONLY to be used on the UI thread!
    // "true" because we are trivially loaded at startup
    boolean m_loaded = true;         

    // m_legs is not safe to access unless m_loaded is true
    Vector<TripLeg> m_legs = new Vector<TripLeg>();   

    final Handler m_handler = new Handler();
    final Runnable m_IODoneUpdater = new Runnable() {
	    // This will be called when IO operations are done on the other thread.
	    // This runs on the UI thread, so it is safe to change m_loaded
	    // Also, force a redraw of the map, so we can draw the newly-gotten route
	    public void run() {
		m_loaded = true;   // done loading
		if (m_mapView != null) {
		    // force redraw and stop caching map view
		    m_mapView.invalidate();
		    m_mapView = null;
		}
	    }
	};

    
    private void startIOProcess(final String filename) {

	// clear our caches so we don't draw an old path while we wait
	// for the data
	m_loaded = false;
	m_legs = null;
	m_path = null;
	m_lastCenter = null;

	Thread t = new Thread() {
		public void run() {

		    // This runs on its own thread.
		    // It's okay to take our sweet time loading data here.
		    m_legs = KMLParser.parse(filename);

		    // Notify the UI thread that we're done (this will cause m_IODoneUpdater.run to run on the UI thread)
		    m_handler.post(m_IODoneUpdater);
		}
	    };
	t.start();
    }
}
