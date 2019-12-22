package seedpod.agents.manned;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;
import seedpod.agents.ground.AerodromeAgent;
import seedpod.agents.ground.BaseGroundAgent;

public class MannedAircraftAdder extends BaseGISAdder {

	public MannedAircraftAdder(List<BaseGroundAgent> groundAgents) {
		super(groundAgents);
	}

	@Override
	public void add(Geography<Object> projection, Object object) {

		int originIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		AerodromeAgent originAerodromeAgent = (AerodromeAgent) this.groundAgents.get(originIndex);
		Geometry originGeometry = originAerodromeAgent.geometry;
		
		// Ignore GA traffic for now
		//TODO: create GA traffic route cases
		if(!originAerodromeAgent.isGA()) {
			int destinationIndex = RandomHelper.nextIntFromTo(0, NavFixes.DepartureArrivalFixes.values().length - 1);
			Coordinate destination = NavFixes.DepartureArrivalFixes.values()[destinationIndex].getCoordinate();
	
			MannedAircraftAgent agent = (MannedAircraftAgent) object;
			agent.setDestination(destination);
			projection.move(agent, originGeometry);
		}
	}

}
