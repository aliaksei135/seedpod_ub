package seedpod.agents;

import static seedpod.constants.Constants.*;

import java.util.ArrayList;
import java.util.List;

import com.dongbat.walkable.FloatArray;
import com.dongbat.walkable.PathHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import gov.nasa.worldwind.geom.LatLon;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.GISNetworkListener;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.agents.meta.AirproxMarker;
import seedpod.agents.meta.RouteMarker;

public abstract class BaseAircraftAgent {

	/* Agent motion fields */
	protected Coordinate currentPosition;
	protected double currentAltitude;
	protected Coordinate nextPoint;
	protected double flightpathBearing;
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
		// angleRad uses angles from a standard set of x-y axes to +90deg would be North
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

		airborne = true;
		this.simTickLife++;
	}

	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		// Prevent self-detection
		if (conflictingAgent.hashCode() == this.hashCode())
			return;
		
		// Check for vertical separation
		double verticalSep = Math.abs(this.currentAltitude - conflictingAgent.currentAltitude);
		if(verticalSep > this.requiredVerticalSeparationM) return;
		
		// Check if just departing
		if (this.simTickLife <= ORIGIN_RSIVA_TICKS) return;

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

	public void findPath() {
		System.out.println("Planning path");
		this.pathCoords.clear();

		List<AirspaceAgent> obstaclesList = getAirspaceObstacles();

		PathHelper pathHelper = new PathHelper((float)Math.abs(MAX_LON-MIN_LON), (float)Math.abs(MAX_LAT-MIN_LAT));
		
		for(AirspaceAgent obstacle : obstaclesList){
			List<LatLon> coords = obstacle.getLocations();
			float[] coordArray = new float[coords.size()*2];
			for(int i=0;i<coords.size();i++) {
				coordArray[2*i] = (float) (coords.get(i).getLongitude().getDegrees()-MIN_LON);
				coordArray[(2*i)+1] = (float) (coords.get(i).getLatitude().getDegrees()-MIN_LAT);
			}
			try {
				pathHelper.addPolygon(coordArray);
			}catch (Exception e) {
				continue;
			}
		}
		
		FloatArray path = new FloatArray();
		pathHelper.findPath((float)(this.currentPosition.x-MIN_LON), (float)(this.currentPosition.y-MIN_LAT),
				(float)(this.destination.x-MIN_LON), (float)(this.destination.y-MIN_LAT),
				1e-7f, path);
		
		for(int i=0;i<path.size/2;i++) {
			Coordinate coord = new Coordinate(path.items[2*i]+MIN_LON, path.items[(2*i)+1]+MIN_LAT);
			this.pathCoords.add(coord);
		}

		// Add destination point on end
		this.pathCoords.add(this.destination);

		// Reset index for new path
		this.pathIndex = 0;
		this.onPath = true;

		if (this.pathCoords.size() <= 1) {
			this.nextPointDestination = true;
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
