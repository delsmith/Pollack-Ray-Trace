package jray;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.media.j3d.Transform3D;

/**
 * Paraboloid, a parabola of revolution
 */
public class Paraboloid implements ConicSection {

	private Point3d vertex; // Location of Vertex
	private double size;    // the diameter of the mirror
	private double a, b, c; // Paraboloid parameters
	private double hole;    // Size of hole in center of parabola
	private double time;    // time elapsed from LightRay start to intersection
	private double angle;   // angle of rotation about y-axis

	/**
	 * The equation of a paraboloid is:
	 *  z / c = (x / a)^2 + (y / b)^2
	 * The required parameters are the vertex and focus.
	 * From the focus we can not uniquely determine the parameters a, b, c
	 * Therefore the second input should be a point3d with (a, b, c)
	 */
	public Paraboloid (Point3d v, Point3d abc, double s) {
		this (v, abc, s, 0);
	}
	/** with a hole in the center of this ConicSection */
	public Paraboloid (Point3d v, Point3d abc, double s, double h) {
		this(v, abc, s, h, 0.0);
	}
	public Paraboloid (Point3d v, Point3d abc, double s,
					   double h, double angle) {
		this.vertex = new Point3d (v);
		this.a = abc.x;
		this.b = abc.y;
		this.c = abc.z;
		this.size = s * SIZE_FACTOR;
		this.hole = h;
		if (angle == Math.PI) {
			angle = 0.0;
			this.c = -this.c;
		}
		this.angle = angle;
		this.time = -1;
	}

	/** Evaluate at point x the value z for 2D Rendering */
	public double Evaluate (double x) {
		if ( Math.abs (x - (this.vertex).x) > this.size/2. )
			return ConicSection.NULL;
		else if ( Math.abs (x - (this.vertex).x) < this.hole/2. )
			return ConicSection.BLANK;
		else if (angle == 0.0)
			return Math.pow ( (x - (this.vertex).x) / this.a, 2.0) * 
				this.c + (this.vertex).z;
		else {
			double dx = x - vertex.x;
			double sa = Math.sin (angle);
			double ca = Math.cos (angle);
			double cot = ca / sa;
			double z = -dx * cot + a * a * cot / 2.0 / c / sa -
				1.0 / sa / sa / 2.0 / c * 
				Math.sqrt ( Math.pow (a*a*ca, 2.0) -
							4.0 * a*a*c*dx*sa) + vertex.z;
			if (Double.isNaN (z) || Double.isInfinite (z))
				return ConicSection.NULL;
			return z;
		}
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

		double dx = iPoint.x - vertex.x;
		double dy = iPoint.y - vertex.y;
		double radius = Math.sqrt ( dx * dx + dy * dy);
		if ( radius > this.size/2.0 ||
			 radius < this.hole/2.0 ||
			 this.time == 0) {
			oVel = new Vector3d (iVel);
		} else {
			// The Gradient of the equation of our ConicSection:
			Vector3d normal = 
				new Vector3d (-2.0*(iPoint.x-vertex.x)/Math.pow(this.a,2.0),
							  -2.0*(iPoint.y-vertex.y)/Math.pow(this.b,2.0),
							  1.0 / this.c);
			if (angle != 0.0) {
				Transform3D rot = new Transform3D ();
				rot.rotY (-this.angle);
				rot.transform (normal);
			}
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
		/* Solve a quadratic for the time of intersection */
		Vector3d pos = input.getPosition ();
		Vector3d dir = input.getVelocity ();
		// Rotate the Coordinate System
		if (angle != 0.0) {
			Transform3D rot = new Transform3D ();
			rot.rotY (this.angle);
			rot.transform (pos);
			rot.transform (dir);
			dir.normalize ();
		}

		double A = -(Math.pow (dir.x / a, 2.0) + Math.pow (dir.y / b, 2.0));
		double B = - 2.0 * (pos.x - vertex.x) * dir.x / Math.pow (a, 2.0) +
			dir.z / c - 2.0 * (pos.y - vertex.y) * dir.y / Math.pow (b, 2.0);
		double C = - Math.pow ((pos.x - vertex.x)/ a, 2.0) +
			(pos.z - vertex.z) / c - Math.pow ((pos.y - vertex.y) / b, 2.0);
		double discr = B * B - 4 * A * C;
		if (discr < 0.0)
			throw new RuntimeException("Paraboloid.Intersect is infeasible!");
		double sqdiscr = Math.sqrt (discr);

		if (A == 0 || Math.abs (B) == sqdiscr )
			time = - C / B;
		if (A != 0 && Math.abs (B) != sqdiscr) {
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

	/** returns the focus in the X dimension */
	public Point3d getXFocus () { 
		Point3d f =  new Point3d (vertex.x, vertex.y,
								  vertex.z + a * a / c / 4.0);
		Transform3D rot = new Transform3D ();
		rot.rotY (-this.angle);
		rot.transform (f);
		return f;
	}
	/** returns the focus in the Y dimension */
	public Point3d getYFocus () { 
		Point3d f = new Point3d (vertex.x, vertex.y,
								 vertex.z + b * b / c / 4.0); 
		Transform3D rot = new Transform3D ();
		rot.rotY (-this.angle);
		rot.transform (f);
		return f;
	}
	/** overrids toString() */
	public String toString () {
		return new String ("Paraboloid with vertex=" + vertex +
						   ", abc=(" + a + ", " + b + ", " + c +
						   "), D=" + size + ", hole=" + hole);
	}
} // end of Class Paraboloid ***************************************************
