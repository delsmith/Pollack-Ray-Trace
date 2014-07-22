package jray.evol;

import jray.ConicSection;
import jray.PlaneDetector;
import jray.TelePar;
import jray.Telescope;
import jray.telescopes.Cassegrain;
import javax.vecmath.*;

/**
 * Made for use in optimization: Cassegrain telescope with
 * detector at particular position.
 * Detector faces in the +z-direction.
 */
public class DetectorPosition extends Cassegrain implements Telescope {

	double position;
	/** p is the position of this DetectorPosition Object */
	public DetectorPosition (TelePar par, double p) {
		this(par, p, DEFAULT_SIZE);
	}
	public DetectorPosition (TelePar par, double p, double size) {
		super(par);
		this.hasDet = true;
		this.title = "Cassegrain Telescope: Detector Position";
		this.position = p;

		ConicSection detector = 
			new PlaneDetector ("DetectorPosition",
							   new Point3d (0, 0, this.position),
							   new Vector3d ( 0, 0, 1), size);
		ConicSections.add (detector);
	}

	public double getPosition () { return this.position; }
	public String toString () {
		return ("Detector Position: " + this.position + "mm");
	}
}
