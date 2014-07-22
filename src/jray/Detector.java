package jray;


/** a two dimensional detector of LightRays */
public interface Detector {

	/** Returns the DetectorDisplay associated with this Detector */
	public DetectorDisplay getDisplay ();
	/** absorbs a LightRay */
	public LightRay reflect (LightRay i);
	/** displays the DetectorDisplay */
	public void display ();
	/** repaints the DetectorDisplay */
	public void repaint ();
	/** cleans the DetectorDisplay */
	public void clean ();

}
