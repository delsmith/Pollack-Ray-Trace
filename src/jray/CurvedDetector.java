package jray;

import jray.DetectorDisplay;

import javax.vecmath.*;

/** A curved detector */
public class CurvedDetector extends Cylinder implements Detector {

	private double z0, cosa, sina;

	private DetectorDisplay display;
	/** construct with a title (for the display),
	 * the location of the detector v, and the radius of curvature R.
	 * s is the linear size of the detector.  The detector is square,
	 * even though the detector is a sphere.
	 */
	public CurvedDetector (String title, Point3d v, double R, double s) {
		this(title, v, R, s/2.0, s/2.0, 0);
	}
	/** construct with a hole in the center of the detector */
	public CurvedDetector (String title, Point3d v, double R,
							double ls, double rs, double h) {
		super(v, R, ls, rs, h, 0.0);
		display = new DetectorDisplay (title, ls + rs);
		sina = (xcenter - vertex.x) / this.R;
		cosa = Math.sqrt (1.0 - sina * sina);
		z0 = (downup) ? -this.R : this.R;
		z0 = z0 * cosa + vertex.z;
	}

	/** Returns the display associated with this detector */
	public DetectorDisplay getDisplay () {return display;}
	/** displays the DetectorDisplay */
	public void display () { display.display (); }
	/** repaints the DetectorDisplay */
	public void repaint () { display.repaint (); }
	/** cleans the DetectorDisplay */
	public void clean () { display.clean (); }

	/** absorbs a LightRay and puts it on the display */
	public LightRay reflect (LightRay input) {
		Point3d iPoint = intersect (input);
		Vector3d iVel = input.getVelocity ();

		if ( Math.abs(iPoint.x - xcenter ) > this.size/2.0 ||
			 Math.abs(iPoint.y - vertex.y) > this.size/2.0 ||
			 getTime (input) == 0 )
			return new LightRay (new Vector3d (iPoint), iVel, input.getLambda ());

		double xb = iPoint.x - vertex.x;
		double yb = iPoint.y - vertex.y;
		double zb = iPoint.z - vertex.z;
		double dx = xb * cosa - zb * sina;
		double dy = yb;

		display.add (new Point2d (dx, dy), input.getColor () );
		return new LightRay ( new Vector3d (iPoint), 
							  new Vector3d (0, 0, 0), input.getLambda ());
	} // end of reflect ********************************************************
} // end of Class CurvedDetector **********************************************
