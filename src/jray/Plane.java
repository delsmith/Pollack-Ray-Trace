package jray;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * This is a Relector Plane
 */
public class Plane implements ConicSection {

	/** location of plane */
	protected Point3d vertex;
	/** the normal to the plane */
	protected Vector3d normal;
	/** diameter of plane, it is square of course */
	protected double size;
	private   double time; // time elapsed from LightRay start to intersection
	/** Zero = vertex (dot) normal */
	protected double zero;
	/** projection length of plane onto the x-axis */
	protected double proj;
	/** a completely vertical plane */
	protected boolean VERTICAL;
	/** size of hole in center of plane */
	protected double hole;

	/**
	 * The equation of a plane is:
	 * a (x - x0) + b (y - y0) + c (z - z0) = d
	 * All that is required is the center of the plane
	 * and the normal vector (a, b, c).  A point P is on the
	 * plane if n . (P - \vec{x0} ) = 0
	 */
	public Plane (Point3d v, Vector3d n, double s) {
		this(v, n, s, 0.0);
	}
	public Plane (Point3d v, Vector3d n, double s, double h) {
		this.vertex = new Point3d (v);
		this.normal = new Vector3d (n);
		normal.normalize ();
		this.zero = normal.dot (new Vector3d (vertex));
		this.size = s;
		this.time = -1;
		this.proj = size / 2.0 * normal.dot (new Vector3d (0, 0, 1));
		this.VERTICAL = false;
		if (this.proj == 0) {
			this.proj = size / 2.0 * normal.dot (new Vector3d (1, 0, 0));
			this.VERTICAL = true;
		}
		this.proj = Math.abs (this.proj);
		this.hole = h;
	}

	/** 2D Rendering: Project to xz-plane, i.e., ignore y-component */
	public double Evaluate (double x) {
		if (!VERTICAL) {
			if ( Math.abs(x - vertex.x) > proj )
				return ConicSection.NULL;
			else if ( Math.abs (x - vertex.x) < this.hole/2. )
				return ConicSection.BLANK;
			else
				return vertex.z - normal.x/normal.z * (x - vertex.x);
		} else {
			// A vertical plane
			if ( Math.abs (x - vertex.z) > size / 2.0 )
				return ConicSection.NULL;
			else if (Math.abs (x - vertex.z) < this.hole/2. )
				return ConicSection.BLANK;
			else
				return vertex.x - normal.z/normal.x * (x - vertex.z);
		}
	}

	/** reflects a LightRay at the surface of the plane */
	public LightRay reflect (LightRay input) {
		Point3d iPoint = intersect (input);
		Vector3d iVel = input.getVelocity ();
		Vector3d oVel = new Vector3d ();
		// Check to see if point needs to be reflected
		// It's a circular plane, with a circular hole
		double dx = (iPoint.x - vertex.x);
		double dy = (iPoint.y - vertex.y);
		double radius = Math.sqrt (dx*dx + dy*dy);
		if (radius >  this.size/2.0 ||
			radius <= this.hole/2.0 ||
			this.time == 0)
			oVel = new Vector3d (iVel);
		else {
			// You gave us the direction of the normal.  Therefore:
			oVel.scaleAdd (-2.0 * normal.dot (iVel), normal, iVel);
		}
		return new LightRay (new Vector3d (iPoint), oVel, input.getLambda ());
	} // end of reflect ********************************************************

	/** returns the time to intersection of the plane */
	public double getTime (LightRay input) {
		intersect (input);
		return this.time;
	}
	/** returns the linear size of this plane */
	public double getSize () { return this.size; }

	/** returns whether or not this plane is vertical */
	public boolean isVertical () { return this.VERTICAL; }

	/** finds the Point3d of intersection between 
	 * the LightRay and this ConicSection.
	 */
	public Point3d intersect (LightRay input) {
		Vector3d pos = input.getPosition ();
		Vector3d dir = input.getVelocity ();

			// This is easy
		if (!VERTICAL)
			time = (zero - normal.dot (pos) ) / normal.dot (dir);
		else {
			time = (vertex.x - pos.x ) / dir.x;
		}
		if (time < TIME_LIMIT || Double.isNaN (time) ||
			Double.isInfinite (time)) {
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
	}
} // end of Class Plane ********************************************************
