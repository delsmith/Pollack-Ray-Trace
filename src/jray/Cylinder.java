package jray;

import javax.vecmath.*;
import javax.media.j3d.Transform3D;

/**
 * Cylinder: circle streched in the y-direction
 */
public class Cylinder implements ConicSection {

	/** location of the vertex of this ConicSection */
	protected Point3d vertex;
	/** The diameter of this cylinder */
	protected double size;
	protected double lsize, rsize;
	/** location of the center of this ConicSection */
	protected double xcenter;
	/** Cylinder radius */
	protected double R;
	/** size of hole in center of ConicSection */
	protected double hole;
	private double time;    // time elapsed from LightRay start to intersection
	/** false = concave down, true = concave up */
	protected boolean downup = false;
	/** angle of rotation about the y-axis */
	protected double angle;

	/**
	 * The equation of a cylinder is
	 * x^2 + z^2 = R^2
	 * The required parameters are the vertex and radius of curvature
	 */
	public Cylinder (Point3d v, double R, double s) {
		this (v, R, s, 0);
	}
	/** Cylinder with a hole in the center */
	public Cylinder (Point3d v, double R, double s, double h) {
		this(v, R, s, h, 0.0);
	}
	public Cylinder (Point3d v, double R, double s, 
					  double h, double angle) {
		this(v, R, s/2.0,s/2.0, h, angle);
	}
	public Cylinder (Point3d v, double R, double ls, double rs,
					  double h, double angle) {
		this.vertex = new Point3d (v);
		this.R = Math.abs (R);
		if (R < 0)
			downup = true;
		this.size = (ls + rs) * SIZE_FACTOR;
		this.lsize = ls;
		this.rsize = rs;
		this.xcenter = vertex.x + 0.5 * (rs - ls);
		this.hole = h;
		if (angle == Math.PI)
			angle = 0;
		this.angle = angle;
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
				return (this.vertex).z - this.R *
					Math.sqrt (1.0 - Math.pow ( (x - (this.vertex).x) / 
												this.R , 2.0));
			else
				return  (this.vertex).z + this.R * 
					Math.sqrt (1.0 - Math.pow ( (x - (this.vertex).x) / 
												this.R , 2.0));
		} else {
			/*
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
			*/
				return ConicSection.NULL;
				//			return z;
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
				new Vector3d (+2.0*(rPoint.x - vertex.x),
							  +2.0*(rPoint.y - vertex.y),
							  +2.0*(rPoint.z - vertex.z) );
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
		Vector3d vel = input.getVelocity ();

		double A = vel.x * vel.x + vel.z * vel.z; 
		double B = 
			+ 2.0 * vel.x * (pos.x - vertex.x)
			+ 2.0 * vel.z * (pos.z - vertex.z);
		double C = -R*R + Math.pow ((pos.z - vertex.z), 2.0)
			+ Math.pow ((pos.x - vertex.x), 2.0);
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
		if ((vel.z > 0 && !downup) || // Up
			(vel.z < 0 &&  downup) )
			time = (-B + sqdiscr) / 2.0 / A;
		if ((vel.z < 0 && !downup) || // Down
			(vel.z > 0 &&  downup) )
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
		throw new RuntimeException ("Cylinder getFocus Error:" +
									" Cylinders have no focii.");
	}
} // end of Class Cylinder ****************************************************
