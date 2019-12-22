package seedpod.agents.unmanned;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;
import seedpod.agents.ground.BaseGroundAgent;

public class UAVAdder extends BaseGISAdder {

	public UAVAdder(List<BaseGroundAgent> groundAgents) {
		super(groundAgents);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void add(Geography<Object> projection, Object object) {
		super.add(projection, object);

		int originIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		int destinationIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		while (originIndex == destinationIndex) {
			destinationIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		}

		Geometry origin = this.groundAgents.get(originIndex).geometry;
		Coordinate destination = this.groundAgents.get(destinationIndex).geometry.getCoordinate();

		UAVAgent agent = (UAVAgent) object;
		agent.setDestination(destination);
		projection.move(agent, origin);
	}

}
