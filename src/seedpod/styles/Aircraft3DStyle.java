package seedpod.styles;

import gov.nasa.worldwind.WorldWind;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.EditedMarkStyle;
import seedpod.agents.BaseAircraftAgent;

public class Aircraft3DStyle extends EditedMarkStyle {

	public Aircraft3DStyle(String userStyleFile) {
		super(userStyleFile);
	}

	@Override
	public double getElevation(Object obj) {
		return ((BaseAircraftAgent) obj).getCurrentAltitude();
	}

	@Override
	public double getHeading(Object obj) {
		return ((BaseAircraftAgent) obj).getFlightpathBearing();
	}

	@Override
	public PlaceMark getPlaceMark(Object object, PlaceMark mark) {
		if (mark == null) {
			mark = new PlaceMark();
		}

		mark.setAltitudeMode(WorldWind.ABSOLUTE);
		mark.setLineEnabled(false);

		return mark;
	}
	
	
}
