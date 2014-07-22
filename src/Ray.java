

/**
 * @author  Scott Pollack
 * @revised Del Smith
 */

import jray.*;
import jray.evol.DetectorCurvature;
import jray.evol.DetectorPosition;
import jray.telescopes.*;

//import javax.vecmath.*;
import com.sun.image.codec.jpeg.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.text.NumberFormat;

/**
 * Cassegrain/Gregorian Telescope:
 * Ray Tracing and Plotting Program.  
 * Use this program to create blur patterns and distortion patterns
 * of Cassegrain and Gregorian telescopes with detectors at various
 * positions and of various radii of curvatures.
 * 
 * @author Scott Pollack
 * @version 26 Jan 2003
 * 
 * Add Reflex - Two-mirror three-optic (folded Newtonian) reflector
 * @author delsmith
 * @version 06 Jun 2005
 */
public class Ray {
	double diameter;
	double obstruction = 0.0;		// Linear fraction of diameter obstructed
	OpticBench bench;
	static boolean CIRRAY  = false;
	static boolean GREG    = false;
	static boolean CUSTOM    = true;
	static boolean BENCH   = false;
	static boolean DISTORT = false;
	static int     nRays   = 100;
	static double  detAt   = -100.0;
	static double  detShift = 0.0;
	static double  radius  = 0.0;
	static double  zoom = 0.0, xcenter = 0.0, ycenter = 0.0;


public static void main (String[] args) {
//		Vector3d v = new Vector3d ();
		TelePar p = new TelePar ();
		ProcessArgs (args, p);

		@SuppressWarnings("unused")
		Ray m = new Ray (p);
	}

	/** Process command line arguments to set the TelePar object */
	public static void ProcessArgs (String args[], TelePar par) {
		boolean USAGE = false;
		// Set Default Values
		par.FOV      = 0.0;
//		 Reflex
//		String args[] = {"-k", "-b", "-n", "32", "-z", "1e-14"};
//
// Gregorian
//		String args[] = {"-g", "-b", "-n", "32", "-d", "250", "-r", "4", "-z", "1e-14"};
		for (int i = 0; i < args.length; i += 2) {
			boolean bool = false;
			if (args[i].equals ("h") || args[i].equals ("-h")) {
				USAGE = true;
				break;
			}
			if (args[i].equals ("-g") ) {
				GREG = true;
				CUSTOM = false;
				bool = true;
				par.diameter = 1000.0;
				par.fratio   = 3.0;
				par.fdesired = 20.0;
				par.holesize = 50.0;
				par.focusAt  = -100.0;
			}
			if (args[i].equals ("-k")) {
				GREG = false;
				CUSTOM = true;
				bool = true;
				par.diameter = 200.0;
				par.fratio = 6.0;
				par.fdesired = 24.0;
				par.holesize = 25.0;
				par.focusAt = 1200.0;
				detAt = par.focusAt;
			}
			if (args[i].equals ("-b")) {
				BENCH = true;
				bool = true;
			}
			if (args[i].equals ("-n"))
				nRays = new Integer (args[i+1]).intValue ();
			if (args[i].equals ("-c")) {
				CIRRAY = true;
				bool = true;
			}
			if (args[i].equals ("-d"))
				par.diameter = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-r"))
				par.fratio = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-f"))
				par.fdesired = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-o"))
				par.holesize = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-a"))
				par.focusAt = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-x"))
				par.FOV = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-t"))
				detAt = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-u"))
				radius = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-i")) {
				bool = true;
				DISTORT = true;
			}
			if (args[i].equals ("-ds"))
				detShift = new Double(args[i+1]).doubleValue ();
			
			if (args[i].equals ("-z"))
				zoom = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-cx"))
				xcenter = new Double(args[i+1]).doubleValue ();
			if (args[i].equals ("-cy"))
				ycenter = new Double(args[i+1]).doubleValue ();
			if (bool) i--;
		}
		if (USAGE) {
			System.out.println ("Usage: java Ray [option] [value]");
			System.out.println ("       -g Gregorian [Cassegrain]");
			System.out.println ("       -k Custom [Cassegrain]");
			System.out.println ("       -b display bench         {set4true}");
			System.out.println ("       -n number of rays (in 1D) [   200]");
			System.out.println ("       -c circular grid         {set4true}");
			System.out.println ("       -d primary diameter  (mm) [  1000]");
			System.out.println ("       -ds detector shift        [   0.0]");
			System.out.println ("       -r f-ratio of primary     [   3.0]");
			System.out.println ("       -f desired f-ratio        [  20.0]");
			System.out.println ("       -o hole in primary   (mm) [  50.0]");
			System.out.println ("       -a desired focus loc (mm) [-100.0]");
			System.out.println ("       -x angle of light (arcsec)[   0.0]");
			System.out.println ("       -t detector position      [-100.0]");
			System.out.println ("       -u detector curvature     [   0.0]");
			System.out.println ("       -i distortion pattern    {set4true}");
			System.out.println ("DetectorDisplay Output Options            ");
			System.out.println ("       -z ZOOM save as JPG       [   0.0]");
			System.out.println ("       -cx X Center              [   0.0]");
			System.out.println ("       -cy Y Center              [   0.0]");
			System.exit (1);
		}
		
		detAt += detShift;
		
	} // end of ProcessArgs ****************************************************

	/** Creates a telescope given the TelePar object */
	public Ray (TelePar par) {
		this.diameter = par.diameter;
		// Create Telescope with a Detector, grab the Detector for display
		Telescope scope = new DetectorPosition (par, detAt, par.holesize * 2.);
		Detector d = (Detector) scope.getConicSection (2);
		System.out.println ("Detector position: " + detAt);
		if (radius != 0.0) {
			scope = new DetectorCurvature (par, detAt, radius,par.holesize*1.2);
			d = (Detector) scope.getConicSection (2);
		}
		if (GREG) {
			scope = new Gregorian (par);
			scope.addConicSection (((ConicSection) d));
		}
		else if (CUSTOM) {
			// Detector is facing the primary
			scope = new Reflex (par);
			scope.addConicSection (((ConicSection) d));
			this.obstruction = par.fratio/par.fdesired;
		}
		System.out.println ("Optimum Hole Size: " + scope.getOptimumHoleSize());

		// Create an OpticBench where the Telescope will live
		bench = new OpticBench (scope.getName (), BENCH, scope);
		// Add some LightRays to the OpticBench
		if (DISTORT) {
			d.getDisplay ().setPixel (3);
			addDistortionPattern (par.FOV);
		} else
			addLight (par.FOV, 0, 450.0);

		// Propagate LightRays through Telescope
		System.out.print ("Propagating: ");
		double t1 = System.currentTimeMillis();
		bench.PropagateLightRays ();
		double t2 = System.currentTimeMillis();
		System.out.println ((t2 - t1) + " milliseconds");
		bench.repaint ();

		// Detector Output
		if (zoom == 0.0)  d.display ();
//		if (zoom < 0.0)
			System.out.println ("Spot Size: " +
								d.getDisplay ().getSpotSize () + " mm");
		if (zoom > 0.0)  JPEG (par, d.getDisplay ());
	} // end of Ray constructor ************************************************

	/** Adds LightRays to the OpticBench of this object.
	 * The light comes in with angle (angx, angy) to the zenith
	 * and attemps to fill the aperature with light. */
	public void addLight (double angx, double angy, double wavelength) {
		double Z0 = bench.iscaley (0);
		for (int i = 1; i <= nRays; i++) {
			for (int j = 1; j <= nRays; j++) {
				double xvel = Math.tan (Math.toRadians (angx/3600.));
				double yvel = Math.tan (Math.toRadians (angy/3600.));

				double x = diameter/2.0 *
					(-1.0 + 2.0 * (i - 1.0) / (nRays - 1.0)) - xvel * Z0;
				double y = diameter/2.0 * 
					(-1.0 + 2.0 * (j - 1.0) / (nRays - 1.0)) - yvel * Z0;
				// Normally a square grid is used, here we switch to concentric circles 
				if (CIRRAY) {
					double r = diameter/2.0 * (i - 1.0) / nRays ;
					double theta = 2 * Math.PI * ( ((double)j) / nRays );
					x = r * Math.cos (theta) - xvel * Z0;
					y = r * Math.sin (theta) - yvel * Z0;
				}
				double radial = Math.sqrt(x*x + y*y);
				if ( radial <= diameter/2. && radial >= diameter * obstruction / 2. ){
					bench.addLight (new LightRay (x, y, Z0, xvel, yvel, -1., wavelength));
				}
			}
		}
} // end of addLight *******************************************************

	/** Adds a 9 concentric squares to the OpticBench
	 * and a circumscribing circle at angle. */
	public void addDistortionPattern (double angle) {
		int nSquares = 9;
		double [] lambda = {350., 400.0, 450., 480., 530.,
							560., 600., 700., 760.};
		int nAngle = 100;
		for (int j = 0; j < nSquares; j++) {
			double mangle = (angle * (j + 1.0)) / nSquares;
			double maxx = mangle / Math.sqrt (2);
			double maxy = mangle / Math.sqrt (2);
			for (int i = 0; i <= nAngle; i++) {
				double angx = (maxx * i) / nAngle;
				double angy = (maxy * i) / nAngle;
				addLight ( maxx,  angy, lambda[j]);
				addLight (-maxx,  angy, lambda[j]);
				addLight ( maxx, -angy, lambda[j]);
				addLight (-maxx, -angy, lambda[j]);
				addLight ( angx,  maxy, lambda[j]);
				addLight ( angx, -maxy, lambda[j]);
				addLight (-angx,  maxy, lambda[j]);
				addLight (-angx, -maxy, lambda[j]);
			}
		}
		// Draw a circle at maximum angle
		for (int i = 0; i < (10 * nAngle); i++) {
			double arg = 2.0 * Math.PI / (nAngle * 10.);
			double x = angle * Math.cos (arg * i);
			double y = angle * Math.sin (arg * i);
			addLight (x, y, lambda[0]);
		}
	} // end of addDistortionPattern *******************************************

	/** Saves the DetectorDisplay output to a JPEG file */
	public void JPEG (TelePar par, DetectorDisplay dd) {
		try {
			// create BufferedImage
			BufferedImage image =
				new BufferedImage (dd.getDim (), dd.getDim (),
								   BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics ();
			g.setBackground (Color.WHITE);
			dd.setCenter (xcenter, ycenter);
			dd.setZoom (zoom);
			dd.paint (g);
			// Annotate
			g.setColor (Color.BLACK);
			if (!DISTORT) {
				int	nPoints = dd.getPoints().size();
				g.setFont ( new Font ( "Times", Font.BOLD, 32 ));
				g.drawString (new String (nPoints + "/" + nRays * nRays + " rays"),
							  (int)(dd.getDim()*0.50) +  5, 30);
				/*
				g.drawString (new String (par.FOV + " arcseconds"), ???
				g.setFont ( new Font ( "Times", Font.PLAIN, 28 ));
				g.drawString (new String (nRays * nRays + " rays"),
							  (int)(dd.getDim()*0.60) + 30, 60);
				*/
			} else {
				NumberFormat nf = NumberFormat.getInstance ();
				nf.setMaximumFractionDigits(5);
				g.setFont ( new Font ( "Times", Font.BOLD, 24 ));
				g.drawString (new String ("R = " + nf.format (radius)),
							  (int)(dd.getDim ()*0.50) + 5, 30);
				g.drawString (new String ("P = " + nf.format (detAt)),
							  5, 30);
			}
			// Annotate holesize
			if (par.holesize != 50.0) {
				g.setFont ( new Font ("Times", Font.PLAIN, 16));
				g.drawString (new String (par.holesize + " mm hole"),
							  (int)(dd.getDim ()*0.60) + 30, 75);
			}
			// Save jpeg
			BufferedOutputStream out =
				new BufferedOutputStream(new FileOutputStream("blur.jpg"));
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.
				getDefaultJPEGEncodeParam(image);
			int quality = 100;
			quality = Math.max(0, Math.min(quality, 100));
			param.setQuality((float)quality / 100.0f, false);
			encoder.setJPEGEncodeParam(param);
			encoder.encode(image);
		} catch (IOException e) {
			System.out.println ("Output Error: " + e);
		}
	} // end of JPEG ***********************************************************
} // end of Class Ray **********************************************************
