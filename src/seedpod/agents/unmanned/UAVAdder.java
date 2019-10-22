package seedpod.agents.unmanned;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;

public class UAVAdder extends BaseGISAdder {
	
	// A list of possible departure/arrival points for UAVs
	List<Geometry> uavPoints;
	
	public UAVAdder(List<Geometry> uavPoints) {
		this.uavPoints = uavPoints;
	}

	@Override
	public void add(Geography<Object> destination, Object object) {
		super.add(destination, object);
				
	}
	
}
