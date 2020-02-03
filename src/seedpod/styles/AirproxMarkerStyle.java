package seedpod.styles;


import gov.nasa.worldwind.render.Offset;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.DefaultMarkStyle;
import seedpod.agents.meta.AirproxMarker;

public class AirproxMarkerStyle extends DefaultMarkStyle<AirproxMarker> {

	@Override
	public PlaceMark getPlaceMark(AirproxMarker object, PlaceMark mark) {
		return super.getPlaceMark(object, mark);
	}

	@Override
	public double getElevation(AirproxMarker obj) {
		return obj.elevation;
	}

	@Override
	public double getLineWidth(AirproxMarker obj) {
		return 0.01;
	}

	@Override
	public Offset getIconOffset(AirproxMarker obj) {
		return Offset.CENTER;
	}
	
	

}
