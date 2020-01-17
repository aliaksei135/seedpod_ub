package seedpod.agents.manned;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.parameter.Parameters;
import seedpod.agents.BaseAircraftAgent;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.constants.EAirspaceClass;
import static seedpod.constants.Constants.*;

public class MannedAircraftAgent extends BaseAircraftAgent {

	private static final double REQ_LATERAL_SEP_M = 5500; // About 3nm

	private static final List<EAirspaceClass> PERMISSABLE_AIRSPACE_CLASSES = 			
		List.of(EAirspaceClass.CLASS_A,
			EAirspaceClass.CLASS_C,
			EAirspaceClass.CLASS_D,
			EAirspaceClass.CLASS_E,
			EAirspaceClass.CLASS_F,
			EAirspaceClass.CLASS_G);

	public MannedAircraftAgent() {
		super();
		Parameters p = RunEnvironment.getInstance().getParameters();
		lateralMaxSpeedMPS = p.getDouble("Manned_LateralMaxSpeedMPS");
		verticalMaxSpeedMPS = p.getDouble("Manned_VerticalMaxSpeedMPS");
		requiredVerticalSeparationM = p.getDouble("Manned_RequiredVerticalSeparationM");
	}

	@Override
	@ScheduledMethod(start = 0)
	public void setup() {
		super.setup();
	}
	
	

	@Override
	public void fly() {
		super.fly();
		
		//When a/c climbs above ceiling remove it
		if(this.currentAltitude > CEILING_FT) destroy();
	}

	@Watch(watcheeClassName = "seedpod.agents.BaseAircraftAgent",
			watcheeFieldNames = "airborne", //Ignore if not airborne
			query = "within " + REQ_LATERAL_SEP_M + " 'airspace_geo'",
			scheduleTriggerPriority = ScheduleParameters.FIRST_PRIORITY,
//			triggerCondition = "$watcher.airborne",
			shuffle = false,
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE )
	@Override
	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		// Manned Aircraft will have priority
//		super.onBufferInfringed(conflictingAgent);
	}

	@Override
	public List<AirspaceAgent> getAirspaceObstacles() {
		// Iterate through all objects in projection
		// TODO: split objects into projection layers to reduce wasteful searching
		Iterator objIterator = this.geography.getAllObjects().iterator();
		List<AirspaceAgent> airspaceObstacles = new ArrayList<>();
		while (objIterator.hasNext()) {
			Object obj = objIterator.next();
			if (obj instanceof AirspaceAgent) {
				AirspaceAgent airspaceAgent = (AirspaceAgent) obj;
				// Check if agent allowed into airspace class
				// add it as an obstacle if not allowed into it
				if (!PERMISSABLE_AIRSPACE_CLASSES.contains(airspaceAgent.getAirspaceClass())) {
					airspaceObstacles.add(airspaceAgent);
				}
			}
		}
		return airspaceObstacles;
	}

}
