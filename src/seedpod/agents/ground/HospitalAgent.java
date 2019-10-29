package seedpod.agents.ground;

import java.util.List;

import org.opengis.feature.simple.SimpleFeatureType;

import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;

public class HospitalAgent extends BaseGroundAgent {
	
	public HospitalAgent() {
		
	}
	
	public boolean isEmergency;

	public boolean isEmergency() {
		return isEmergency;
	}

	public void setEmergency(boolean isEmergency) {
		this.isEmergency = isEmergency;
	}
	
}
