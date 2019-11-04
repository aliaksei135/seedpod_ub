package seedpod.agents.ground;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.parameter.Parameter;

public class BaseGroundAgent{
	
	public String id;
	public String readableName;
	public Geometry geometry;
	
	public BaseGroundAgent() {
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Parameter(usageName = "readableName",
			displayName = "Name")
	public String getReadableName() {
		return readableName;
	}
	public void setReadableName(String readableName) {
		this.readableName = readableName;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	
	
}
