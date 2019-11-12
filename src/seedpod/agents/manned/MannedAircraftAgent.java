package seedpod.agents.manned;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import seedpod.agents.BaseAircraftAgent;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.constants.EAirspaceClass;

public class MannedAircraftAgent extends BaseAircraftAgent {
	
	private static final int SEP_DIST_M = 5500; //About 3nm
	
	private static final List<EAirspaceClass> PERMISSABLE_AIRSPACE_CLASSES = 
			List.of(EAirspaceClass.CLASS_A,
					EAirspaceClass.CLASS_C,
					EAirspaceClass.CLASS_D,
					EAirspaceClass.CLASS_E,
					EAirspaceClass.CLASS_F,
					EAirspaceClass.CLASS_G);

	public MannedAircraftAgent() {
		super();
		speedMPS = 100; //About 200kts
	}

	@Watch(watcheeClassName = "seedpod.agents.BaseAircraftAgent",
			watcheeFieldNames = "airborne", //Ignore if not airborne
			query = "within " + SEP_DIST_M,
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
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
