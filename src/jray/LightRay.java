package jray;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.Color;

import jray.util.Wavelength;

/**
 * LightRay consists of two Vector3d objects and a wavelength.
 * The first represents the position vector of the light beam.  
 * The second represents the velocity vector of the light beam.
 */
public class LightRay {

	private Vector3d position;
	private Vector3d velocity;
	private Vector3d ipos, ivel;
	private double lambda;
	private float gamma = 1.0f;
	private static double DEFAULT_COLOR = 450.0;

	public LightRay () {
		position = new Vector3d (0, 0, 0);
		velocity = new Vector3d (0, 0, 1);
		velocity.normalize ();
		this.lambda = DEFAULT_COLOR;
	}

	/** p = position, v = velocity */
	public LightRay (Vector3d p, Vector3d v) {
		this(p, v, DEFAULT_COLOR);
	}
	/**
	 * @param p position of LightRay
	 * @param v velocity of LightRay
	 * @param wavelength of LightRay (in nm)
	 */
	public LightRay (Point3d p, Vector3d v, double wavelength) {
		this(new Vector3d (p), v, wavelength);
	}
	/** p = position, v = velocity, wavelength in nm */
	public LightRay (Vector3d p, Vector3d v, double wavelength) {
		position = new Vector3d (p);
		velocity = new Vector3d (v);
		velocity.normalize ();
		ipos = new Vector3d (position);
		ivel = new Vector3d (velocity);
		this.lambda = wavelength;
	}
	/** position = (x, y, z), velocity = (xa, ya, za) */
	public LightRay (double x, double y, double z,
					 double xa, double ya, double za) {
		this(x, y, z, xa, ya, za, DEFAULT_COLOR);
	}
	/** position = (x, y, z), velocity = (xa, ya, za), wavelength in nm */
	public LightRay (double x, double y, double z,
					 double xa, double ya, double za,
					 double wavelength) {
		position = new Vector3d (x, y, z);
		velocity = new Vector3d (xa, ya, za);
		velocity.normalize ();
		ipos = new Vector3d (position);
		ivel = new Vector3d (velocity);
		this.lambda = wavelength;
	}
	/** construct a LightRay from the one given. */
	public LightRay (LightRay l) {
		position = new Vector3d (l.getPosition ());
		velocity = new Vector3d (l.getVelocity ());
		velocity.normalize ();
		ipos = new Vector3d (l.getiPos ());
		ivel = new Vector3d (l.getiVel ());
		this.lambda = l.getLambda ();
	}
	/** Returns a new Vector3d of the position */
	public Vector3d getPosition () { return new Vector3d (position); }
	/** Returns a new Vector3d of the velocity */
	public Vector3d getVelocity () { return new Vector3d (velocity); }
	/** returns the initial position of this LightRay */
	public Vector3d getiPos () { return ipos; }
	/** returns the initial velocity of this LightRay */
	public Vector3d getiVel () { return ivel; }

	/** initilize this LightRay */
	public void init () {
		position = new Vector3d (ipos);
		velocity = new Vector3d (ivel);
	}
	/** propagate this LightRay by a time t */
	public void propagate (double t) {
		// t is the proper time elapsed
		position.scaleAdd (t, velocity, position);
	}
	/** get the Color object corresponding to the wavelenght of this light */
	public Color getColor () {
		float w = (float)this.lambda;
		if (this.lambda < 300.0)
			w = (float) (15.0 * (this.lambda - 140.0) + 380.0);
		return Wavelength.wvColor ( w, this.gamma);
	}
	/** get the wavelength in nm of this LightRay */
	public double getLambda () {
		return this.lambda;
	}
	/** get the wavelength in nm of this LightRay */
	public double getWavelength () {
		return getLambda ();
	}
	/** get the gamma factor for the color of this LightRay */
	public float getGamma () { return gamma; }
	/** set the gamma factor for the color of this LightRay */
	public void setGamma (float f) { this.gamma = f; }

	/** overrides toString() */
	public String toString () {
		return ("Position  = " + position + "\n" +
				"Velocity  = " + velocity + "\n" + 
				"Wavelength= " + lambda);
	}
} // end of Class LightRay *****************************************************
