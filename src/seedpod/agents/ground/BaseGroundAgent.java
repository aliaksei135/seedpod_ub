package seedpod.agents.ground;

import java.util.List;

import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.space.gis.FeatureAgent;
import repast.simphony.space.gis.Geography;

public class BaseGroundAgent extends FeatureAgent{
	
	public BaseGroundAgent(SimpleFeatureType type, Object agent, Geography geog, List adapters,
			List classAttributeList) {
		super(type, agent, geog, adapters, classAttributeList);
	}
	public String id;
	public String readableName;
	public Geometry geometry;
	
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
