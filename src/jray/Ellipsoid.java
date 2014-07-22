package jray;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.media.j3d.Transform3D;

/**
 * Ellipsoid: Ellipse of revolution
 */
public class Ellipsoid implements ConicSection {

	/** location of the vertex of this ConicSection */
	protected Point3d vertex;
	/** The diameter of this ellipsoid */
	protected double size;
	protected double lsize, rsize;
	/** location of the center of this ConicSection */
	protected double xcenter;
	/** Ellipsoid parameter */
	protected double a, b, c;
	/** size of hole in center of ConicSection */
	protected double hole;
	private double time;    // time elapsed from LightRay start to intersection
	/** false = concave down, true = concave up */
	protected boolean downup = false;
	/** angle of rotation about the y-axis */
	protected double angle;

	/**
	 * The equation of a ellipsoid is
	 * (x/a)^2 + (y/b)^2 + (z/c)^2 = 1
	 * The required parameters are the vertex and a point3d with (a, b, c)
	 */
	public Ellipsoid (Point3d v, Point3d abc, double s) {
		this (v, abc, s, 0);
	}
	/** Ellipsoid with a hole in the center */
	public Ellipsoid (Point3d v, Point3d abc, double s, double h) {
		this(v, abc, s, h, 0.0);
	}
	public Ellipsoid (Point3d v, Point3d abc, double s, 
					  double h, double angle) {
		this(v, abc, s/2.0,s/2.0, h, angle);
	}
	public Ellipsoid (Point3d v, Point3d abc, double ls, double rs,
					  double h, double angle) {
		this.vertex = new Point3d (v);
		this.a = Math.abs (abc.x);
		this.b = Math.abs (abc.y);
		this.c = Math.abs (abc.z);
		if (abc.x < 0 || abc.y < 0 || abc.z < 0)
			downup = true;
		this.size = (ls + rs) * SIZE_FACTOR;
		this.lsize = ls;
		this.rsize = rs;
		this.xcenter = vertex.x + 0.5 * (rs - ls);
		this.hole = h;
		if (angle == Math.PI)
			angle = 0;
		this.angle = angle;
		if (c > a) this.angle = -this.angle;
		this.time = -1;
	}

	/** Evaluate at point x the value z for 2D Rendering */
	public double Evaluate (double x) {
		if ( (x-vertex.x) < -this.lsize )
			return ConicSection.NULL;
		else if ((x-vertex.x) > this.rsize)
			return ConicSection.NULL;
		else if ( Math.abs (x - (this.vertex).x) < this.hole/2. )
			return ConicSection.BLANK;
		else if (angle == 0.0) {
			if (downup)
				return (this.vertex).z - this.c *
					Math.sqrt (1.0 - Math.pow ( (x - (this.vertex).x) / 
												this.a , 2.0));
			else
				return  (this.vertex).z + this.c * 
					Math.sqrt (1.0 - Math.pow ( (x - (this.vertex).x) / 
												this.a , 2.0));
		} else {
			double x0 = vertex.x;
			double z0 = vertex.z;
			double sa = Math.sin (-angle);
			double ca = Math.cos (-angle);
			double xp = (a*a*ca*(x + z0*sa) +
						 c*sa*(c*x0*sa - a*
							   Math.sqrt (-x*x + 2*x*x0*ca +
										  (a*a-x0*x0)*ca*ca -
										  2*x*z0*sa + (c*c-z0*z0)*sa*sa +
										  x0*z0*2*sa*ca))) /
				(a*a*ca*ca + c*c*sa*sa);
			double zp = z0 - c * Math.sqrt (1.0 - Math.pow((xp - x0)/a, 2.0));
			double z = xp * sa + zp * ca;
			if (Double.isNaN (z) || Double.isInfinite (z))
				return ConicSection.NULL;
			return z;
		}
	} // end of Evaluate *******************************************************

	/** 
	 * Returns a LightRay at the surface of the
	 * ConicSection in the outgoing reflected direction.
	 */
	public LightRay reflect (LightRay input) {
		// Find intersection of LightRay and ConicSection
		Point3d iPoint = intersect (input);
		Vector3d iVel = input.getVelocity ();
		Vector3d oVel = new Vector3d ();
		// Check to see if point needs to be reflected
		Point3d  rPoint = new Point3d ();
		Transform3D rot = new Transform3D ();
		rot.rotY (-this.angle);
		rot.transform (iPoint, rPoint);
		double cx = iPoint.x - xcenter;
		double cy = iPoint.y - vertex.y;
		double radius = Math.sqrt ( cx * cx + cy * cy);
		if ( radius > this.size/2.0 ||
			 radius < this.hole/2.0 ||
			 this.time == 0)
			oVel = new Vector3d (iVel);
		else {
			// The Gradient of the equation of our ConicSection:
			Vector3d normal = 
				new Vector3d (+2.0*(rPoint.x - vertex.x)/Math.pow (this.a,2.0),
							  +2.0*(rPoint.y - vertex.y)/Math.pow (this.b,2.0),
							  +2.0*(rPoint.z - vertex.z)/Math.pow (this.c,2.0));
			normal.normalize ();
			if (downup && normal.z < 0) normal.negate ();
			rot.transpose ();
			rot.transform (normal);
			if (downup && normal.z < 0) normal.z = -normal.z;
			oVel.scaleAdd (-2.0 * normal.dot (iVel), normal, iVel);
		}		
		return new LightRay (new Vector3d (iPoint), oVel, input.getLambda ());
	} // end of reflect ********************************************************

	/** returns the time to intersection */
	public double getTime (LightRay input) {
		intersect (input);
		return this.time;
	}
	/** returns the diameter of this ConicSection */
	public double getSize () { return this.size / SIZE_FACTOR; }

	/** finds the Point3d of intersection between 
	 * the LightRay and this ConicSection.
	 */
	public Point3d intersect (LightRay input) {
		Vector3d pos = input.getPosition ();
		Vector3d dir = input.getVelocity ();
		// Rotate the Coordinate System
		if (angle != 0.0) {
			Transform3D rot = new Transform3D ();
			rot.rotY (-this.angle);
			rot.transform (pos);
			rot.transform (dir);
			dir.normalize ();
		}
		double A = Math.pow (dir.x / a, 2.0) +
			Math.pow (dir.y / b, 2.0) + Math.pow (dir.z / c, 2.0);
		double B = + 2.0 * (pos.z - vertex.z) * dir.z / Math.pow (c, 2.0)
			+ 2.0 * (pos.x - vertex.x) * dir.x / Math.pow (a, 2.0)
			+ 2.0 * (pos.y - vertex.y) * dir.y / Math.pow (b, 2.0);
		double C = -1.0 + Math.pow ((pos.z - vertex.z) / c, 2.0)
			+ Math.pow ((pos.x - vertex.x) / a, 2.0)
			+ Math.pow ((pos.y - vertex.y) / b, 2.0);
		double sqdiscr = Math.sqrt (Math.pow (B, 2.0) - 4 * A * C);
		if (A == 0)
			time = - C / B;
		if (A != 0) {
			if (B >= 0)
				time = (-B + sqdiscr) / 2.0 / A;
			if (B < 0)
				time = (-B - sqdiscr) / 2.0 / A;
		}
		/* Upward going rays want + whereas downward want -
		 * Has to do with being inside or outside of hyperbola
		 * Look at the sign of dir.z to determine
		 */
		if ((dir.z > 0 && !downup) || // Up
			(dir.z < 0 &&  downup) )
			time = (-B + sqdiscr) / 2.0 / A;
		if ((dir.z < 0 && !downup) || // Down
			(dir.z > 0 &&  downup) )
			time = (-B - sqdiscr) / 2.0 / A;
		if (time < TIME_LIMIT || 
			Double.isNaN (time) ) {
			// Then you can only get there by going backwards.  Too bad!
			time = 0;
		}

		// Propagate the LightRay till intersect
		// this = s*t1 + t2).;
		Point3d intersection = new Point3d ();
		intersection.scaleAdd (this.time,
							   input.getVelocity (),
							   input.getPosition ());
		return intersection;
	} // end of intersect ******************************************************

	/** Returns a focus of this ConicSection */
	public Point3d getFocus (int i) {
		double f = Math.sqrt ( c * c - a * a );
		if (i == 0) {
			Point3d f0 = new Point3d (vertex.x, vertex.y, vertex.z + f);
			Transform3D rot = new Transform3D ();
			rot.rotY (this.angle);
			rot.transform (f0);
			return f0;
		}
		if (i == 1) {
			Point3d f0 = new Point3d (vertex.x, vertex.y, vertex.z - f);
			Transform3D rot = new Transform3D ();
			rot.rotY (this.angle);
			rot.transform (f0);
			return f0;
		}
		throw new RuntimeException ("Ellipsoid getFocus Error:" +
									" Ellipsoids only have two focii.");
	}
} // end of Class Ellipsoid ****************************************************
