package jray;


/** A Grating */
public interface Grating {

	public static final int DEFAULT_ORDER = 1;

	/** returns light reflected into order m */
	public LightRay reflect (LightRay i);
	public LightRay reflect (LightRay i, int m);

}
