package com.github.tommywalsh.map;

import java.util.Vector;
import java.util.Scanner;
import java.lang.Math;
import java.util.StringTokenizer;
import com.google.android.maps.GeoPoint;


public class TripLeg
{
    public TripLeg (String coordinates) {
	Scanner scanner = new Scanner(coordinates);
	while (scanner.hasNextLine()) {
	    String line = scanner.nextLine();
	    StringTokenizer st = new StringTokenizer(line, ",");
	    if (st.hasMoreTokens()) {
		try {
		    double lng = Double.parseDouble(st.nextToken());
		    if (st.hasMoreTokens()) {
			double lat = Double.parseDouble(st.nextToken());
			addPoint(lat,lng);
		    }
		} catch (NumberFormatException e) {
		}
	    }
	}
    }

    private void addPoint(double lat, double lng) {
	addPoint((int)(lat*1E6), (int)(lng*1E6));
    }

    private void addPoint(int lat, int lng) {
	if (m_pathPoints.isEmpty()) {
	    minLat = lat;
	    maxLat = lat;
	    minLng = lng;
	    maxLng = lng;
	} else {
	    minLat = Math.min(lat, minLat);
	    maxLat = Math.max(lat, maxLat);
	    minLng = Math.min(lng, minLng);
	    maxLng = Math.max(lng, maxLng);
	}
	m_pathPoints.add(new GeoPoint(lat, lng));
    }

    public int size() {
	return m_pathPoints.size();
    }

    public GeoPoint elementAt(int idx) {
	return m_pathPoints.elementAt(idx);
    }

    boolean hasPointsInRect(int lat1, int lat2, int lng1, int lng2) {
	return (lat2 >= minLat &&
		lat1 <= maxLat &&
		lng2 >- minLng &&
		lng1 <= maxLat);		
    }

    Vector<GeoPoint> m_pathPoints = new Vector<GeoPoint>();
    int minLat;
    int maxLat;
    int minLng;
    int maxLng;
}