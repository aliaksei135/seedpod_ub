package seedpod.agents.ground;

import repast.simphony.parameter.Parameter;

public class HospitalAgent extends BaseGroundAgent {

	public HospitalAgent() {

	}

	public boolean isEmergency;

	@Parameter(displayName = "Has A&E", usageName = "emergency")
	public boolean isEmergency() {
		return isEmergency;
	}

	public void setEmergency(boolean isEmergency) {
		this.isEmergency = isEmergency;
	}

}
