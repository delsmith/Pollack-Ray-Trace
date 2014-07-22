package jray.telescopes;

import jray.*;
import java.util.Vector;
import javax.vecmath.*;

/** Generic Cassegrain Telescope */
public class Cassegrain implements Telescope {

	protected String title = "Cassegrain Telescope";

 	protected TelePar par;
	protected double diameter, fratio, holesize;
	protected double focusAt, fdesired;
	protected double optHole, FOV;
	protected Vector ConicSections;
	protected Vector focii;
	protected boolean hasDet = false;

	/** Construct a Cassegrain Telescope with parameters TelePar */
	public Cassegrain (TelePar par) {
		this.par = par;
		this.diameter = par.diameter;
		this.fratio   = par.fratio;
		this.holesize = par.holesize;
		this.focusAt  = par.focusAt;
		this.fdesired = par.fdesired;
		this.FOV      = par.FOV;

		// Create Primary Circular Paraboloid
		Point3d focus1 = new Point3d (0, 0, diameter * fratio);
		double a = 2.0 * Math.sqrt ( focus1.distance (new Point3d (0,0,0)) );
		Paraboloid primary = new Paraboloid ( new Point3d (0, 0, 0),
											  new Point3d (a, a, 1), 
											  diameter, holesize);

		// Create Secondary Hyperboloid
		double hyperv = (fratio * diameter + focusAt) / 2.0;
		double hypera = (fratio * diameter - focusAt) /
			(1.0 + fratio/fdesired) - (-focusAt + hyperv);
		double hyperb = Math.sqrt (Math.pow (fratio * diameter - hyperv, 2.0) 
								   - hypera * hypera);
		double xvel = Math.tan (Math.toRadians (FOV/3600.));
		LightRay l = new LightRay (diameter/2.0 - xvel * 100000., 0, 100000., 
								   xvel, 0, -1);
		l = primary.reflect (l);
		double hypers = 2.0 * (new Hyperbola (new Point3d (0, 0, hyperv),
											  focus1, hypera, diameter).
							   intersect (l)).x;
		hypers = Math.round (hypers);
		Hyperboloid secondary = 
			new Hyperboloid (new Point3d (0, 0, hyperv),
							 new Point3d (hyperb, hyperb, hypera), 
							 hypers);
		focii = new Vector ();
		focii.add (focus1);
		focii.add (secondary.getFocus (1));
		l = secondary.reflect (l);
		optHole = Math.abs (2.0 * (primary.intersect (l)).x);
		ConicSections = new Vector ();
		ConicSections.add (primary);
		ConicSections.add (secondary);
	} // end of Cassegrain Constructor *****************************************

	/** returns the TelePar Object associated with this Telescope */
	public TelePar getPar () { return par; }
	/** number of ConicSections (including Detectors and Gratings) */
	public int numElements () { return ConicSections.size (); }
	/** Return the ConicSection at position i */
	public ConicSection getConicSection (int i) {
		return ((ConicSection) ConicSections.get (i));
	}
	/** Return the Vector containing all of the ConicSections */
	public Vector getConicSections () { return ConicSections; }
	/** add a ConicSection to the end of list of ConicSections */
	public void addConicSection (ConicSection c) { ConicSections.add (c); }
	/** A generic telescope does not have a detector.  But subclasses can. */
	public boolean hasDetector () {return hasDet;}
	/** Optimum Hole Size in Primary Mirror */
	public double getOptimumHoleSize () {return optHole;}
	/** Adds a Focus to this Telescope */
	public void addFocus (Point3d f) { focii.add (f); }
	/** A Cassegrain Telescope has 2 focii */
	public int numFocii () {return focii.size ();}
	/** Return the focus at position i */
	public Point3d getFocus (int i) {
		if (i < focii.size () )
			return ((Point3d) focii.get (i));
		throw new RuntimeException ("Cassegrain Telescope has only " + 
									"2 focii normally.  Focus asked for = " + 
									(i+1) );
	}
	/** Return the field of view this Telescope is designed for */
	public double getFOV () {return FOV;}
	/** Return the String Name of this Telescope */
	public String getName () {return title;}
	/** overrides toString () */
	public String toString () { return new String (title + par); }
} // end of Class Cassegrain ***************************************************
