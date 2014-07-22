package jray;

import java.util.*;

import javax.vecmath.*;

/** A generic Telescope Object */
public interface Telescope {
	/** Default linear size of anything you want */
	public static final double DEFAULT_SIZE = 500.0;
	/** Returns the TelePar object of this Telescope */
	public TelePar getPar ();
	/** number of ConicSections (including Detectors and Gratings) */
	public int numElements ();
	/** Return the ConicSection at position i */
	public ConicSection getConicSection (int i);
	/** Return the Vector containing all of the ConicSections */
	public Vector<ConicSection> getConicSections ();
	/** add a ConicSection to the end of list of ConicSections */
	public void addConicSection (ConicSection cs);
	/** Optimum Hole Size in Primary Mirror */
	public double getOptimumHoleSize ();
	/** Returns true is the Telescope has an associated Detector */
	public boolean hasDetector ();
	/** Return the number of focii of this Telescope */
	public int numFocii ();
	/** Adds a Focus to this Telescope */
	public void addFocus (Point3d focus);
	/** Return the focus at position i */
	public Point3d getFocus (int i);
	/** Return the field of view this Telescope is designed for */
	public double getFOV ();
	/** Return the String Name of this Telescope */
	public String getName ();
	/** overrides toString () */
	public String toString ();
}
