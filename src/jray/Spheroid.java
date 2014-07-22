package jray;

import javax.vecmath.Point3d;

/**
 * Spheroid is simply a special case of an ellipsoid.
 * Also known as a sphere.
 */
public class Spheroid extends Ellipsoid {

	/** center of the sphere v, radius of the sphere R, and diameter s */
	public Spheroid (Point3d v, double R, double s) {
		this (v, R, s, 0);
	}
	/** construct with a hole in the center of the spheroid */
	public Spheroid (Point3d v, double R, double s, double h) {
		this(v, R, s/2., s/2., h);
	}
	public Spheroid (Point3d v, double R, double ls, double rs, double h) {
		// R is the radius of curvature = 2 focal length
		super(v, new Point3d (R, R, R), ls, rs, h, 0.0);
	}

	/** overrides getFocus() */
	public Point3d getFocus (int i) {
		System.out.println ("Spheroid getFocus Error:" +
							" Spheroids have no focii.");
		return new Point3d ();
	}
} // end of Class Spheroid *****************************************************
