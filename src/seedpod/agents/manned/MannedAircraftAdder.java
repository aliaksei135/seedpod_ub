package seedpod.agents.manned;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;
import seedpod.agents.ground.AerodromeAgent;
import seedpod.agents.ground.BaseGroundAgent;

public class MannedAircraftAdder extends BaseGISAdder {
	
	private int numLayers;
	private double verticalLayerSeparation;
	private double baseAltitude;

	public MannedAircraftAdder(List<BaseGroundAgent> groundAgents) {
		super(groundAgents);
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.numLayers = (int) Math.floor(p.getDouble("Manned_LayersPer360"));
		this.verticalLayerSeparation = p.getDouble("Manned_RequiredVerticalSeparationM");
		this.baseAltitude = p.getDouble("Manned_BaseAltitude");
	}

	@Override
	public void add(Geography<Object> projection, Object object) {

		int originIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		AerodromeAgent originAerodromeAgent = (AerodromeAgent) this.groundAgents.get(originIndex);
		Geometry originGeometry = originAerodromeAgent.geometry;
		
		Coordinate destinationCoordinate = null;
		// Ignore GA traffic for now
		//TODO: create GA traffic route cases
		if(!originAerodromeAgent.isGA()) {
			int destinationIndex = RandomHelper.nextIntFromTo(0, NavFixes.DepartureArrivalFixes.values().length - 1);
			destinationCoordinate = NavFixes.DepartureArrivalFixes.values()[destinationIndex].getCoordinate();

		}
		
		MannedAircraftAgent agent = (MannedAircraftAgent) object;

		
		Coordinate originCoordinate = originGeometry.getCoordinate();
		double dy = destinationCoordinate.y - originCoordinate.y;
		double dx = Math.cos(Math.PI / 180 * originCoordinate.y)
				* (destinationCoordinate.x - originCoordinate.x);
		double angleRad = Math.atan2(dy, dx);
		
		agent.setTargetAltitude(getTargetAltitude(angleRad));
		agent.setDestination(destinationCoordinate);
		projection.move(agent, originGeometry);
	}
	
	private double getTargetAltitude(double destinationBearingRad) {
		int layerNum = (int) Math.floor(destinationBearingRad/Math.floor(2*Math.PI/this.numLayers));
		return this.baseAltitude + (layerNum*this.verticalLayerSeparation);
	}

}
