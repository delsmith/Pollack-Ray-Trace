package jray;

import javax.vecmath.*;

// import jray.EllipticGrating;

/** Spherical Grating disperses light
 * @deprecated as of the creation of this class
 * @see jray.EllipticGrating
 */
public class SphericGrating extends Spheroid implements Grating {

	private double cd; // in nm
	private Vector3d cg;
	private int order;
	private double xcenter;
	private Vector3d crossg;

	private Vector3d ncenter;

	/** A Spherical Grating
	 * @param v position vector of the vertex of the grating
	 * @param R radius of curvature
	 * @param s diameter of grating (it's a sphere!) (optionally ls and rs)
	 * @param linespacing line-spacing = # lines / mm
	 * @param g a vector in the direction of lines on grating at center
	 * @param m and possibly the order of dispersed light this grating is blazed for
	 */
	public SphericGrating (Point3d v, double R, double s,
						 double linespacing, Vector3d g) {
		this(v, R, s/2.0,s/2.0, linespacing, g, DEFAULT_ORDER);
	}
	public SphericGrating (Point3d v, double R, double ls, double rs,
						 double linespacing, Vector3d g, int m) {
		super(v, R, ls, rs, 0);
		this.cd = (1e6) / linespacing; // in nm
		this.cg = new Vector3d (g);
		(this.cg).normalize ();
		this.order = m;
		// Physical center of grating
		this.xcenter = vertex.x + 0.5 * (rs - ls);
		// Direction of Normal at the center of the grating
		 ncenter = 
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
		if ( (iPoint.x - vertex.x) > this.size/2.0 ||
			 (iPoint.y - vertex.y) > this.size/2.0 ||
			getTime (input) == 0)
			return new LightRay (new Vector3d (iPoint), iVel, lambda);

		Vector3d normal = 
			new Vector3d (-2*(iPoint.x - vertex.x) / Math.pow (this.a, 2.0),
						  -2*(iPoint.y - vertex.y) / Math.pow (this.b, 2.0),
						  -2*(iPoint.z - vertex.z) / Math.pow (this.c, 2.0));
		normal.normalize ();
	
		// local linespacing on sphere
		double dx = iPoint.x - xcenter;
		double dz = iPoint.z - Evaluate (xcenter);
		double dd = Math.sqrt ( dx*dx + dz*dz );
		double ld = a * ( Math.asin ( (dd + 0.5 * this.cd * 1e-6) / a )
						 -Math.asin ( (dd - 0.5 * this.cd * 1e-6) / a )
						  ) * 1e6; // in nm

		// local direction of grooves
		Vector3d lg = new Vector3d ();
		lg.cross (normal, crossg);

		Vector3d oVel = GratingEqn.grating (m, lambda, ld, lg, normal, iVel);
		return new LightRay (new Vector3d (iPoint), oVel, lambda);
	} // end of reflect ********************************************************

} // end of Class SphericGrating
