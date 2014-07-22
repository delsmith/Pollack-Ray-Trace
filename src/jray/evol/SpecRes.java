package jray.evol;

import jray.Detector;
import jray.LightRay;
import jray.OpticBench;
import jray.Telescope;

/***
 * SpecRes evaluates a Telescope+Spectrograph with a detector.
 * The fittness of the telescope is determined by
 *      \sum_colors (spot_size)
 * where colors is bound by the evaluator, and spot_size is the 
 * linear size of the line.
 */
public class SpecRes implements Evaluator {

	double min, max, BW;
	int nRays, nColors;

	OpticBench bench;
	Detector det;
	double diameter;

	/** Construct a SpotSize with 140-160 nm bandpass
	 *  3 LightRays and 21 colors */
	public SpecRes () {
		this(140, 160, 3, 21);
	}
	/** Construct a SpecRes with FOV, nR, nAngles */
	public SpecRes (double ml, double xl, int nr, int nC) {
		this.min = ml;
		this.max = xl;
		if (ml > xl) {
			this.max = ml;
			this.min = xl;
		}
		this.nRays = nr;
		this.nColors = nC;
		this.BW = this.max - this.min;
	}

	/** Initilize this Evaluator with the Given Telescope */
	public void init (Telescope t) {
		bench = new OpticBench ("Optimization of Simple Telescope", false);
		bench.addTelescope (t);
		if (!t.hasDetector ())
			throw new RuntimeException ("Evaluator: You are trying " +
	 									"to evaluate a telescope without " +
										"a detector!");
		// The detector should be the last ConicSection of the Telescope
		this.det = (Detector) t.getConicSection (t.numElements () - 1);
		// Diameter of the Primary ConicSection
		this.diameter = (t.getConicSection (0)).getSize ();
	}

	/** Evaluate an Object -- redirect to Evaluate (Telescope o) */
	public double evaluate (Object o) { return evaluate ( ((Telescope) o) ); }
	/** Evaluate a Telescope */
	public double evaluate (Telescope t) {
		init (t);
		double res = 0.;
		for (int k = 0; k < nColors; k++) {
			double lambda = min + (BW * k) / (nColors - 1.0);
			double ares = lambda / LineComputer (lambda);
			res += ares;
		}
		// want to maximum the mean resolution
		double meanres = (res / (nColors - 1.0)) / BW;
		return meanres;
	} // end of evaluate *******************************************************

	/** returns the size of the line on the detector of the telescope */
	public double LineComputer (double l) {
		return LineComputer (l, false);
	}
	/** bNoisy flag for outputing computing time and line size */
	public double LineComputer (double lambda, boolean bNoisy) {
		bench.clean ();
		det.clean ();

		// For each angle propagate some rays.
		double Z0 = 5000;//bench.iscaley (0);
		for (int i = 1; i <= nRays; i++) {
			for (int j = 1; j <= nRays; j++) {
				//double xvel = Math.tan (Math.toRadians (ang/3600.));
				double x = diameter/2.0 * (-1.0 + 2.0 * (i - 1.0)/(nRays-1.0));
				double y = diameter/2.0 * (-1.0 + 2.0 * (j - 1.0)/(nRays-1.0));
				bench.addLight ( new LightRay ( x, y, Z0, 0, 0., -1, lambda ));
			}
		}

		if (bNoisy) {
			double t1 = 0, t2 = 0;
			t1 = System.currentTimeMillis();
			System.out.print (lambda + " nm -- Propagating: ");
			bench.PropagateLightRays ();
			t2 = System.currentTimeMillis();
			System.out.print ((t2 - t1) + " milliseconds");
		} else
			bench.PropagateLightRays ();

		double aline = det.getDisplay ().getXSpotSize ();
		if (bNoisy) System.out.println ("\tLine Size: " + aline + " mm");
		return aline;
	} // end of LineComputer ***************************************************
} // end of Class SpecRes *****************************************************
