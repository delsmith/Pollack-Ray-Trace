package jray;

import javax.vecmath.*;

/** Torical Grating disperses light
 */
public class ToricGrating extends Toroid implements Grating {

	private double cd; // in nm
	private Vector3d cg;
	private int order;
	private double xcenter;
	private Vector3d ncenter, crossg;

	/** A Toric Grating, basically a squeezed spherical grating.
	 * @param v position vector of the vertex of the grating,
	 * @param a center of two circle of torus
	 * @param b radius of each cirlce 
	 * @param s diameter of grating (it's roughly spherical),
	 * @param linespacing line-spacing = # lines / mm, 
	 * @param g a vector in the direction of lines on grating at center
	 * @param m order of dispersed light this grating is blazed for
	 * @param angle rotation angle about vertex
	 */
	public ToricGrating (Point3d v, double a, double b, double s,
						 double linespacing, Vector3d g, double angle) {
		this(v, a, b, s, linespacing, g, DEFAULT_ORDER, angle);
	}
	public ToricGrating (Point3d v, double a, double b, double s,
						 double linespacing, Vector3d g, int m, double angle) {
		super(v, a, b, s, angle);
		this.cd = (1e6) / linespacing; // in nm
		this.cg = new Vector3d (g);
		(this.cg).normalize ();
		this.order = m;
		// Direction of Normal at the center of the grating
		ncenter = 
			getNormal ( new Point3d (xcenter, 0, Evaluate (xcenter)) );
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
		Point3d  iPoint = intersect (input);
		Point3d  rPoint = new Point3d (iPoint);
		Vector3d iVel   = input.getVelocity ();
		toTorus.transform (rPoint);
		double lambda = input.getLambda ();
		double radius = Math.sqrt (rPoint.x*rPoint.x+ rPoint.y*rPoint.y);
		if ( radius > this.size/2.0 ||
			 getTime (input) == 0)
			return new LightRay (iPoint, iVel, lambda);

		// local linespacing on sphere
		// assuming cg = (0, 1, 0), then
		double dd = rPoint.x;
		double ld = b * ( Math.asin ( (dd + 0.5 * this.cd * 1e-6) / b )
						 -Math.asin ( (dd - 0.5 * this.cd * 1e-6) / b )
						  ) * 1e6; // in nm

		// local direction of grooves
		Vector3d normal = getNormal (iPoint);
		Vector3d lg = new Vector3d ();
		lg.cross (normal, crossg);
		lg.normalize ();

		Vector3d oVel = GratingEqn.grating (m, lambda, ld, lg, normal, iVel);
		return new LightRay (new Vector3d (iPoint), oVel, lambda);
	} // end of reflect ********************************************************
} // end of Class ToricGrating
