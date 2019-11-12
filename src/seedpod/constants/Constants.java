package seedpod.constants;

public final class Constants {
	
	public static final int SIM_TICK_SECS = 2; //Number of real seconds per simulation tick
	public static final double WAYPOINT_BOUNDARY = 5e-3; //Assume agent at waypoint if within this distance from it
	public static final int ORIGIN_RSIVA_TICKS = 10; //Ignore buffer infringements within this many ticks of origin
	public static final float AVOIDANCE_MOMENTUM = 0.5f; //(0-1.0) Momentum value from previous flightpath bearing. 0 makes agent move directly away from conflicting agent. 1.0 makes agent move along previous flightpath bearing
}
