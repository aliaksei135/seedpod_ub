package seedpod.agents;

import static seedpod.constants.Constants.AVOIDANCE_MOMENTUM;
import static seedpod.constants.Constants.ORIGIN_RSIVA_TICKS;
import static seedpod.constants.Constants.SIM_TICK_SECS;
import static seedpod.constants.Constants.WAYPOINT_BOUNDARY;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.agents.meta.AirproxMarker;
import seedpod.agents.meta.RouteMarker;
import seedpod.agents.navutils.PathFinderFactory;
import seedpod.agents.navutils.WrappedPathFinder;
import straightedge.geom.KPoint;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathData;

public abstract class BaseAircraftAgent implements AirspaceObstacleFetchCallback{

	/* Agent motion fields */
	protected Coordinate currentPosition;
	protected double currentAltitude;
	protected Coordinate nextPoint;
	protected double flightpathBearing; // in radians!
	protected ArrayList<Coordinate> pathCoords;

	/* Agent state fields */
	public boolean airborne = false;
	private boolean nextPointDestination = false;
	private boolean onPath = false;
	protected boolean inTCAS = false;
	protected int pathIndex = 0;

	/* Agent constant fields */
	protected Coordinate destination;
	protected double requiredVerticalSeparationM;
	protected double lateralMaxSpeedMPS;
	protected double verticalMaxSpeedMPS;
	protected double targetAltitude;

	/* Simulation-related fields */
	protected Context context;
	protected Geography geography;
	private Network routeNetwork;
	private RouteMarker previousRouteMarker;
	private int simTickLife = 0;

	public BaseAircraftAgent() {
		this.pathCoords = new ArrayList<>(10);
	}

	@ScheduledMethod(start = 0)
	public void setup() {
		this.context = ContextUtils.getContext(this);
		this.geography = (Geography) context.getProjection("airspace_geo");
		this.routeNetwork = (Network) context.getProjection("routes");
		this.previousRouteMarker = new RouteMarker(this.currentPosition);
		this.context.add(this.previousRouteMarker);
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void fly() {

		// Prevent double stepping after avoidance manoeuvres
		if (this.inTCAS) {
			this.inTCAS = false;
			return;
		}

		this.currentPosition = this.geography.getGeometry(this).getCoordinate();
		
		if(this.simTickLife % 5 == 0) {
			dropRouteMarker();
		}

		// Replan path if not on it
		if (!onPath)
			findPath();

		this.nextPoint = this.pathCoords.get(this.pathIndex);
		// Find angle between current position and next point in path
		// This assumes points are close together and far from poles
		double dy = nextPoint.y - this.currentPosition.y;
		double dx = Math.cos(Math.PI / 180 * this.currentPosition.y) * (nextPoint.x - this.currentPosition.x);
		double dist = Math.sqrt(dy * dy + dx * dx);

		if (dist < WAYPOINT_BOUNDARY) {
			if (this.nextPointDestination) {
				// Check if at final destination, if so destroy agent
				destroy();
				return;
			} else {
				this.pathIndex++;
				if (this.pathCoords.size() - 2 == this.pathIndex) {
					this.nextPointDestination = true;
				}
			}
		}

		double angleRad = Math.atan2(dy, dx);
		if (angleRad < 0) {
			angleRad = angleRad + 2 * Math.PI;
		}
		// angleRad uses angles from a standard set of x-y axes so +90deg would be North
		// and -90deg would be South
		// rotate this to get the bearing from North
		this.flightpathBearing = 2.5 * Math.PI - angleRad;
		this.flightpathBearing %= 2 * Math.PI;

		double dz;
		if (this.currentAltitude == this.targetAltitude) {
			dz = 0;
		} else {
			dz = verticalMaxSpeedMPS * SIM_TICK_SECS;
			double targetDelZ = this.targetAltitude - this.currentAltitude;
			if (dz > targetDelZ) {
				dz = targetDelZ;
			}
		}

		this.geography.moveByVector(this, this.lateralMaxSpeedMPS * SIM_TICK_SECS, angleRad);
		this.currentAltitude += dz;

		this.airborne = true;
		this.simTickLife++;
	}

	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		// Prevent self-detection
		if (conflictingAgent.hashCode() == this.hashCode())
			return;
		
		// Check if just departing
		if (this.simTickLife <= ORIGIN_RSIVA_TICKS) return;
		
		// Check for vertical separation
		double verticalSep = Math.abs(this.currentAltitude - conflictingAgent.currentAltitude);
		if(verticalSep > this.requiredVerticalSeparationM) return;
		

		// Try not to calculate distance where possible
		if (this.nextPointDestination) {
			double dy = this.destination.y - this.currentPosition.y;
			double dx = Math.cos(Math.PI / 180 * this.currentPosition.y)
					* (this.destination.x - this.currentPosition.x);
			double dist = Math.sqrt(dy * dy + dx * dx);
			if (dist < WAYPOINT_BOUNDARY) {
				performAvoidanceActions(conflictingAgent);
			}
		} else {
			performAvoidanceActions(conflictingAgent);
		} 
	}

	public void performAvoidanceActions(BaseAircraftAgent conflictingAgent) {
		this.onPath = false;
		this.inTCAS = true;
		dropAirproxMarker();
		Coordinate conflicterCoordinate = conflictingAgent.currentPosition;
		double dy = conflicterCoordinate.y - this.currentPosition.y;
		double dx = Math.cos(Math.PI / 180 * this.currentPosition.y)
				* (conflicterCoordinate.x - this.currentPosition.x);
		double angleRad = Math.atan2(dy, dx);
		double maxAvoidanceAngleRad = (angleRad + Math.PI);
		double currentAngleRad = 2.5 * Math.PI - this.flightpathBearing;
		double avoidanceAngleRad = (AVOIDANCE_MOMENTUM * (maxAvoidanceAngleRad - currentAngleRad) + currentAngleRad)
				% (2 * Math.PI);
		this.geography.moveByVector(this, this.lateralMaxSpeedMPS * SIM_TICK_SECS, avoidanceAngleRad);
	}

	public void dropAirproxMarker() {
		AirproxMarker marker = new AirproxMarker(this.currentPosition);
		this.context.add(marker);
		Geometry currentPos = this.geography.getGeometry(this);
		this.geography.move(marker, currentPos);
	}
	
	public void dropRouteMarker() {
		RouteMarker marker = new RouteMarker(this.currentPosition);
		this.context.add(marker);
		RepastEdge edge = new RepastEdge(this.previousRouteMarker, marker, true);
		this.routeNetwork.addEdge(edge);
		this.geography.move(marker, this.geography.getGeometry(this));
	}

	public abstract List<AirspaceAgent> getAirspaceObstacles();

	@Override
	public List<AirspaceAgent> fetchAirspaceObstacles() {
		return this.getAirspaceObstacles();
	}

	public void findPath() {
		this.pathCoords.clear();

		WrappedPathFinder pathFinder = PathFinderFactory.getInstance().getPathFinder(this.currentAltitude, this);
		KPoint startPoint = new KPoint(this.currentPosition.x, this.currentPosition.y);
		KPoint endPoint = new KPoint(this.destination.x, this.destination.y);
		PathData pathData = pathFinder.calc(startPoint, endPoint);

		for(KPoint point : pathData.getPoints()) {
			this.pathCoords.add(new Coordinate(point.x, point.y));
		}

		this.pathIndex = 0;
		if(this.pathCoords.size() < 2) {
			this.pathCoords.clear();
			this.pathCoords.add(this.destination);
		} else {
			this.pathCoords.remove(0); //Remove current position
		}

	}

	public void destroy() {
		this.context.remove(this);
	}

	public Coordinate getDestination() {
		return destination;
	}

	public void setDestination(Coordinate destination) {
		this.destination = destination;
	}

	public double getCurrentAltitude() {
		return this.currentAltitude;
	}

	public double getFlightpathBearing() {
		return this.flightpathBearing;
	}

}
