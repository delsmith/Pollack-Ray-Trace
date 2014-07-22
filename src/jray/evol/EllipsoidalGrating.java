package jray.evol;

import jray.ConicSection;
import jray.CurvedDetector;
import jray.EllipticGrating;
import jray.Plane;
import jray.PlaneDetector;
import jray.TelePar;
import jray.Telescope;
import jray.telescopes.Cassegrain;
import javax.vecmath.*;

/**
 * Made for use in optimization: Cassegrain telescope with
 * an elliptical grating, and a detector at some position.
 */
public class EllipsoidalGrating extends Cassegrain implements Telescope {

	public final static double FLAT = 4435.93;

	double Drowland = 1000.0; // mm
	double grooves = 3600.; // per mm
	double central = 150. ; // nm central wavelength
	int order = 1;
	double position;
	double radius;
	double rfactor;

	/** p is the position, r the radius of curvature of the detecto,
     *  and f the rFactor of the ellipsoidal grating,
	 *  of this EllipsoidalGrating object */
	public EllipsoidalGrating (TelePar par, double p, double r, double f) {
		this(par, p, r, f, DEFAULT_SIZE);
	}
	public EllipsoidalGrating (TelePar par, double p, double r,
							   double f, double size) {
		super(par);
		this.hasDet = true;
		this.title = "Cassegrain Telescope: Ellipsoidal Grating";
		this.position = p;
		this.radius   = r;
		this.rfactor  = f;

		// Aperture Stop at Cassegrain focal plane
		ConicSection stop = new Plane (new Point3d (0, 0, par.focusAt),
									   new Vector3d (0, 0, 1),
									   par.holesize * 2.0, par.holesize * 0.5);

		// A SphericeGrating
		double sina = 1e-6 * central * grooves * order;
		Point3d detFocus = new Point3d (Drowland * sina, 0, par.focusAt);
		double gratingsize = 2.0 * par.holesize;
		double ls = gratingsize / 2.0 + Drowland * sina;
		double rs = gratingsize / 2.0 - Drowland * sina;
		//addFocus (detFocus);

		ConicSection grating = 
			new EllipticGrating (detFocus,
								 new Point3d (-Drowland,
											  Drowland*rfactor, Drowland),
								 ls, rs, grooves, 
								 new Vector3d (0, 1, 0), 1, 0);

		Vector3d ndet = new Vector3d (sina, 0, Math.sqrt (1 - sina*sina) );
		//this = s*t1 + t2
		ConicSection detector;
		if (radius != FLAT) {
			double R = Drowland / 2.0;
			Point3d detPos = new Point3d (R * sina, 0, focusAt -
										  R * Math.sqrt (1.0 - sina*sina) );
			detPos.scaleAdd ((this.position - radius), ndet, detPos);
			detector = 
				new CurvedDetector (getName (), detPos, (R + radius),
									 -R*sina+size/2.0, R*sina+size/2.0, 0.0);
		} else {
			Point3d detPos = new Point3d (detFocus);
			detPos.scaleAdd (this.position, ndet, detPos);
			ndet.negate ();
			detector = 
				new PlaneDetector (getName (), detPos, ndet, size);
		}

		addConicSection (stop);
		addConicSection (grating);
		addConicSection (detector);
	}// end of EllipsoidalGrating Constructor

	public double   getPosition  () { return this.position  ; }
	public double   getCurvature () { return this.radius    ; }
	public double   getRadius    () { return this.radius    ; }
	public double   getFactor    () { return this.rfactor   ; }
	public String   toString     () {
		String radiusStr = (this.radius == FLAT) ? "FLAT" : 
			new String ("" + this.radius);
		return ("Detector Position: " + this.position + "mm, Radius: " +
				radiusStr + 
				"mm, rFactor: " + this.rfactor);
	}// end of toString
}// end of Class EllipsoidalGrating
