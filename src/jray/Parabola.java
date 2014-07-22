package jray;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/** This is a two dimensional parabola
 * The purpose is to get familiar with ray tracing
 * and 2D rendering of the optical bench
 */
public class Parabola implements ConicSection {

	private Point3d vertex; // Location of Vertex
	private Point3d focus;  // Location of Focus
	private double size;    // the diameter of the mirror
	private double p;       // distance from vertex to focus
	private double hole;    // Size of hole in center of parabola
	private double time;    // time elapsed from LightRay start to intersection

	/**
	 * The equation of a parabola in 2 dimensions is:
	 *  (y - y0) = (x - x0)^2 / 4 p
	 * The required parameters are the 2D vertex, and the
	 * distance between the vertex and the 2D focus, or directrix.
	 * In this case we want the focus since this is where the light
	 * is focused.
	 */
	public Parabola (Point3d v, Point3d f, double s) {
		this (v, f, s, 0);
	}
	/** with a hole in the center of this ConicSection */
	public Parabola (Point3d v, Point3d f, double s, double h) {
		// Create a 2D parabola with the given vertex and focus
		this.vertex = new Point3d (v);
		this.focus = new Point3d (f);
		this.size = s;
		this.p = vertex.distance (focus);
		this.hole = h;
		this.time = -1;
	}

	/** Evaluate at point x the value z for 2D Rendering */
	public double Evaluate (double x) {
		if ( Math.abs (x - (this.vertex).x) > this.size/2. )
			return ConicSection.NULL;
		else if ( Math.abs (x - (this.vertex).x) < this.hole/2. )
			return ConicSection.BLANK;
		else 
			return Math.pow ((x - (this.vertex).x), 2.0) /
				4.0 / this.p + (this.vertex).z;
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
			Vector3d normal = 
				new Vector3d ( -(iPoint.x - vertex.x)/ 2.0 / this.p, 0, 1.0 );
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
		/* Solve a quadratic for the time of intersection */
		Vector3d pos = input.getPosition ();
		Vector3d dir = input.getVelocity ();

		double A = Math.pow (dir.x, 2.0);
		double B = 2.0 * (pos.x - vertex.x) * dir.x - 4 * p * dir.z;
		double C = - 4.0 * p * (pos.z - vertex.z) + 
			Math.pow ((pos.x - vertex.x), 2.0);
		double sqdiscr = Math.sqrt (Math.pow (B, 2.0) - 4 * A * C);
		if (A == 0)
			time = - C / B;
		if (A != 0) {
			if (B >= 0)
				time = (-B + sqdiscr) / 2.0 / A;
			if (B < 0)
				time = (-B - sqdiscr) / 2.0 / A;
		}
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
}
