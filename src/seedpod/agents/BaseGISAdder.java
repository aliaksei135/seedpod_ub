package seedpod.agents;

import java.util.List;

import repast.simphony.space.gis.GISAdder;
import repast.simphony.space.gis.Geography;
import seedpod.agents.ground.BaseGroundAgent;

public class BaseGISAdder implements GISAdder<Object> {
	
	protected List<BaseGroundAgent> groundAgents;
	
	public BaseGISAdder(List<BaseGroundAgent> groundAgents) {
		this.groundAgents = groundAgents;
	}

	@Override
	public void add(Geography<Object> destination, Object object) {

	}

}
