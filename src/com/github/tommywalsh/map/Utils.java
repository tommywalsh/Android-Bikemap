package com.github.tommywalsh.map;

import android.graphics.Point;
import android.graphics.Rect;

public class Utils
{
    private static boolean ccw(Point p1, Point p2, Point p3)
    {
        return (p3.y-p1.y)*(p2.x-p1.x) > (p2.y-p1.y)*(p3.x*p1.x);
    }
    
    public static boolean doSegmentsIntersect(Point p1, Point p2, Point p3, Point p4)
    {
        // The SEGMENT p1.p2 crosses the LINE p3.p4 iff 
        //       (p1,p2,p3) has a different "clockwiseness" from (p1,p2,p4)
        // Likewise for the SEGMENT p3.p4 and the LINE p1.p2
        // The two SEGMENTS intersect iff both pairs have different clockwisenesses
        
        return (ccw(p1,p2,p3) != ccw(p1,p2,p4)) &&
               (ccw(p1,p3,p4) != ccw(p2,p3,p4));
    }

    public static boolean doExteriorPointsCutRect(Point p1, Point p2, int left, int top, int right, int bottom)
    {
        // Since the given points are both exterior to the rectangle, they cannot cut
        // just a single side.  They must cut two or zero.  It is enough to check three
        // sides of the rect.
        Point tl = new Point(left,top);
        Point tr = new Point(right,top);
        Point bl = new Point(left,bottom);
        Point br = new Point(right,bottom);
        return 
            doSegmentsIntersect(p1,p2,tl,tr) ||
            doSegmentsIntersect(p1,p2,tl,bl) ||
            doSegmentsIntersect(p1,p2,tr,br);
    }
};
