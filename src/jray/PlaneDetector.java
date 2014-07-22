package jray;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.media.j3d.*;
import javax.vecmath.*;

/** This is a Two Dimensional Detector
 * The purpose is to absorb photons, i.e., LightRays
 */
public class PlaneDetector extends Plane implements Detector {

	private DetectorDisplay display;

	/** construct with a title (for the Display),
	 * the location of the detector, and the normal to the surface.
	 * s is the linear size of the detector.  The detector is square.
	 */
	public PlaneDetector (String title, Point3d v, Vector3d n, double s) {
		super(v, n, s);
		display = new DetectorDisplay (title, s);
	}

	/** Returns the display associated with this detector */
	public DetectorDisplay getDisplay () {return display;}

	/** absorbs a LightRay and puts it on the display */
/*
 * NOTE: Del Smith (26.Jul.05) 
 * This function needs to check Absolute Value of distance from Vertex to Point
 */
	
	public LightRay reflect (LightRay input) {
		Point3d iPoint = intersect (input);
		Vector3d iVel = input.getVelocity ();
		// Check to see if point needs to be reflected ( A Plane Detector is SQUARE!)
		if (!VERTICAL) {
			if ( Math.abs(iPoint.x - vertex.x) > this.size/2.0 ||
				 Math.abs(iPoint.y - vertex.y) > this.size/2.0 ||
				 getTime (input) == 0 
				 /* || this.normal.dot (iVel) > 0 -- absorb on either face? Del S. */   ) 
			{
				return new LightRay (new Vector3d (iPoint), iVel, input.getLambda ());
			}
		} 
		else {
			if ( Math.abs(iPoint.z - vertex.z) > this.size/2.0 ||
				getTime (input) == 0) {
				return new LightRay (new Vector3d (iPoint),
						 iVel, input.getLambda ());
				
			}
		}

		/** A true plane would reflect the light.
		 * This Detector will absorb the light
		 * Every point absorbed is added to the display;
		 */
//		double nz = (this.normal).dot (new Vector3d (0, 0, 1));
		double nx = (this.normal).dot (new Vector3d (1, 0, 0));
		double ny = (this.normal).dot (new Vector3d (0, 1, 0));
		double x, y;
		if (!VERTICAL) {
			Matrix3d yrot = new Matrix3d ();
			yrot.rotY (-Math.asin (nx));
			Matrix3d xrot = new Matrix3d ();
			xrot.rotX ( Math.asin (ny));
			Matrix3d frot = new Matrix3d ();
			frot.mul (yrot, xrot);
			Transform3D trot = new Transform3D(frot, new Vector3d (), 1.0);
			Vector3d a = new Vector3d (iPoint);
			a.sub (new Vector3d (vertex));
			trot.transform(a);
			a.add (new Vector3d (vertex));
			x = (a.x - vertex.x);
			y = (a.y - vertex.y);
		} else { // Ignoring the case where normal != (+-1, 0, 0)
			x = (iPoint.z - vertex.z);
			y = (iPoint.y - vertex.y);
		}

		display.add (new Point2d (x, y), input.getColor () );
		return new LightRay ( new Vector3d (iPoint),
							  new Vector3d (0, 0, 0), input.getLambda ());
	} // end of reflect ********************************************************

	/** displays the DetectorDisplay */
	public void display () { display.display (); }
	/** repaints the DetectorDisplay */
	public void repaint () { display.repaint (); }
	/** cleans the DetectorDisplay */
	public void clean () { display.clean (); }

} // end of Class PlaneDetector ************************************************
