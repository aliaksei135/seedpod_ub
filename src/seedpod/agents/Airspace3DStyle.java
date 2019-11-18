package seedpod.agents;

import java.awt.Color;

import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.EditedMarkStyle;
import repast.simphony.visualization.gis3D.style.EditedSurfaceShapeStyle;
import repast.simphony.visualization.gis3D.style.MarkStyle;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;
import seedpod.agents.airspace.AirspaceAgent;

public class Airspace3DStyle extends EditedSurfaceShapeStyle{

	public Airspace3DStyle(String userStyleFile) {
		super(userStyleFile);
	}

	@Override
	public SurfaceShape getSurfaceShape(Object object, SurfaceShape shape) {
		((AirspaceAgent)object).updateSurfaceShape(shape);
		return shape;
	}
	

}
