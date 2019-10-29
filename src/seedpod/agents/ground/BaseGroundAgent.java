package seedpod.agents.ground;

import java.util.List;

import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.space.gis.FeatureAgent;
import repast.simphony.space.gis.Geography;

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
