package seedpod.agents;

import repast.simphony.visualization.gis3D.style.DefaultMarkStyle;
import repast.simphony.visualization.gis3D.style.EditedMarkStyle;

public class Aircraft3DStyle extends EditedMarkStyle{

	
	public Aircraft3DStyle(String userStyleFile) {
		super(userStyleFile);
	}


	@Override
	public double getElevation(Object obj) {
		return ((BaseAircraftAgent)obj).currentAltitude;
	}

	
	@Override
	public double getHeading(Object obj) {
		return ((BaseAircraftAgent)obj).flightpathBearing;
	}
	
	
}
