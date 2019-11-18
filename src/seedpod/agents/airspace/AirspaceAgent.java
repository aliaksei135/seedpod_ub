package seedpod.agents.airspace;

import com.vividsolutions.jts.geom.Geometry;

import seedpod.constants.EAirspaceClass;

public class AirspaceAgent {
	
	Geometry polygon;
	EAirspaceClass airspaceClass;
	double baseM;
	double ceilingM;

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

	public double getBaseM() {
		return baseM;
	}

	public void setBaseM(double baseM) {
		this.baseM = baseM;
	}

	public double getCeilingM() {
		return ceilingM;
	}

	public void setCeilingM(double ceilingM) {
		this.ceilingM = ceilingM;
	}
	
	
}