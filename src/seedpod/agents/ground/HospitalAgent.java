package seedpod.agents.ground;

import seedpod.agents.BaseGISAdder;

public class HospitalAgent extends BaseGroundAgent {
	
	public boolean isEmergency;

	public boolean isEmergency() {
		return isEmergency;
	}

	public void setEmergency(boolean isEmergency) {
		this.isEmergency = isEmergency;
	}
	
}
