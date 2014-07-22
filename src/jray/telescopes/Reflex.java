package jray.telescopes;

import jray.*;
import java.util.Vector;
import javax.vecmath.*;

/** Reflex Telescope */
/*
 * Reflex uses a parabolic primary and convex secondary with hole
 * The secondary is positioned so its focus is 'near' the primary focus
 * The primary acts as a tertiary reflector
 * The final image is formed behind the secondary, through the hole
 * A diagonal reflector directs the final image through the side of the tube.
 * 
 * The design parameters are 
 *	[PDI]	primary diameter					[300]
 *	[PFR]	primary f-ratio						[3]
 *	[FFR]	desired f-ratio						[10]
 *  [FFP]	focus z-position			        [1200]  (normally ~100mm outside tube)
 *          note: FFP must be greater than secondary position
 *
 * These directly yield 
 *	[PFL]	the primary focal length
 *	[SFL]	the secondary focal length
 *
 * Then we derive
 *	[P3]	tertiary image distance (from main reflector - non-critical)
 *	[V2]	primary-secondary spacing (critical setting)
 *	[EFL]	the system focal length
 * to achieve focus at desired position
 *
 * Finally we calculate optimal values for
 *	[SDI]	secondary diameter and
 *	[HDI]	exit hole diameter
 * 
 * Procedure:
 * a) let M = PFR/FFR, then SFL = -M*PFL (negative => convex)
 * b) let P3 = PFL + SFL + FFP, 
 * c) find DV, such that 
 * 		P3 = PFL + (PFL^2 * DV)/(DV^2 + M*(PFL^2)), or
 *		DV^2 - DV*(PFL^2/(P3-PFL)) + M*(PFL^2) = 0
 * d) let V2 = PFL + SFL + DV
 * 
 * This defines the positions of the elements, now determine the optimal secondary dimensions
 * e) SDI/PDI = 1 - (V2/PFL), SDI = PDI*(1-(V2/PFL))  (for paraxial rays)
 * f) find [P2]	notional secondary image position, such that
 * 		1/(P2-V2) = 1/SFL - 1/(PFL-V2)	(first image is at PFL)
 * g) then [TDI] notional tertiary reflector diameter, such that
 * 		SDI/TDI = 1 - (V2/P2), TDI = SDI/(1-(V2/P2)) or SDI*P3/(P2-V2)
 * h) HDI/TDI = 1 - (V2/P3), HDI = TDI*(1-(V2/P3))
 * 
 * Now determine the system f-ratio and focal length (should match FFR and PDI*FFR)
 * i) EFR = P3/TDI
 * j) EFL = PDI * EFR
 */
public class Reflex implements Telescope {

	protected String title = "Reflex Telescope";
 	protected TelePar par;
	protected double diameter, fratio, holesize;
	protected double focusAt, fdesired;
	protected double optHole, FOV;
	protected Vector ConicSections;
	protected Point3d focus1, focus2, focus3;
	protected boolean hasDet = false;

	/** Construct a Reflex Telescope with parameters TelePar */
	public Reflex (TelePar par) {
		this.par      = par;

		//	Copy the design specifications
		diameter = par.diameter;
		fratio = par.fratio;
		fdesired = par.fdesired;
		focusAt  = par.focusAt;
		
		//  Find the resulting dimensions
		double mFactor = fratio / fdesired;
		double dFocus1 = diameter * fratio;
		double dFocus2 = -dFocus1 * mFactor;			// Convex
		double dFinal = focusAt; // + dFocus2 + dFocus1;	// Image distance from primary
		
		// Create Primary Circular Paraboloid - vertex at (0,0,0), focus at (0,0,diameter*fratio)
		// (a,a,1) is the point where the paraboloid intersects the plane x=y and the plane z=1 
		focus1 = new Point3d (0, 0, dFocus1);
		double a = 2.0 * Math.sqrt ( focus1.distance (new Point3d (0,0,0)) );
		Paraboloid primary = new Paraboloid ( new Point3d (0,0,0), new Point3d (a,a,1), diameter);
		
		// Create Secondary Paraboloid at vertex2
		double dVertex2 = secondVertexPosition(dFocus1, mFactor, dFinal);
		Point3d vertex2 = new Point3d(0, 0, dVertex2);
		focus2 = new Point3d( 0, 0, dVertex2 - dFocus2);	// double negative
		double diameter2 = Math.ceil(diameter * (1-(dVertex2/dFocus1)));
		
		double dImage2 = -1.e50, diameter3 = diameter2;
		if ((dVertex2 - dFocus1 - dFocus2) != 0) {
			dImage2 = Math.ceil(dVertex2 - (dFocus2*(dVertex2-dFocus1)/(dVertex2 - dFocus1 - dFocus2)));
			diameter3 = Math.ceil(diameter2 * dImage2 /(dImage2-dVertex2));
			System.out.println("I2=" + dImage2 + ", D3=" + diameter3 );
		}
		holesize = Math.ceil(diameter3 * (1-(dVertex2/dFinal)));
		
		a = 2.0 * Math.sqrt ( focus2.distance(vertex2));
		Paraboloid secondary = new Paraboloid ( vertex2, new Point3d (a, a, 1), diameter2, 0);
		
		// Find optimum diameter for secondary
		double xvel = Math.tan (Math.toRadians (FOV/3600.));
		
		LightRay l = new LightRay (diameter/2.0 - xvel * 100000., 0, 100000., xvel, 0, -1);
		l = primary.reflect (l);
		double hypers1 = Math.abs (2.0 * (secondary.intersect (l)).x);
		
		l = new LightRay (-diameter/2.0 - xvel * 100000., 0, 100000., xvel, 0, -1);
		l = primary.reflect (l);
		double hypers2 = Math.abs (2.0 * (secondary.intersect (l)).x);
		
		diameter2 = (hypers1 > hypers2) ? hypers1 : hypers2;
		diameter2 = Math.ceil (diameter2);
		
		l = secondary.reflect (l);
		l = primary.reflect (l);
		optHole = Math.ceil(Math.abs (2.0 * (secondary.intersect (l)).x));
		
		// Detector will be behind Secondary, after third reflection off Primary 
		secondary = new Paraboloid ( vertex2, new Point3d (a, a, 1), diameter2, holesize);
		ConicSections = new Vector ();
		ConicSections.add (primary);
		ConicSections.add (secondary);
		ConicSections.add (primary);
		
		// Show the focii as the three paraxial image positions
		focus2 = new Point3d(0, 0, dImage2);
		focus3 = new Point3d(0, 0, dFinal);

		// Confirm design specifications
		double dSystemRatio = dFinal / diameter3;
		double dSystemLength = diameter * dSystemRatio;
		System.out.println("F1=" + dFocus1 + ", V2=" + dVertex2 + ", F2=" + dFocus2 + ", FF=" + dFinal );
		System.out.println("D2=" + diameter2 + ", RS=" + dSystemRatio + ", FS= " + dSystemLength );
		
	} // end of Reflex Constructor ******************************************

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

	/** Adds a Focus to this Telescope : DUMMY: Reflex always has TWO foci. */
	public void addFocus (Point3d f) { }
	
	/** A Reflex Telescope has 2 focii */
	public int numFocii () {return 3;}
	
	/** Return the focus at position i */
	public Point3d getFocus (int i) {
		if (i == 0)
			return focus1;
		if (i == 1)
			return focus2;
		if (i == 2)
			return focus3;
		throw new RuntimeException ("Reflex Telescope has only " +
									"3 focii.  Focus asked for = " + 
									(i+1));
	}

	/** Return the field of view this Telescope is designed for */
	public double getFOV () {return FOV;}
	/** Return the String Name of this Telescope */
	public String getName () {return title;}
	/** overrides toString () */
	public String toString () { return new String (title + par); }
	
  // private utility functions
	static double secondVertexPosition(double dFocus1, double mFactor, double dFinal  )
	{
		/*
			 * find DV, such that 
			 * 		P3 = PFL + (PFL^2 * DV)/(DV^2 + M*(PFL^2)), or
			 *		DV^2 - DV*(PFL^2/(P3-PFL)) + M*(PFL^2) = 0
			 * let V2 = PFL + SFL + DV
		 * secondVertexPosition (assume focus2 = -dFocus1*mFactor)
		 * 		vertex2 = dFocus1 + focus2 + delta
		 * where delta depends on dFinal and satisfies
		 * 		dFinal = dFocus1 + (dFocus1^2 * delta)/(delta^2 + mFactor*(dFocus1^2))
		 * obviously when delta==0, dFinal=dFocus1
		 * otherwise find delta by solving
		 * 		delta^2 -(dFocus1^2/(dFinal-dFocus1))*delta + mFactor*(dFocus1^2) = 0
		 */
		double vertex2 = dFocus1 * (1-mFactor);		// dFocus1+focus2

		if ( dFinal != dFocus1 ) 
		{
/*
			double a = 1;
			double b = -(dFocus1*dFocus1)/(dFinal-dFocus1);
			double c = mFactor*(dFocus1*dFocus1);
*/
			double a = (dFinal-dFocus1);
			double b = -(dFocus1*dFocus1);
			double c = mFactor*(dFocus1*dFocus1)*(dFinal-dFocus1);
			double t1 = 2.0 * Math.sqrt(mFactor) * (dFinal-dFocus1)/dFocus1;
			double DISC = /* b*b - 4.0*a*c */
						dFocus1*dFocus1*Math.sqrt((1-t1)*(1+t1));
			if ( DISC >= 0.0 ) {
				double t2 = Math.sqrt((1-t1)*(1+t1));
				double t3 = dFocus1*dFocus1 /(2.0*a); 
				double soln1 = t3 * (1.0-t2);
				double soln2 = t3 * (1.0+t2);
				if ( Math.abs(soln1) <= Math.abs(soln2) )
					vertex2 -= soln1;
				else
					vertex2 -= soln2;
				return (vertex2);
			}
			else
				throw new RuntimeException( "Infeasible arguments for function 'secondVertexPosition()'");
		}
		return (vertex2);
	}
} // end of Class Reflex ****************************************************
