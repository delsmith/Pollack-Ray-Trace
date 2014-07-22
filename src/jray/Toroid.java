package jray;

import java.util.Vector;

import javax.vecmath.*;
import javax.media.j3d.Transform3D;

/**
 * Toroid or Torus
 */
public class Toroid implements ConicSection {

	private static double CHK_MAX = 1e9;

	/** location of the vertex of this ConicSection */
	protected Point3d vertex;
	/** toroid radii: two circles of radius b centered at +- a */
	protected double a, b;
	/** The diameter of this ellipsoid */
	protected double size;
	/** time elapsed from LightRay start to intersection */
	private double time;
	/** angle of rotation about the y-axis */
	protected double angle;

	/** The Global Transform3D for this object */
	protected Transform3D toWorld, toTorus;
	/** collection of points for the Evaluate function */
	private Vector<Point2d> points;
	/** finness of said points in the x-direction */
	private int scale = 10;

	/**
	 * The equation of a torid is
	 *  ( sqrt(y^2 + z^2) - a )^2 + x^2 = b^2
	 * or
	 *  ((y^2 + z^2 - a)^2 + x^2 - b^2)*((y^2 + z^2 + a)^2 + x^2 - b^2) = 0
	 * yields
	 *  z = (-/+)sqrt( b^2 + a^2 - y^2 - x^2 + (+/-) 2 a sqrt(b^2 - x^2) )
	 *       
	 * @param v	vertex of the toroid
	 * @param a center of two circles
	 * @param b radius of each circle
	 * @param s linear size of torus
	 * @param angle rotation angle about vertex
	 */
	public Toroid (Point3d v, double a, double b, double s, double angle) {
		this.vertex = new Point3d (v);
		this.a = a;
		this.b = b;
		this.size = s * SIZE_FACTOR;
		this.time = -1;
		this.angle = angle;

		// Create Transform3D object for this Toroid
		Vector3d tvec = new Vector3d (v);
		tvec.negate ();
		Matrix3d mid = new Matrix3d ();
		mid.setIdentity ();
		Matrix3d mrt = new Matrix3d ();
		mrt.rotY (-angle);
		Transform3D translate = new Transform3D (mid, tvec, 1.0);
		Transform3D rotate = new Transform3D (mrt, new Vector3d (), 1.0);
		toTorus = new Transform3D ();
		//(this = t1*t2)
		toTorus.mul (rotate, translate);
		toTorus.mul (new Transform3D (mid, new Vector3d (0,0,-(a+b)), 1.0),
					 toTorus);
		toWorld = new Transform3D ();
		toWorld.invert (toTorus);
		createPoints ();
	}

	/** Evaluate at point x the value z for 2D Rendering */
	public double Evaluate (double x) {
		double z = ConicSection.NULL;
		for (int i = 0; i < scale * size - 1; i++) {
			Point2d p1 = (Point2d) points.get (i);
			Point2d p2 = (Point2d) points.get (i+1);
			if (x > p1.x && x <= p2.x) {
				z = 0.5 * (p1.y + p2.y);
				break;
			}
		}
		if (Double.isNaN (z) || Double.isInfinite (z))
			return ConicSection.NULL;
		return z;
	} // end of Evaluate *******************************************************

	private void createPoints () {
		points = new Vector<Point2d> ();
		Matrix3d mid = new Matrix3d ();
		mid.setIdentity ();
		Matrix3d mrt = new Matrix3d ();
		mrt.rotY (angle);
		Transform3D lookAt = new Transform3D ();
		lookAt.mul (new Transform3D (mid, new Vector3d (vertex), 1.0),
					new Transform3D (mrt, new Vector3d (), 1.0));
		for (int i = 0; i < scale * size; i ++) {
			double x = 0.5 * size * (-1.0 + (2.0 * i) / (scale * size - 1.0));
			double z = b - Math.sqrt (b*b - x*x);
			Point3d p = new Point3d (x, 0, z);
			lookAt.transform (p);
			Point2d p2d = new Point2d (p.x, p.z);
			points.add (p2d);
		}
	}

	/** Returns the normal vector at the specified point */
	protected Vector3d getNormal (Point3d p) {
		Point3d tp = new Point3d (p);
		toTorus.transform (tp);
		double fp = 1.0 + a / Math.sqrt (tp.y*tp.y + tp.z*tp.z);
		double fm = 1.0 - a / Math.sqrt (tp.y*tp.y + tp.z*tp.z);
		Vector3d n = new Vector3d (tp.x, tp.y * fp, tp.z * fm);
		n.normalize ();
		if (n.z < 0) n.negate ();
		toWorld.transform (n);
		return n;
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
		Point3d  rPoint = new Point3d (iPoint);
		toTorus.transform (rPoint);
		double cx = rPoint.x;
		double cy = rPoint.y;
		double radius = Math.sqrt ( cx * cx + cy * cy);
		if ( radius > this.size/2.0 ||
			 this.time == 0)
			oVel = new Vector3d (iVel);
		else {
			Vector3d normal = getNormal (iPoint);
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
		Point3d pos = new Point3d (input.getPosition ());
		Vector3d vel = new Vector3d (input.getVelocity ());
		toTorus.transform (pos);
		toTorus.transform (vel);

		/** Mathematica does wonders.  Here are the 
		 * coefficients for our 4th order polynomial in t
		 */

		double [] c = {0,0,0,0,0};
		double [] s = {-1,-1,-1,-1};

		double gp = vel.x * pos.x + vel.y * pos.y + vel.z * pos.z;
		double pp = pos.x * pos.x + pos.y * pos.y + pos.z * pos.z;
		double g2 = pos.x*vel.x*pos.y*vel.y +
			pos.x*vel.x*pos.z*vel.z + 
			pos.y*vel.y*pos.z*vel.z;
//		double tz = vel.z * pos.z;
		c[4] = 1.0;
		c[3] = 4.0 * gp;
		c[2] = 2.0 * ( -b*b+ a*a*(2*vel.x*vel.x - 1.0) + 4 * g2
					   + vel.x*vel.x * (2*pos.x*pos.x + pp)
					   + vel.y*vel.y * (2*pos.y*pos.y + pp)
					   + vel.z*vel.z * (2*pos.z*pos.z + pp) );
		c[1] = 4.0 * ( gp * (pp - b*b) + a*a * (2 * vel.x * pos.x - gp) );
		c[0] = a*a*a*a + Math.pow (b*b - pp, 2.0) - 2 * a * a *
			(b*b + pp - 2 * pos.x * pos.x);

		/** use GraphGem's Solve Quartic to find roots */
		int num = new Roots ().SolveQuartic (c, s);
		if (num == 0) // no roots
			this.time = 0;
		else {
			double best = checkTime (c, s[0]);
			if (s[0] > TIME_LIMIT || Double.isNaN (time))
				this.time = s[0];
			for (int i = 1; i < num; i++) {
				double chk = checkTime (c, s[i]);
				if (best > chk) {
					best = chk;
					this.time = s[i];
				}
			}
			if (best == CHK_MAX)
				this.time = 0.0;
			for (int i = 0; i < num; i++)
				if (this.time < s[i]) this.time = s[i];
		}

		// Propagate the LightRay till intersect
		// this = s*t1 + t2).;
		Point3d intersection = new Point3d ();
		intersection.scaleAdd (this.time,
							   input.getVelocity (),
							   input.getPosition ());
		return intersection;
	} // end of intersect ******************************************************

	/** Checks the Solution of Time: returns true for a match */
	private double checkTime (double [] c, double sol) {
		if (sol == 0.0 || sol < TIME_LIMIT)
			return CHK_MAX;

		double sum = 0.0;
		for (int i = 0; i <= 4; i++)
			sum += c[i] * Math.pow (sol, i);
		return sum;
	}

	/** Returns a focus of this ConicSection */
	public Point3d getFocus (int i) {
		System.out.println ("Toroid getFocus Error:" +
							" Toroids have no focii.");
		return new Point3d (vertex);
	} // end of getFocus
} // end of Class Toroid ****************************************************


/*
				System.out.println ("SUM : " + sum);
				sum = 0.0;
				for (int i = 0; i <= 4; i++) {
					System.out.println (sum + " " + c[i] + " "+ Math.pow (this.time, i));
				sum += c[i] * Math.pow (this.time, i);
				}
				System.out.println (c[0] + " " + c[1] + " " + c[2] + " " + c[3] + " " + c[4]);
				double A, B, C, D, sq_A, p, q, r;
				A = c[ 3 ] / c[ 4 ];
				B = c[ 2 ] / c[ 4 ];
				C = c[ 1 ] / c[ 4 ];
				D = c[ 0 ] / c[ 4 ];
				sq_A = A * A;
				p = - 3.0/8 * sq_A + B;
				q = 1.0/8 * sq_A * A - 1.0/2 * A * B + C;
				r = - 3.0/256*sq_A*sq_A + 1.0/16*sq_A*B - 1.0/4*A*C + D;
				System.out.println (p + " " + q + " " + r);
*/


