package seedpod.styles;

import java.awt.Color;
import static seedpod.constants.Constants.AIRSPACE_COLOUR_MAP;
import seedpod.agents.airspace.AirspaceAgent;

import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.EditedSurfaceShapeStyle;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.constants.EAirspaceClass;

public class Airspace3DStyle extends EditedSurfaceShapeStyle {

	public Airspace3DStyle(String userStyleFile) {
		super(userStyleFile);
	}

//	@Override
//	public SurfaceShape getSurfaceShape(Object object, SurfaceShape shape) {
//		((AirspaceAgent) object).updateSurfaceShape(shape);
//		return shape;
//	}
	
	@Override
	public Color getFillColor(Object obj) {
		EAirspaceClass airspaceClass = ((AirspaceAgent)obj).getAirspaceClass();
		return AIRSPACE_COLOUR_MAP.get(airspaceClass);
	}

}
