package jray.telescopes;

import jray.*;
import java.util.Vector;
import javax.vecmath.*;

/** Generic Gregorian Telescope */
public class Gregorian implements Telescope {

	protected String title = "Gregorian Telescope";
 	protected TelePar par;
	protected double diameter, fratio, holesize;
	protected double focusAt, fdesired;
	protected double optHole, FOV;
	protected Vector ConicSections;
	protected Point3d focus1, focus2;
	protected boolean hasDet = false;

	/** Construct a Gregorian Telescope with parameters TelePar */
	public Gregorian (TelePar par) {
		this.par      = par;
		this.diameter = par.diameter;
		this.fratio   = par.fratio;
		this.holesize = par.holesize;
		this.focusAt  = par.focusAt;
		this.fdesired = par.fdesired;
		this.FOV      = par.FOV;

		// Create Primary Circular Paraboloid
		focus1 = new Point3d (0, 0, diameter * fratio);
		double a = 2.0 * Math.sqrt ( focus1.distance (new Point3d (0,0,0)) );
		Paraboloid primary = 
			new Paraboloid ( new Point3d (0, 0, 0),
							 new Point3d (a, a, 1), diameter, holesize);

		// Create Secondary Ellipsoid
		double hyperv = (fratio * diameter + focusAt) / 2.0;
		double ellipc = (fratio * diameter - focusAt) /
			(1.0 - fratio/fdesired) - (-focusAt + hyperv);
		double ellipb = Math.sqrt ( ellipc * ellipc - 
									Math.pow (fratio * diameter - hyperv, 2.0));
		double xvel = Math.tan (Math.toRadians (FOV/3600.));
		// Find optimum diameter for secondary
		Ellipsoid e = new Ellipsoid (new Point3d (0, 0, hyperv),
									 new Point3d (ellipb, ellipb, ellipc),
									 diameter);
		LightRay l = new LightRay (diameter/2.0 - xvel * 100000., 0, 100000., 
								   xvel, 0, -1);
		l = primary.reflect (l);
		double hypers1 = Math.abs (2.0 * (e.intersect (l)).x);
		l = new LightRay (-diameter/2.0 - xvel * 100000., 0, 100000., 
						  xvel, 0, -1);
		l = primary.reflect (l);
		double hypers2 = Math.abs (2.0 * (e.intersect (l)).x);
		double hypers = (hypers1 > hypers2) ? hypers1 : hypers2;
		hypers = Math.ceil (hypers);
		Ellipsoid secondary = 
			new Ellipsoid ( new Point3d (0, 0, hyperv),
							new Point3d (ellipb, ellipb, ellipc), 
							hypers);
		focus2 = secondary.getFocus (1);

		l = secondary.reflect (l);
		optHole = Math.abs (2.0 * (primary.intersect (l)).x);
		ConicSections = new Vector ();
		ConicSections.add (primary);
		ConicSections.add (secondary);
	} // end of Gregorian Constructor ******************************************

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
	public void addConicSection (ConicSection cs) { ConicSections.add (cs); }
	/** A generic telescope does not have a detector.  But subclasses can. */
	public boolean hasDetector () {return hasDet;}
	/** Optimum Hole Size in Primary Mirror */
	public double getOptimumHoleSize () {return optHole;}

	/** Adds a Focus to this Telescope : DUMMY: Gregorian always has TWO foci. */
	public void addFocus (Point3d f) { }
	
	/** A Gregorian Telescope has 2 focii */
	public int numFocii () {return 2;}
	/** Return the focus at position i */
	public Point3d getFocus (int i) {
		if (i == 0)
			return focus1;
		if (i == 1)
			return focus2;
		throw new RuntimeException ("Gregorian Telescope has only " +
									"2 focii.  Focus asked for = " + 
									(i+1));
	}

	/** Return the field of view this Telescope is designed for */
	public double getFOV () {return FOV;}
	/** Return the String Name of this Telescope */
	public String getName () {return title;}
	/** overrides toString () */
	public String toString () { return new String (title + par); }
} // end of Class Gregorian ****************************************************
