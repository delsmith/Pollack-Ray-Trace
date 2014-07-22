package jray;

import javax.vecmath.*;
import javax.media.j3d.*;

/** The Grating Equation */
public class GratingEqn {
	public static double ROUNDERR = 1e-17;
	/** GratingEqn is a static class */
	public GratingEqn () {}
	/**
	 * The grating equation in all generality.  Modeled on 
	 * Jim Green's grating code, we transform coordinates so
	 * that n is in the y-direction and g is in the z-direction.
	 * Then the x-component of the incident vector is shifted
	 * by the simple grating equation, the z-component is
	 * preserved, and the y-component is given by normalization.
	 * 
	 * @param	m	order of diffration desired
	 * @param	l	wavelength of light (in some units)
	 * @param	d   distance between grooves on grating (same units as l)
	 * @param	g	vector in the direction of the grooves on the grating
	 * @param 	n	normal vector at the point of reflection
	 * @param	i	incident vector at the grating
	 * @return	outgoing diffracted vector in order desired.
	 */   
	public static Vector3d grating (double m, double l, double d, 
									Vector3d g, Vector3d n, Vector3d i) {
		Vector3d o = new Vector3d ();
		n.normalize ();
		g.normalize ();

		/* Goldstein p146
		 * Transform from basic to xx coordinates
		 * in xx coordinates, g || z, n || y
		 */
		double a = Math.sqrt (1.0 - g.z*g.z);
		double xn1 = (n.x*g.y - n.y*g.x) / a;
		double xn2 = g.z*(n.x*g.x + n.y*g.y)/a - a*n.z;

		Matrix3d mR = 
			new Matrix3d ( ( xn2*g.y-xn1*g.z*g.x)/a,
						   (-xn2*g.x-xn1*g.y*g.z)/a, xn1*a, 
						   ( xn1*g.y+xn2*g.z*g.x)/a, 
						   (-xn1*g.x+xn2*g.y*g.z)/a, -a*xn2,
						   g.x, g.y, g.z );

		Transform3D R = new Transform3D (mR, new Vector3d (0,0,0), 1.0);
		Vector3d xxi = new Vector3d (i);
		R.transform (xxi);
		xxi.normalize ();
		if (xxi.x > 0) {
			g.negate ();
			return grating (m, l, d, g, n, i);
		}
		o.x = (m * l / d) + xxi.x;
		o.z = xxi.z;
		o.y = Math.sqrt (1.0 - o.x*o.x - o.z*o.z);

		R.transpose ();
		R.transform (o);
		return o;
	}
} // end of Class GratingEqn *************************************************
