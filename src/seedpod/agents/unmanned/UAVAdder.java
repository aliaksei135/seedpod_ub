package seedpod.agents.unmanned;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;

public class UAVAdder extends BaseGISAdder {

	// A list of possible departure/arrival points for UAVs
	List<Geometry> uavPoints;

	public UAVAdder(List<Geometry> uavPoints) {
		this.uavPoints = uavPoints;
	}

	@Override
	public void add(Geography<Object> projection, Object object) {
		super.add(projection, object);

		int originIndex = RandomHelper.nextIntFromTo(0, uavPoints.size() - 1);
		int destinationIndex = RandomHelper.nextIntFromTo(0, uavPoints.size() - 1);
		while (originIndex == destinationIndex) {
			destinationIndex = RandomHelper.nextIntFromTo(0, uavPoints.size() - 1);
		}

		Geometry origin = this.uavPoints.get(originIndex);
		Coordinate destination = this.uavPoints.get(destinationIndex).getCoordinate();

		UAVAgent agent = (UAVAgent) object;
		agent.setDestination(destination);
		projection.move(agent, origin);
//		System.out.println("Added UAV");
	}

}
