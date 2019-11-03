package seedpod.agents.unmanned;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseAircraftAgent;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.constants.EAirspaceClass;

public class UAVAgent extends BaseAircraftAgent {
	
	private static final int SEP_DIST_M = 1000; //About 0.5nm
	
	private static final List<EAirspaceClass> PERMISSABLE_AIRSPACE_CLASSES = 
			List.of(EAirspaceClass.CLASS_D,
					EAirspaceClass.CLASS_E,
					EAirspaceClass.CLASS_F,
					EAirspaceClass.CLASS_G);
	
	public UAVAgent() {
		super();
		speedMPS = 20; //About 40kts
	}


	@Watch(watcheeClassName = "seedpod.agents.BaseAircraftAgent",
			watcheeFieldNames = "airborne", //Ignore if not airborne
			query = "within " + SEP_DIST_M ,
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
	@Override
	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		super.onBufferInfringed(conflictingAgent);
	}


	@Override
	public List<AirspaceAgent> getAirspaceObstacles() {
		//Iterate through all objects in projection
		//TODO: split objects into projection layers to reduce wasteful searching
		Iterator objIterator = this.geography.getAllObjects().iterator();
		List<AirspaceAgent> airspaceObstacles = new ArrayList<>();
		while(objIterator.hasNext()) {
			Object obj = objIterator.next();
			if(obj instanceof AirspaceAgent) {
				AirspaceAgent airspaceAgent = (AirspaceAgent)obj;
				//Check if agent allowed into airspace class
				//add it as an obstacle if not allowed into it
				if(!PERMISSABLE_AIRSPACE_CLASSES
						.contains(airspaceAgent.getAirspaceClass())) {
					airspaceObstacles.add(airspaceAgent);
				}
			}
		}
		return airspaceObstacles;
	}
	
	

}
