package jray;

import javax.vecmath.*;

/** Plane Grating disperses light
 */
public class PlaneGrating extends Plane implements Grating {

	private double d;
	private int order;

	/**  A PlaneGrating requires the following inputs:
	 * position vector, normal, linear size of grating (square of course).
	 * line-spacing = # lines / mm, 
	 * and the order of dispersed light this grating is blazed for
	 */
	public PlaneGrating (Point3d v, Vector3d n, double s,
						 double linespacing) {
		this(v, n, s, linespacing, DEFAULT_ORDER);
	}
	public PlaneGrating (Point3d v, Vector3d n, double s,
						 double linespacing, int m) {
		super(v, n, s);
		this.d = (1e6) / linespacing; // in nm
		this.order = m;
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

		/** Simple grating equation */
		double sina = Math.sin (normal.angle (iVel));
		double sinb = (((double) m) * lambda / this.d) - sina;
		Vector3d change = new Vector3d (normal);
		change.scale ( Math.sqrt ( 2.0 * (1.0 - sinb) ));
		Vector3d oVel = new Vector3d (iVel);
		oVel.add (change);
		oVel.x = sinb;
		oVel.y = iVel.y;
		oVel.z = - Math.sqrt ( 1 - oVel.y*oVel.y - oVel.x*oVel.x );
		return new LightRay (new Vector3d (iPoint), oVel, lambda);
	} // end of reflect ********************************************************

} // end of Class PlaneGrating *************************************************
