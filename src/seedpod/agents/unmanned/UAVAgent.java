package seedpod.agents.unmanned;

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

public class UAVAgent extends BaseAircraftAgent {

	private static final double REQ_LATERAL_SEP_M = 500; 

	private static final List<EAirspaceClass> PERMISSABLE_AIRSPACE_CLASSES = List.of(EAirspaceClass.CLASS_D,
			EAirspaceClass.CLASS_E, EAirspaceClass.CLASS_F, EAirspaceClass.CLASS_G);

	public UAVAgent() {
		super();
		Parameters p = RunEnvironment.getInstance().getParameters();
		lateralMaxSpeedMPS = p.getDouble("UAV_LateralMaxSpeedMPS");
		verticalMaxSpeedMPS = p.getDouble("UAV_VerticalMaxSpeedMPS");
		requiredVerticalSeparationM = p.getDouble("UAV_RequiredVerticalSeparationM");
	}

	@Override
	@ScheduledMethod(start = 0)
	public void setup() {
		super.setup();
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
		super.onBufferInfringed(conflictingAgent);
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
				// Check that airspace is at the same altitude as us
				// otherwise it is not an obstacle
				if(!airspaceAgent.isAtAltitude(this.targetAltitude)) continue;
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
