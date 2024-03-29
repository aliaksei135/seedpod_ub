package seedpod.constants;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class Constants {

	public static final int SIM_TICK_SECS = 2; // Number of real seconds per simulation tick
	public static final double WAYPOINT_BOUNDARY = 5e-3; // Assume agent at waypoint if within this distance from it
	public static final int ORIGIN_RSIVA_TICKS = 20; // Ignore buffer infringements within this many ticks of origin
	public static final float AVOIDANCE_MOMENTUM = 0.8f; // (0-1.0) Momentum value from previous flightpath bearing. 0
															// makes agent move directly away from conflicting agent.
															// 1.0 makes agent move along flightpath bearing prior to conflict
	
	public static final double MESH_LAYER_SPACING_M = 152.4; // Vertical spacing between airspace navmesh layers in metres
	public static final double MESH_COORD_SCALE_FACTOR = 10; // Scale up all coords to avoid issues with polygon buffers being larger than actual geometry
	
	/* Set sim area limits */
	public static final double MAX_LAT = 51.3;
	public static final double MIN_LAT = 50.5;
	public static final double MAX_LON = -0.2;
	public static final double MIN_LON = -2.6;
	public static final double CEILING_FT = 5500;
	
	/* Airspace Colouring */
	public static final Map<EAirspaceClass, Color> AIRSPACE_COLOUR_MAP = Map.of(
			EAirspaceClass.DANGER, new Color(255, 20, 20),
			EAirspaceClass.PROHIBITED, new Color(170, 0, 127),
			EAirspaceClass.RESTRICTED, new Color(0, 85, 0),
			EAirspaceClass.CLASS_A, Color.MAGENTA,
			EAirspaceClass.CLASS_C, Color.CYAN,
			EAirspaceClass.CLASS_D, new Color(216, 191, 216),
			EAirspaceClass.CLASS_E, new Color(172, 225, 175),
			EAirspaceClass.CLASS_F, Color.LIGHT_GRAY,
			EAirspaceClass.CLASS_G, Color.DARK_GRAY);

}
