package seedpod.agents.manned;

import org.stringtemplate.v4.compiler.CodeGenerator.includeExpr_return;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.relogo.ide.dynamics.NetLogoSystemDynamicsParser.intg_return;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseAircraftAgent;

public class MannedAircraftAgent extends BaseAircraftAgent {
	
	private static final int SEP_DIST_M = 5500; //About 3nm


	public MannedAircraftAgent() {
		super();
		speedMPS = 100; //About 200kts
	}

//	@Watch(watcheeClassName = "seedpod.agents.BaseAircraftAgent",
//			watcheeFieldNames = "airborne", //Ignore if not airborne
//			query = "within " + SEP_DIST_M,
//			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	@Override
	public void onBufferInfringed() {
		super.onBufferInfringed();
		
	}
	
	

}
