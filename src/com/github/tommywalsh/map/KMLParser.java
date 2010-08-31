package com.github.tommywalsh.map;

import android.sax.RootElement;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.EndElementListener;
import android.util.Xml;

import java.util.Vector;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import android.util.Log;

public class KMLParser {

    static public Vector<TripLeg> parse(String filename) {

	final Vector<TripLeg> legs = new Vector<TripLeg>();


	final String NS = "http://earth.google.com/kml/2.2";
	RootElement root = new RootElement("http://earth.google.com/kml/2.2", "kml");
	Element doc = root.getChild(NS, "Document");      
	Element pm = doc.getChild(NS, "Placemark");
	Element ls = pm.getChild(NS, "LineString");
	ls.getChild(NS, "coordinates").setEndTextElementListener(new EndTextElementListener() {
		public void end(String body) {
		    legs.add(new TripLeg(body));
		}
	    });
	    

	try {
	    BufferedInputStream ifstream = new BufferedInputStream(new FileInputStream(filename));
	    Xml.parse(ifstream, Xml.Encoding.UTF_8, root.getContentHandler());
	} catch (Exception e) {
	    Log.d("kml", e.toString());
	}
	
	return legs;	 	    
    }
}
