package jray;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Hyperboloid of two sheets about the z-axis
 */
public class Hyperboloid implements ConicSection {

	private Point3d vertex; // Location of Vertex
	private double size;    // the diameter of the mirror
	private double c;       // distance from vertex to focus
	private double a, b;    // Hyperboloid parameters
	private double hole;    // Size of hole in center of hyperbola
	private double time;    // time elapsed from LightRay start to intersection

	/**
	 * The equation of a hyperboloid of two sheets is
	 * - (x/a)^2 - (y/b)^2 + (z/c)^2 = 1
	 * The required parameters are the vertex and a point3d with (a, b, c)
	 */
	public Hyperboloid (Point3d v, Point3d abc, double s) {
		this (v, abc, s, 0);
	}
	/** with a hole in the center of this ConicSection */
	public Hyperboloid (Point3d v, Point3d abc, double s, double h) {
		this.vertex = new Point3d (v);
		this.a = abc.x;
		this.b = abc.y;
		this.c = abc.z;
		this.size = s * SIZE_FACTOR;
		this.hole = h;
		this.time = -1;
	}

	/** Evaluate at point x the value z for 2D Rendering */
	public double Evaluate (double x) {
		if ( Math.abs (x - (this.vertex).x) > this.size/2. )
			return ConicSection.NULL;
		else if ( Math.abs (x - (this.vertex).x) < this.hole/2. )
			return ConicSection.BLANK;
		else // Always return the upward hyperbola
			return  (this.vertex).z + this.c * 
				Math.sqrt (1.0 + Math.pow ( (x - (this.vertex).x)
											/ this.a , 2.0));
	}

	/** 
	 * Returns a LightRay at the surface of the
	 * ConicSection in the outgoing reflected direction.
	 */
	public LightRay reflect (LightRay input) {
		Point3d iPoint = intersect (input);
		Vector3d iVel = input.getVelocity ();
		Vector3d oVel = new Vector3d ();
		// Check to see if point needs to be reflected
		double dx = iPoint.x - vertex.x;
		double dy = iPoint.y - vertex.y;
		double radius = Math.sqrt ( dx * dx + dy * dy);
		if ( radius > this.size/2.0 ||
			 radius < this.hole/2.0 ||
			 this.time == 0)
			oVel = new Vector3d (iVel);
		else {
			// The Gradient of the equation of our ConicSection:
			Vector3d normal = 
				new Vector3d ( -2.0*(iPoint.x-vertex.x)/Math.pow(this.a,2.0),
							   -2.0*(iPoint.y-vertex.y)/Math.pow(this.b,2.0),
							   +2.0*(iPoint.z-vertex.z)/Math.pow(this.c,2.0));
			normal.normalize ();
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

		double A = Math.pow (dir.z / c, 2.0) -
			(Math.pow (dir.x / a, 2.0) + Math.pow (dir.y / b, 2.0));
		double B = + 2.0 * (pos.z - vertex.z) * dir.z / Math.pow (c, 2.0)
			- 2.0 * (pos.x - vertex.x) * dir.x / Math.pow (a, 2.0)
			- 2.0 * (pos.y - vertex.y) * dir.y / Math.pow (b, 2.0);
		double C = -1.0 + Math.pow ((pos.z - vertex.z) / c, 2.0)
			- Math.pow ((pos.x - vertex.x) / a, 2.0)
			- Math.pow ((pos.y - vertex.y) / b, 2.0);
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
		if (dir.z > 0) // Up
			time = (-B + sqdiscr) / 2.0 / A;
		if (dir.z < 0) // Down
			time = (-B - sqdiscr) / 2.0 / A;
		if (time < TIME_LIMIT || 
				Double.isNaN (time)) {
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
		double f = Math.sqrt ( a * a + c * c );
		if (i == 0)
			return new Point3d (vertex.x, vertex.y,
								vertex.z + f);
		if (i == 1)
			return new Point3d (vertex.x, vertex.y,
								vertex.z - f);
		throw new RuntimeException ("Hyperboloid getFocus Error:" +
									" Hyperboloids only have two focii.");
	}

	public String toString () {
		return new String ("Hyperboloid with vertex=" + vertex +
						   ", abc=(" + a + ", " + b + ", " + c +
						   "), D=" + size + ", hole=" + hole);
	}
} // end of Class Hyperboloid **************************************************
