package jray;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.vecmath.Point3d;



/** An optical bench for displaying a Telescope and LightRays */
public class OpticBench extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6612217876572904363L;
	public  static int XSIZE = 1000;
	public  static int YSIZE = 1000;
	public  static final int XSCALE = 1600;//1500//8000
	private static final double xtraScale = 2.0;
	public  static final int YSCALE = (int)
		Math.ceil (XSCALE * (1376./1032.) * xtraScale);
	private double YCENTER = 970.0;

	private int FSIZE = 10;
	private double END_TIME = 10000;
	private double time = 0;

	private Vector<ConicSection> cs; // The ConicSections
	private Vector<LightRay> lr; // The LightRays
	private Vector<Point3d> focus; // The Focii
	private boolean SECONDARY = false;		// true, if Gregorian or Cassegrain
	private boolean REFLEX = false;			// true, if Folded (Loveday or Reflex)
	private boolean display;

	private final static Color COLOR_CONIC = Color.BLACK;
	private final static Color COLOR_RAY   = Color.BLUE;
	private final static Color COLOR_FOCUS = Color.RED;

	private int[][] lX;
	private int[][] lY;
	private int nl;

	/** Construct an OpticBench with a specified title */
	public OpticBench (String title) { this(title, false); }
	/** Construct an OpticBench with a specified title,
	 * boolean to display the Bench, and initial Telescope */
	public OpticBench (String title, boolean display, Telescope scope) {
		this(title, display);
		addTelescope (scope);
	}
	/** Construct an OpticBench with a specified title,
	 * boolean to display the Bench */
	public OpticBench (String title, boolean display) {
		setTitle (title);
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		setSize (XSIZE, YSIZE);
		cs = new Vector<ConicSection> ();
		lr = new Vector<LightRay> ();
		focus = new Vector<Point3d> ();
		this.addComponentListener (new ReSize (this));
		this.display = display;
		setVisible (display);
	}
	/** Add ConicSection to the Bench */
	public void addOptic (ConicSection c) { cs.add (c); }
	/** Remove the ConicSection at position i */
	public void removeOptic (int i) { cs.remove (i); }
	/** Returns the ConicSection at position i */
	public Object getOptic (int i) { return cs.get (i); }
	/** Adds a LightRay to the Bench */
	public void addLight (LightRay l) { lr.add (l); }
	/** Adds a Focus to the Bench (plotted as a Red Dot) */
	public void addFocus (Point3d p) { focus.add (p); } 
	/** Removes all LightRays from the Bench */
	public void clean () { lr = new Vector<LightRay> (); }

	/** Adds a complete Telescope to this Bench */
	public void addTelescope (Telescope t) {
		String	type = t.getName();
		if ( type == "Reflex Telescope")
			REFLEX = true;
		else if ( type == "Cassegrain Telescope" || type == "Gregorian Telescope" )
			SECONDARY = true;

		for (int i = 0; i < t.numFocii (); i++)
			addFocus (t.getFocus (i));
		for (int i = 0; i < t.numElements (); i++)
			addOptic (t.getConicSection (i));
	}

	/** Propagate all LightRays through the Bench */
	public void PropagateLightRays () {
		/*
		 * lX:lY store the turning points of each ray
		 * There is a Start, plus one for each surface (including detector), plus an End
		 * If we are displaying, we create the turning point tables.
		 */
		
		nl = cs.size () + 2;
		if (cs.size () >= 2 && SECONDARY) nl += 2;
		if (display) {
			lX = new int [lr.size ()][nl];
			lY = new int [lr.size ()][nl];
		}
		
		/*
		 * 'lr' is a vector of light rays
		 * get each one and trace it from primary to detector
		 * if displaying, add the turning points to the tables
		 */
		for (int q = 0; q < lr.size (); q++) {
			LightRay ray = (LightRay) lr.get (q);
			ray.init ();
			
			// This is the Start position
			if (display) {
//				System.out.println ("\nTracing ray # " + (q+1));
//				System.out.println ( ray.getPosition () );
				lX [q][0] = scalex ((ray.getPosition ()).x);
				lY [q][0] = scaley ((ray.getPosition ()).z);
//				System.out.println("0:"+lX [q][0]+","+lY [q][0]);
			}
			
			int start = 0;
			int css = 0;
			/*
			 * Handle a simple reflector here
			 */
			if (cs.size () == 1) 
			{
				// Find the single Primary reflection point
				ray = ((ConicSection) cs.get (0)).reflect (ray);
				if (display) {
					lX [q][1] = scalex ((ray.getPosition ()).x);
					lY [q][1] = scaley ((ray.getPosition ()).z);
//					System.out.println("1:"+lX [q][1]+","+lY [q][1]);
				}
			}
			
			/*
			 * Handle compound systems here
			 */
			else
			{
				/*
				 * Here we handle two-stage systems (Cassegrain & Gregorian)
				 */
				if ( SECONDARY ) 
				{
					/* Reflect off of the second optic first,
					 * since it is generally blocking our view
					 * How do we know it is obstructed?
					 * if (intersect != ray.ipos)
					 */
					ray = ((ConicSection) cs.get (1)).reflect (ray);
					if (display) {
						lX [q][1] = scalex ((ray.getPosition ()).x);
						lY [q][1] = scaley ((ray.getPosition ()).z);
//						System.out.println("1:"+lX [q][1]+","+lY [q][1]);
					}
					
					/* After that.  Reflect off first, reflect off second,
					 * and pass through hole of the first
					 */
					ray = ((ConicSection) cs.get (0)).reflect (ray);
					if (display) {
						lX [q][2] = scalex ((ray.getPosition ()).x);
						lY [q][2] = scaley ((ray.getPosition ()).z);
//						System.out.println("2:"+lX [q][2]+","+lY [q][2]);
					}
					ray = ((ConicSection) cs.get (1)).reflect (ray);
					if (display) {
						lX [q][3] = scalex ((ray.getPosition ()).x);
						lY [q][3] = scaley ((ray.getPosition ()).z);
//						System.out.println("3:"+lX [q][3]+","+lY [q][3]);
					}
					/* Before we get to first, make sure there is 
					 * no other optic in the way
					 */
					start = 3;

					double t0 = 0;
					double t2 = 1;
					if (cs.size () > 2) {
						t0 = ((ConicSection) cs.get (0)).getTime (ray);
						t2 = ((ConicSection) cs.get (2)).getTime (ray);
					}
					if (t0 < t2) {
						ray = ((ConicSection) cs.get (0)).reflect (ray);
						if (display) {
							lX [q][4] = scalex ((ray.getPosition ()).x);
							lY [q][4] = scaley ((ray.getPosition ()).z);
//						System.out.println("4:"+lX [q][4]+","+lY [q][4]);
						}
						start++;
					}
					css = 2;
				}
				else if (REFLEX) {
				}
				
				// Now trace through the remaining optics until the Detector
				for (int l = 1; l <= cs.size () - css; l++) 
				{
					int	element = l + css - 1;
					ray = ((ConicSection) cs.get (element)).reflect (ray);
					if (display) {
						if (Double.isNaN (ray.getPosition ().x)) {
							lX [q][l + start] = lX [q][l + start - 1];
							lY [q][l + start] = lY [q][l + start - 1];
						} else {
							lX [q][l + start] = scalex ((ray.getPosition ()).x);
							lY [q][l + start] = scaley ((ray.getPosition ()).z);
						}
//						System.out.println((l+start)+":"+lX [q][l + start]+","+lY [q][l + start]);
					}
				}
			}
			
			ray.propagate (END_TIME);
			if (display) {
				int loc = cs.size () - css + start + 1;
/*
 * Del Smith: 26-Jul-05
 * Hide rays that don't intersect the Detector
 */
				if (!Double.isNaN (ray.getPosition ().x)) {
					loc = 0;		// Hide this ray, collapse onto Start position
				} 
				else {
					lX [q][loc] = lX [q][loc-1];
					lY [q][loc] = lY [q][loc-1];
//					System.out.println(loc+":"+lX [q][loc]+","+lY [q][loc]);
				}
				for (int i = loc+1; i < nl; i++) {
					lX [q][i] = lX [q][i - 1];
					lY [q][i] = lY [q][i - 1];
//					System.out.println("#" + i + ":" + lX [q][i] + "," + lY [q][i]);
				}
			}
			this.time = 1;
		}
	} // end of PropagateLightRays *********************************************

	/** paints the Bench onto the Graphics Object */
	public void paint (Graphics g) {
		g.clearRect (0, 0, XSIZE, YSIZE);

		/*********** Draw the ConicSection **************/
		int n = XSIZE;
		g.setColor (COLOR_CONIC);
		for (int k = 0; k < cs.size (); k++) {
			boolean VERTICAL = false;
			ConicSection c = (ConicSection) cs.get (k);
			// test for class Plane, test vertical
			if (c.getClass ().getName ().equals ("jray.Plane") ||
				c.getClass ().getSuperclass ().
				getName ().equals ("jray.Plane") ) {
				VERTICAL = ((Plane) c).isVertical ();
			}
			int [] X = new int [n];
			int [] Y = new int [n];
			int j = -1;
			for (int i = 0; i < n; i++) {
				double temp = 0;
				if (VERTICAL) {
					temp = c.Evaluate (iscaley (i));
				} else
					temp = c.Evaluate (iscalex (i));
				if (temp != ConicSection.NULL && 
					temp != ConicSection.BLANK) {
					++j;
					if (!VERTICAL) {
						X [j] = i;
						Y [j] = scaley (temp);
					} else {
						X [j] = scalex (temp);
						Y [j] = i;
					}
				}
				if (temp == ConicSection.BLANK) {
					/* There's a hole in the optic.
					 * Go ahead and plot the first section,
					 * then plot the second section.
					 */
					if (j != -1 && j != 0)
						g.drawPolyline (X, Y, j);
					j = -1;
				}
			}
			if (j > 0) g.drawPolyline (X, Y, j);
		}

		/************ Draw the LightRays *************/
		if (this.time != 0) {
			g.setColor (COLOR_RAY);
			for (int q = 0; q < lr.size (); q++) {
				g.setColor ( ((LightRay) lr.get (q)).getColor () );
				g.drawPolyline (lX[q], lY[q], nl);
			}
		}

		/*********** Draw the Focii ****************/
		g.setColor (COLOR_FOCUS);
		for (int k = 0; k < focus.size (); k++) {
			Point3d f = (Point3d) focus.get (k);
			g.fillOval ( scalex (f.x) - FSIZE / 2,
						 scaley (f.z) - FSIZE / 2, FSIZE, FSIZE );
		}
	} // end of paint **********************************************************

	/** returns the physical location of pixel at x */
	public double iscalex (int x) {
		return XSCALE * ( ((double) x) / ((double) XSIZE) - 0.5);
	}
	/** returns the physical location of pixel at y */
	public double iscaley (int y) {
		return -YSCALE * ( (((double) y) / (double) YSIZE) - 0.5) + YCENTER;
	}
	/** returns the pixel location of x */
	public int scalex (double x) {
		return (int) (XSIZE * (x / XSCALE + 0.5));
	}
	/** returns the pixel location of y */
	public int scaley (double y) {
		return (int) (YSIZE * ((YCENTER-y) / YSCALE + 0.5));
	}

	/** Private class for ReSizing the frame */
	private class ReSize implements ComponentListener {
		JFrame parent;
		public ReSize (JFrame p) {parent = p;}
		public void componentHidden(ComponentEvent e) {}
		public void componentMoved(ComponentEvent e) {}
		public void componentShown(ComponentEvent e) {}
		public void componentResized(ComponentEvent e) {
			XSIZE = parent.getWidth();
			YSIZE = parent.getHeight();
			PropagateLightRays ();
		}
	} // End of class ReSize
} // end of Class OpticBench ***************************************************
