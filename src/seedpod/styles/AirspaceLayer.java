package seedpod.styles;

import gov.nasa.worldwind.render.airspaces.Airspace;
import repast.simphony.visualization.gis3D.AbstractRenderableLayer;

public class AirspaceLayer extends AbstractRenderableLayer<Airspace3DStyle, Airspace>{

	public AirspaceLayer(String name, Airspace3DStyle style) {
		super(name, style);
	}

	@Override
	protected void applyUpdatesToShape(Object o) {
		// This is handled internally by AirspaceAgent as it subclasses Airspace
	}

	@Override
	protected Airspace createVisualItem(Object o) {
		return (Airspace) o;
	}

}
