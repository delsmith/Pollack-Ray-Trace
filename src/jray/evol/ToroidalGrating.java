package jray.evol;

import jray.ConicSection;
import jray.CurvedDetector;
import jray.Plane;
import jray.PlaneDetector;
import jray.TelePar;
import jray.Telescope;
import jray.ToricGrating;
import jray.telescopes.Cassegrain;
import javax.vecmath.*;

/**
 * Made for use in optimization: Cassegrain telescope with
 * a toroidal grating, and a detector at some position.
 */
public class ToroidalGrating extends Cassegrain implements Telescope {

	/** set r = FLAT for a flat detector */
	public final static double FLAT = 4435.93;

	double Drowland = 1000.0; // mm
	double grooves = 3600.; // per mm
	double central = 150. ; // nm central wavelength
	int order = 1;
	double position;
	double radius;
	double a;

	/** 
	 * Construct a ToroidalGrating Cassegrain telescope.  
	 * Set r = ToroidalGrating.FLAT to use a flat detector
	 * rather than a curved detector.
	 * @param par Telescope Parameter object
	 * @param p position of the detector from from
	 * @param r radius of curvature of the detector from match
	 * @param a difference of two radii of curvature
	 */
	public ToroidalGrating (TelePar par, double p, double r) {
		this(par, p, r, 0.0);
	}
	public ToroidalGrating (TelePar par, double p, double r, double a) {
		this(par, p, r, a, DEFAULT_SIZE);
	}
	public ToroidalGrating (TelePar par, double p, double r, double a,
							   double size) {
		super(par);
		this.hasDet = true;
		this.title = "Cassegrain Telescope: Toroidal Grating";
		this.position = p;
		this.radius   = r;
		this.a = a;

		// Aperture Stop at Cassegrain focal plane
		ConicSection stop = new Plane (new Point3d (0, 0, par.focusAt),
									   new Vector3d (0, 0, 1),
									   par.holesize * 2.0, par.holesize * 0.5);

		// A SphericeGrating
		double correct = 155.0;
		double sina = 1e-6 * central * grooves * order;
		double sinb = 1e-6 * correct * grooves * order * 1.7 - sina;
		double cosa = Math.sqrt (1 - sina*sina);
		double cosb = Math.sqrt (1 - sinb*sinb);
		Point3d detFocus = new Point3d (Drowland * sina, 0, par.focusAt);
		double gratingsize = 2.0 * par.holesize;
		//addFocus (detFocus);
		Point3d vertex = new Point3d (0, 0, par.focusAt - 
									  Drowland * Math.sqrt (1 - sina*sina));
		// a = R - rho = R (1 - cosa cosb)
		if (this.a == 0.0) this.a = Drowland * (1.0 - cosa * cosb);
		ConicSection grating = 
			new ToricGrating (vertex, this.a, Drowland, gratingsize,
							  grooves, new Vector3d (0, 1, 0), 
							  1, Math.asin (0.54));

		addFocus (vertex);

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
	}// end of ToroidalGrating Constructor

	public double   getPosition  () { return this.position  ; }
	public double   getCurvature () { return this.radius    ; }
	public double   getRadius    () { return this.radius    ; }
	public double   getA         () { return this.a         ; }
	public String   toString     () {
		return ("Detector Position: " + this.position + "mm, Radius: " + 
				( (this.radius == FLAT) ? 0.0 : this.radius) + "mm, a: " +
				this.a );
	}// end of toString
}// end of Class ToroidalGrating
