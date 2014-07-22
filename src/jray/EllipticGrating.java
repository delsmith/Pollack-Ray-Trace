package jray;

import javax.vecmath.*;
import javax.media.j3d.Transform3D;

/** Elliptical Grating disperses light
 */
public class EllipticGrating extends Ellipsoid implements Grating {

	private double cd; // in nm
	private Vector3d cg;
	private int order;
	private double xcenter;
	private Vector3d crossg;

	/** A Elliptical Grating requires the following inputs:
	 * position vector of the vertex of the grating,
	 * ellipsoid parameters Point3d(a, b, c),
	 * diameter of grating (it's roughly spherical),
	 * line-spacing = # lines / mm, 
	 * a vector in the direction of lines on grating at center
	 * and possibly the order of dispersed light this grating is blazed for
	 */
	public EllipticGrating (Point3d v, Point3d abc, double s,
						 double linespacing, Vector3d g) {
		this(v, abc, s/2.0,s/2.0, linespacing, g, DEFAULT_ORDER, 0.0);
	}
	public EllipticGrating (Point3d v, Point3d abc, double ls, double rs,
						 double linespacing, Vector3d g, int m, double angle) {
		super(v, abc, ls, rs, 0, angle);
		this.cd = (1e6) / linespacing; // in nm
		this.cg = new Vector3d (g);
		(this.cg).normalize ();
		this.order = m;
		// Direction of Normal at the center of the grating
		Vector3d ncenter = 
			new Vector3d (-2.0*(xcenter - vertex.x) / Math.pow (a, 2.0),
						  -2.0*(0 - vertex.y) / Math.pow (b, 2.0),
						  -2.0*(Evaluate (xcenter)-vertex.z)/Math.pow (c, 2.0));
		ncenter.normalize ();

		if (ncenter.dot (g) != 0)
			throw new RuntimeException ("Normal not perpendicular" + 
										" to the groove direction");
		crossg = new Vector3d ();
		crossg.cross (ncenter, this.cg);
		crossg.normalize ();
	}

	/** returns light reflected into order m */
	public LightRay reflect (LightRay input) {
		return reflect (input, this.order);
	}
	public LightRay reflect (LightRay input, int m) {
		Point3d iPoint = intersect (input);
		Vector3d iVel = input.getVelocity ();
		double lambda = input.getLambda ();
		Point3d  rPoint = new Point3d ();
		Transform3D rot = new Transform3D ();
		rot.rotY (-this.angle);
		rot.transform (iPoint, rPoint);
		double cx = iPoint.x - xcenter;
		double cy = iPoint.y - vertex.y;
		double radius = Math.sqrt ( cx * cx + cy * cy);
		if ( radius > this.size/2.0 ||
			 radius < this.hole/2.0 ||
			 getTime (input) == 0)
			return new LightRay (new Vector3d (iPoint), iVel, lambda);

		Vector3d normal = 
			new Vector3d (-2.0*(rPoint.x - vertex.x) / Math.pow (this.a, 2.0),
						  -2.0*(rPoint.y - vertex.y) / Math.pow (this.b, 2.0),
						  -2.0*(rPoint.z - vertex.z) / Math.pow (this.c, 2.0));
		normal.normalize ();
		if (downup && normal.z < 0) normal.negate ();
		rot.transpose ();
		rot.transform (normal);
		if (downup && normal.z < 0) normal.z = -normal.z;
	
		// local linespacing on sphere
		// assuming cg = (0, 1, 0), then
		double dx = rPoint.x - xcenter;
		double dz = rPoint.z - Evaluate (xcenter);
		double dd = Math.sqrt ( dx*dx + dz*dz );
		double ld = a * ( Math.asin ( (dd + 0.5 * this.cd * 1e-6) / a )
						 -Math.asin ( (dd - 0.5 * this.cd * 1e-6) / a )
						  ) * 1e6; // in nm

		// local direction of grooves
		Vector3d lg = new Vector3d ();
		lg.cross (normal, crossg);
		lg.normalize ();

		Vector3d oVel = GratingEqn.grating (m, lambda, ld, lg, normal, iVel);
		return new LightRay (new Vector3d (iPoint), oVel, lambda);
	} // end of reflect ********************************************************
} // end of Class EllipticGrating
