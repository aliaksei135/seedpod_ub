package seedpod.agents;

import java.util.List;

import seedpod.agents.airspace.AirspaceAgent;

//TODO: there must be a more elegant way of doing this
public interface AirspaceObstacleFetchCallback {

	public List<AirspaceAgent> fetchAirspaceObstacles();
	
}
