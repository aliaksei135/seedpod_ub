package seedpod.agents.ground;

public class AerodromeAgent extends BaseGroundAgent {

	public boolean isGA;
	public boolean isMilitary;
	
	public AerodromeAgent() {

	}

	public boolean isGA() {
		return isGA;
	}

	public void setGA(boolean isGA) {
		this.isGA = isGA;
	}

	public boolean isMilitary() {
		return isMilitary;
	}

	public void setMilitary(boolean isMilitary) {
		this.isMilitary = isMilitary;
	}

	
}
