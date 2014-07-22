package jray.evol;

import jray.ConicSection;
import jray.SphericDetector;
import jray.TelePar;
import jray.Telescope;
import jray.telescopes.Cassegrain;
import javax.vecmath.*;

/**
 * Made for use in optimization: Cassegrain telescope with
 * detector at particular position with
 * some spherical radius of curvature
 */
public class DetectorCurvature extends Cassegrain implements Telescope {

	double radius;
	double position;

	/** p is the position, r the radius of curvature
	 *  of this DetectorCurvature */
	public DetectorCurvature (TelePar par, double p, double r) {
		this(par, p, r, DEFAULT_SIZE);
	}
	public DetectorCurvature (TelePar par, double p, double r, double size) {
		super(par);
		this.hasDet = true;
		this.title = "Cassegrain Telescope: Detector Curvature";
		this.position = p;
		this.radius = r;

		ConicSection detector =
			new SphericDetector ("DetectorCurvature",
								 new Point3d (0, 0, radius + position),
								 -radius, size);
		ConicSections.add (detector);
	}

	public double   getPosition  () { return this.position  ; }
	public double   getCurvature () { return this.radius    ; }
	public double   getRadius    () { return getCurvature (); }
	public String   toString     () {
		return ("Detector Position: " + this.position +
				"mm, Radius: " + this.radius + "mm");
	}
}
