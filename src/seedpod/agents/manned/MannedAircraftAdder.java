package seedpod.agents.manned;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;

public class MannedAircraftAdder extends BaseGISAdder {

	List<Geometry> aerodromePoints;

	public MannedAircraftAdder(List<Geometry> aerodromePoints) {
		this.aerodromePoints = aerodromePoints;
	}

	@Override
	public void add(Geography<Object> projection, Object object) {

		int originIndex = RandomHelper.nextIntFromTo(0, aerodromePoints.size());
		int destinationIndex = RandomHelper.nextIntFromTo(0, aerodromePoints.size());
		while (originIndex == destinationIndex) {
			destinationIndex = RandomHelper.nextIntFromTo(0, aerodromePoints.size());
		}

		Geometry origin = this.aerodromePoints.get(originIndex);
		Coordinate destination = this.aerodromePoints.get(destinationIndex).getCoordinate();

		MannedAircraftAgent agent = (MannedAircraftAgent) object;
		agent.setDestination(destination);
		projection.move(agent, origin);
	}

}
