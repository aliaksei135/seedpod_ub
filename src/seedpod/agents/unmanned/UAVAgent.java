package seedpod.agents.unmanned;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseAircraftAgent;

public class UAVAgent extends BaseAircraftAgent {
	
	private static final int SEP_DIST_M = 1000; //About 0.5nm
		
	
	public UAVAgent() {
		super();
		speedMPS = 20; //About 40kts
	}


	@Watch(watcheeClassName = "seedpod.agents.BaseAircraftAgent",
			watcheeFieldNames = "airborne", //Ignore if not airborne
			query = "within " + SEP_DIST_M,
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
	@Override
	public void onBufferInfringed() {
		super.onBufferInfringed();
	}
	
	

}
