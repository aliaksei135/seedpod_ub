package seedpod.agents.unmanned;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import seedpod.agents.BaseGISAdder;
import seedpod.agents.ground.BaseGroundAgent;

public class UAVAdder extends BaseGISAdder {
	
	private int numLayers;
	private double verticalLayerSeparation;
	private double baseAltitude;

	public UAVAdder(List<BaseGroundAgent> groundAgents) {
		super(groundAgents);
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.numLayers = (int) Math.floor(p.getDouble("UAV_LayersPer360"));
		this.verticalLayerSeparation = p.getDouble("UAV_RequiredVerticalSeparationM");
		this.baseAltitude = p.getDouble("UAV_BaseAltitude");
	}

	@Override
	public void add(Geography<Object> projection, Object object) {
		super.add(projection, object);

		int originIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		int destinationIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		while (originIndex == destinationIndex) {
			destinationIndex = RandomHelper.nextIntFromTo(0, this.groundAgents.size() - 1);
		}

		Geometry originGeometry = this.groundAgents.get(originIndex).geometry;
		Coordinate destinationCoordinate = this.groundAgents.get(destinationIndex).geometry.getCoordinate();

		UAVAgent agent = (UAVAgent) object;
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
