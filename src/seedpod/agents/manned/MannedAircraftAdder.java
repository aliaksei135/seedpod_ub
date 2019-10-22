package seedpod.agents.manned;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;

public class MannedAircraftAdder extends BaseGISAdder {
	
	List<Geometry> aerodromePoints;

	public MannedAircraftAdder(List<Geometry> aerodromePoints) {
		this.aerodromePoints = aerodromePoints;
	}
	
	@Override
	public void add(Geography<Object> destination, Object object) {
		super.add(destination, object);
		
		
	}
	

}
