package seedpod.agents.airspace;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContextImpl;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.Polygon;
import seedpod.constants.EAirspaceClass;

public class AirspaceAgent extends Polygon {

	private EAirspaceClass airspaceClass;

	public EAirspaceClass getAirspaceClass() {
		return airspaceClass;
	}

	public void setAirspaceClass(EAirspaceClass airspaceClass) {
		this.airspaceClass = airspaceClass;
	}

	public void updateSurfaceShape(SurfaceShape shape) {
		this.updateSurfaceShape(new DrawContextImpl(), shape);
	}

	public void setCoords(Coordinate[] coords) {
		List<LatLon> latLons = new ArrayList<>(coords.length);
		for (Coordinate coordinate : coords) {
			Angle lat = Angle.fromDegreesLatitude(coordinate.y);
			Angle lon = Angle.fromDegreesLongitude(coordinate.x);
			LatLon latLon = new LatLon(lat, lon);
			latLons.add(latLon);
		}
		this.setLocations(latLons);
	}
	
	public boolean isAtAltitude(double altitude) {
		return altitude <= this.upperAltitude
				&& altitude >= this.lowerAltitude;
	}

}