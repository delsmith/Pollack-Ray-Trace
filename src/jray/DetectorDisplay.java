package jray;

import javax.vecmath.Point2d;
import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * All Detectors should contain a DetectorDisplay.  Contains a list of
 * points which have been absorbed by the Detector.  Paints points
 * to a graphics object.
 */
public class DetectorDisplay extends JFrame {

	/** Maximum spot size (returned for spot sizes less than 0) */
	public final static double SPOTMAX = 1000.;
	/** Number of tick marks on the display */
	private final static int TICK = 5;

	private int DOT    = 1;
	private int BORDER = 50;
	private int SIZE   = 0;
	private double SCALE   = 5.0;
	private double ySCALE  = 5.0;
	private double LINEAR  = 0;
	private double iLIN    = 0;
	private double yLINEAR = 0;
	private double yiLIN   = 0;
	private double xcenter = 0;
	private double ycenter = 0;
	private Vector points;
	private Vector colors;

	/** Create display with title and linear size of detector */
	public DetectorDisplay (String title, double s) {
		setTitle (title);
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		this.LINEAR  = s;
		this.iLIN    = s;
		this.yLINEAR = s;
		this.yiLIN   = s;
		this.SIZE    = 500;
		this.SCALE   = this.SIZE / this.LINEAR;
		this.ySCALE   = this.SIZE / this.yLINEAR;
		setSize (SIZE + 2 * BORDER, SIZE + 2 * BORDER);
		this.points = new Vector ();
		this.colors = new Vector ();

		// Add a TextBox for dynamic zooming
		TextField zxbox = new TextField ("ZX", 7);
		TextField zybox = new TextField ("ZY", 7);
		JPanel panel = new JPanel ();
		panel.add (zxbox, BorderLayout.EAST);
		panel.add (zybox, BorderLayout.EAST);
		TextField xbox = new TextField ("X", 5);
		panel.add (xbox, BorderLayout.EAST);
		TextField ybox = new TextField ("Y", 5);
		panel.add (ybox, BorderLayout.EAST);
		getContentPane ().add (panel, BorderLayout.NORTH);
		zxbox.addActionListener(new Zoomed (zxbox, 0));
		zybox.addActionListener(new Zoomed (zybox, 1));
		xbox.addActionListener(new Center (xbox, 0));
		ybox.addActionListener(new Center (ybox, 1));
		addComponentListener (new ReSize (this));
	} // end of constructor ****************************************************

	/** display frame */
	public void display () {this.setVisible (true);}

	/** add a 2D point with color c to the display */
	public void add (Point2d p, Color c) {
		points.add (p);
		colors.add (c);
	}
	public Vector getPoints () { return points; }
	public Vector getColors () { return colors; }

	/** paint the display */
	public void paint (Graphics g) {
		g.setColor (Color.WHITE);
		g.clearRect (0, 0, SIZE*10, SIZE*10);
		g.setColor (Color.BLACK);
		g.setFont ( new Font ( "Dialog", Font.PLAIN, 9 ) );

		// Draw Axes
		g.drawLine (BORDER, SIZE+BORDER, SIZE+BORDER, SIZE+BORDER);
		g.drawLine (BORDER, BORDER, BORDER, SIZE+BORDER);
		g.drawString ("Both Axes in mm", SIZE/2, SIZE + (int)(1.5 * BORDER));
		int nTicks = 11;
		for (int i = 0; i < nTicks; i++) {
			double xVal = -1.0 + 2.0 * ((double) i) / (nTicks - 1.0);
			double yVal = -1.0 + 2.0 * ((double) i) / (nTicks - 1.0);
			xVal *= LINEAR / 2.0;
			yVal *= yLINEAR / 2.0;
			xVal += xcenter;
			yVal += ycenter;
			xVal = Fix (xVal, 3);
			yVal = Fix (yVal, 3);
			double xPos = LINEAR / 2.0 + xVal - xcenter;
			double yPos = yLINEAR / 2.0 + yVal - ycenter;
			g.drawString (new Double(xVal).toString (),
						  (int)(xPos * SCALE) + BORDER - 5, 
						  SIZE+BORDER+TICK+10);
			g.drawString (new Double(yVal).toString (), 10, 
						  (int)(-yPos * ySCALE) + SIZE + BORDER + 5);
			g.drawLine ((int)(xPos * SCALE) + BORDER, SIZE+BORDER-TICK,
						(int)(xPos * SCALE) + BORDER, SIZE+BORDER+TICK);
			g.drawLine (BORDER-TICK, (int)(-yPos * ySCALE) + SIZE + BORDER,
						BORDER+TICK, (int)(-yPos * ySCALE) + SIZE + BORDER );
		}

		// Draw Dots
		for (int i = 0; i < points.size (); i++) {
			Point2d p = (Point2d) points.get (i);
			g.setColor ( (Color) colors.get (i) );
			g.fillOval ((int)((p.x - xcenter) * SCALE + BORDER + SIZE/2.0),
						(int)((p.y + ycenter) * ySCALE + BORDER + SIZE/2.0),
						DOT, DOT);
		}
	} // end of paint **********************************************************

	/** Fixes number of digits after decimal place */
	private static double Fix (double x, int p) {
		int e = 0;
		while (x != 0 && Math.abs (x) < 1.0 &&
			   Math.abs (x) * Math.pow (10.0, e) < 1.0)
			e++;
		double y = ((double) Math.round (x * Math.pow (10.0, p+e))) /
			Math.pow (10.0, p+e);
		return y;
	}

	/** Removes all points from the DetectorDisplay */
	public void clean () {
		this.points = new Vector ();
		this.colors = new Vector ();
	}
	/** Set the size of the circular pixels */
	public void setPixel (int i) {this.DOT = Math.abs (i);}
	/** returns linear dimension of display */
	public int getDim () {return (SIZE + 2 * BORDER);}
	/** set (x, y) center of display */
	public void setCenter (double x, double y) {
		xcenter = x;
		ycenter = y;
		repaint ();
	}
	/** set zoom of display */
	public void setZoom (double zoom) { setZoom (zoom, zoom); }
	public void setZoom (double zoom, double yzoom) {
		LINEAR = iLIN * zoom;
		yLINEAR = yiLIN * yzoom;
		if (LINEAR == 0)
			LINEAR = iLIN;
		if (yLINEAR == 0)
			yLINEAR = yiLIN;
		SCALE = SIZE / LINEAR;
		ySCALE = SIZE / yLINEAR;
		repaint ();
	} 

	/** Private class for resizing the frame */
	private class ReSize implements ComponentListener {
		JFrame parent;
		public ReSize (JFrame p) {parent = p;}
		public void componentHidden(ComponentEvent e) {}
		public void componentMoved(ComponentEvent e) {}
		public void componentShown(ComponentEvent e) {}
		public void componentResized(ComponentEvent e) {
			int w = parent.getWidth();
			int h = parent.getHeight();
			SIZE = (w < h) ? w : h;
			SIZE -= 2 * BORDER;
			SCALE = SIZE / LINEAR;
			ySCALE = SIZE / yLINEAR;
			parent.setSize (SIZE + 2 * BORDER, SIZE + 2 * BORDER);
		}
	} // End of class ReSize ***************************************************

	/** Private class for real time zooming */
	private class Zoomed implements ActionListener {
		TextComponent parent;
		int xy;
		public Zoomed (TextComponent p, int i) { parent = p; xy = i; }
		public void actionPerformed (ActionEvent e) {
			double zoom = new Double(parent.getText ()).doubleValue ();
			if (xy == 0) {
				LINEAR = iLIN * zoom;
				if (LINEAR == 0)
					LINEAR = iLIN;
				SCALE = SIZE / LINEAR;
			} else {
				yLINEAR = yiLIN * zoom;
				if (yLINEAR == 0)
					yLINEAR = yiLIN;
				ySCALE = SIZE / yLINEAR;
			}
			repaint ();
		}
	} // End of class Zoomed ***************************************************

	/** Private class for real time centering */
	private class Center implements ActionListener {
		TextComponent parent;
		int xy;
		public Center (TextComponent p, int i) {parent = p; xy = i;}
		public void actionPerformed (ActionEvent e) {
			double center = new Double(parent.getText ()).doubleValue ();
			if (xy == 0)
				xcenter = center;
			else
				ycenter = center;
			repaint ();
		}
	} // End of class Center ***************************************************

	/** computes the spot size on the detector by finding
	 * the maximal distance between points.
	 */
	public double getSpotSize () {
		/** Compute the spot size in mm
		 * This will simply be the maximum
		 * distance between any two points.
		 */
		double spot = 0;
		for (int i = 0; i < points.size (); i++) {
			Point2d n = (Point2d) points.get (i);
			for (int j = 0; j < points.size (); j++) {
				Point2d m = (Point2d) points.get (j);
				double r = Math.sqrt ( Math.pow ((n.x - m.x), 2.0) +
									   Math.pow ((n.y - m.y), 2.0) );
				spot = (r > spot) ? r : spot;
			}
		}
		if (spot <= 0)
			spot = SPOTMAX;
		return spot;
	} // end of getSpotSize ****************************************************

	/** computes the spot size in the x-direction */
	public double getXSpotSize () {
		double spot = 0;
		for (int i = 0; i < points.size (); i++) {
			double x1 = ((Point2d) points.get (i)).x;
			for (int j = 0; j < points.size (); j++) {
				double x2 = ((Point2d) points.get (j)).x;
				double r = Math.abs (x1 - x2);
				spot = (r > spot) ? r : spot;
			}
		}
		if (spot <= 0)
			spot = SPOTMAX;
		return spot;
	} // end of getXSpotSize ***************************************************

	/** computes the spot size in the y-direction */
	public double getYSpotSize () {
		double spot = 0;
		for (int i = 0; i < points.size (); i++) {
			double y1 = ((Point2d) points.get (i)).y;
			for (int j = 0; j < points.size (); j++) {
				double y2 = ((Point2d) points.get (j)).y;
				double r = Math.abs (y1 - y2);
				spot = (r > spot) ? r : spot;
			}
		}
		if (spot <= 0)
			spot = SPOTMAX;
		return spot;
	} // end of getYSpotSize ***************************************************
}// end of Class DetectorDisplay ***********************************************
