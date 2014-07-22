package jray;

/** Generic Telescope Parameter Object */
public class TelePar {	
	/** Field of View in arcseconds */
	public double FOV      = 30.0;
	/** Diameter of Primary (mm) */
	public double diameter = 1e3;
	/** f-number of Primary */
	public double fratio   = 3;
	/** Size of exit hole (mm) */
	public double holesize = 50;
	/** Desired offset of final focus */
	public double focusAt  = -100;
	/** desired f-number (Gregorian) */
	public double fdesired = 20;
	/** TelePar Object has no specified constructor */
	public TelePar () {}
	/** overrides equals() */
	public boolean equals (TelePar p) {
		if (FOV      == p.FOV      &&
			diameter == p.diameter &&
			fratio   == p.fratio   &&
			holesize == p.holesize &&
			focusAt  == p.focusAt  &&
			fdesired == p.fdesired  )
			return true;
		else
			return false;
	}
	/** overrides toString() */
	public String toString () {
		return new String (" D=" + diameter + ", f#=" + fratio + 
						   ", hole=" + holesize + ", focus=" + focusAt + 
						   ", feff=" + (fdesired * diameter) + 
						   ", FOV=" + FOV );
	}
} // end of Class TelePar ******************************************************

