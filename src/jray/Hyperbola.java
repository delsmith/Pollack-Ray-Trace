package jray;

import java.lang.Math;
import javax.vecmath.*;

/** This is two-dimensional hyperbola
 * intended to get a feeling for ray tracing
 * only working in the xz plane, so y components are not changed
 */
public class Hyperbola implements ConicSection {

	private Point3d vertex; // Location of Vertex
	private Point3d focus;  // Location of Focus
	private double size;    // the diameter of the mirror
	private double c;       // distance from vertex to focus
	private double b;       // parameter
	private double a;       // half the separation between hyperbolae
	private double hole;    // Size of hole in center of hyperbola
	private double time;    // time elapsed from LightRay start to intersection

	/**
	 * The equation of a hyperbola in 2 dimensions is:
	 * x^2 / a^2 - y^2 / b^2 = 1
	 * where b^2 = c^2 - a^2
	 * The required parameters are the 2D vertex, the
	 * distance between the vertex and the 2D focus and the parameter a.
	 */
	public Hyperbola (Point3d v, Point3d f, double a, double s) {
		this (v, f, a, s, 0);
	}
	public Hyperbola (Point3d v, Point3d f, double a, double s, double h) {
		// Create a 2D parabola with the given vertex and focus
		this.vertex = new Point3d (v);
		this.focus = new Point3d (f);
		this.a = a;
		this.size = s;
		this.c = vertex.distance (focus);
		this.hole = h;
		this.time = -1;
		this.b = Math.sqrt ( Math.pow (this.c, 2.0) - Math.pow (this.a, 2.0) );
	}

	/** Evaluate at point x the value z for 2D Rendering */
	public double Evaluate (double x) {
		if ( Math.abs (x - (this.vertex).x) > this.size/2. )
			return ConicSection.NULL;
		else if ( Math.abs (x - (this.vertex).x) < this.hole/2. )
			return ConicSection.BLANK;
		else // Always return the upward hyperbola
			return  (this.vertex).z + this.a *
				Math.sqrt (Math.pow ( (x - (this.vertex).x) /
									  this.b , 2.0) + 1.0 );
	}

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
		if (Evaluate (iPoint.x) == ConicSection.NULL ||
			Evaluate (iPoint.x) == ConicSection.BLANK ||
			this.time == 0)
			oVel = new Vector3d (iVel);
		else {
			// The Gradient of the equation of our ConicSection:
			Vector3d normal = new Vector3d ( -2.0 * (iPoint.x - vertex.x)/b/b,
											 0, 
											 +2.0 * (iPoint.z - vertex.z)/a/a);
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
	public double getSize () { return this.size; }

	/** finds the Point3d of intersection between 
	 * the LightRay and this ConicSection.
	 */
	public Point3d intersect (LightRay input) {
		Vector3d pos = input.getPosition ();
		Vector3d dir = input.getVelocity ();

		double A = - (Math.pow (dir.x / b, 2.0) - Math.pow (dir.z / a, 2.0));
		double B = -2.0 * ( dir.x * (pos.x - vertex.x) / b / b -
							dir.z * (pos.z - vertex.z) / a / a);
		double C = -1.0 - (Math.pow ( (pos.x - vertex.x) / b, 2.0) -
						   Math.pow ((pos.z - vertex.z) / a, 2.0));
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

	/**
	 * Return the other focus of the hyperbola.
	 * I assume you gave me the focus closest to the vertex.
	 * Therefore f + c (v-f).norm is the answer
	 */
	public Point3d getFocus () {
		Vector3d vec = new Vector3d (vertex); // v
		vec.sub (new Vector3d (focus) ); // v - f
		vec.normalize (); // (v-f).norm
		vec.scaleAdd (this.c, vec, new Vector3d (vertex) );
		return new Point3d (vec);
	}
} // end of Class Hyperbola ****************************************************
