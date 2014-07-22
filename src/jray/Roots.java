package jray;
/**
 * A Java wrapper class for the C roots solver functions.
 */

public class Roots {
	public native int SolveQuartic (double [] c, double [] s);
	public native int SolveQuadratic (double [] c, double [] s);
	static {
		System.loadLibrary ("roots");
	}
}
