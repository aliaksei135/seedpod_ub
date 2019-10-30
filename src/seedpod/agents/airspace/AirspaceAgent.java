package seedpod.agents.airspace;

import com.vividsolutions.jts.geom.Geometry;

import seedpod.constants.EAirspaceClass;

public class AirspaceAgent {
	
	Geometry polygon;
	EAirspaceClass airspaceClass;

	public AirspaceAgent() {
		
	}

	public Geometry getPolygon() {
		return polygon;
	}

	public void setPolygon(Geometry polygon) {
		this.polygon = polygon;
	}

	public EAirspaceClass getAirspaceClass() {
		return airspaceClass;
	}

	public void setAirspaceClass(EAirspaceClass airspaceClass) {
		this.airspaceClass = airspaceClass;
	}
	
	
}
