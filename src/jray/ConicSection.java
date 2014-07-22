package jray;

import javax.vecmath.Point3d;

/** Generalized ConicSection */
public interface ConicSection {

	/** Time elapses less than TIME_LIMIT are zero */
	public static final double TIME_LIMIT = 1e-10;
	/** Return value for when point lies outside of dimension of ConicSection */
	public static final double NULL  =  14252.0;
	/** Return value for when point lies inside hole of ConicSection */
	public static final double BLANK = -14252.0;
	/** Dimension of ConicSecion is SIZE_FACTOR larger than given */
	public static final double SIZE_FACTOR = 1.01;

	/**
	 * Returns a LightRay at the surface of the
	 * ConicSection in the outgoing reflected direction.
	 */
	public LightRay reflect (LightRay input);

	/**
	 * Finds the point of intersection between
	 * a LightRay and the ConicSection
	 * Sets the object variable time of intersection.
	 */
	public Point3d intersect(LightRay input);
	/**
	 * Returns the time elapsed to intersection.
	 */
	public double getTime (LightRay input);

	/**
	 * For use in rendering.  Currently doing 2D rendering,
	 * So evaluate at point x the value z
	 */
	public double Evaluate (double x);

	/**
	 * Return the linear size of the conic section.  (Not scaled.)
	 */
	public double getSize ();
}
