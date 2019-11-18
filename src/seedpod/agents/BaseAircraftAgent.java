package seedpod.agents;

import static seedpod.constants.Constants.WAYPOINT_BOUNDARY;
import static seedpod.constants.Constants.ORIGIN_RSIVA_TICKS;
import static seedpod.constants.Constants.SIM_TICK_SECS;
import static seedpod.constants.Constants.AVOIDANCE_MOMENTUM;

import java.util.ArrayList;
import java.util.List;

import com.dongbat.walkable.FloatArray;
import com.dongbat.walkable.PathHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.agents.meta.Marker;

public abstract class BaseAircraftAgent {
	
	protected Coordinate currentPosition;
	protected Coordinate nextPoint;
	protected double flightpathBearing;
	protected Coordinate destination;
	protected ArrayList<Coordinate> pathCoords;
	protected int pathIndex = 0;
	
	protected boolean airborne = false;
	private boolean nextPointDestination = false;
	private boolean onPath = false;
	protected boolean inTCAS = false;
	protected int speedMPS;
	
	protected Context context;
	protected Geography geography;
	private int simTickLife = 0;
	
	public BaseAircraftAgent() {
		this.pathCoords = new ArrayList<>(10);
	}
	
	@ScheduledMethod(start = 0)
	public void setup() {
		this.context = ContextUtils.getContext(this);
		this.geography = (Geography)context.getProjection("airspace_geo");
	}


	@ScheduledMethod(start = 1, interval = 1)
	public void fly() {
		
		// Prevent double stepping after avoidance manoeuvres
		if(this.inTCAS) {
			this.inTCAS = false;
			return;
		}
		
		this.currentPosition = this.geography.getGeometry(this).getCoordinate();
		
		//Replan path if not on it
		if(!onPath) findPath();
		
		this.nextPoint = this.pathCoords.get(this.pathIndex);
		//Find angle between current position and next point in path
		//This assumes points are close together and far from poles
		double dy = nextPoint.y - this.currentPosition.y;
		double dx = Math.cos(Math.PI/180*this.currentPosition.y)*(nextPoint.x-this.currentPosition.x);
		double dist = Math.sqrt(dy*dy + dx*dx);
		double angleRad = Math.atan2(dy, dx);
		if(angleRad < 0) {
			angleRad = angleRad + 2*Math.PI;
		}
		this.flightpathBearing = angleRad;
		
		if(dist < WAYPOINT_BOUNDARY) {
			if(this.nextPointDestination) {
				//Check if at final destination, if so destroy agent
				destroy();
				return;
			} else {
				this.pathIndex++;
				if(this.pathCoords.size()-2 == this.pathIndex) {
					this.nextPointDestination = true;
				}
			}
		}
		
		this.geography.moveByVector(this, this.speedMPS*SIM_TICK_SECS, angleRad);
		
		airborne = true;
		this.simTickLife++;
	}

	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		//Prevent self-detection
		if(conflictingAgent.hashCode() == this.hashCode()) return;
		
		//Try not to calculate distance where possible 
		if(this.simTickLife > ORIGIN_RSIVA_TICKS) {
			performAvoidanceActions(conflictingAgent);
		}else if(this.nextPointDestination) {
			double dy = this.destination.y - this.currentPosition.y;
			double dx = Math.cos(Math.PI/180*this.currentPosition.y)*(this.destination.x-this.currentPosition.x);
			double dist = Math.sqrt(dy*dy + dx*dx);
			if(dist < WAYPOINT_BOUNDARY) {
				performAvoidanceActions(conflictingAgent);
			}
		}
	}
	
	public void performAvoidanceActions(BaseAircraftAgent conflictingAgent) {
		this.onPath = false;
		this.inTCAS = true;
		dropMarker();
		Coordinate conflicterCoordinate = conflictingAgent.currentPosition;
		double dy = conflicterCoordinate.y - this.currentPosition.y;
		double dx = Math.cos(Math.PI/180*this.currentPosition.y)*(conflicterCoordinate.x-this.currentPosition.x);
		double angleRad = Math.atan2(dy, dx);
		double maxAvoidanceAngleRad = (angleRad + Math.PI);
		double avoidanceAngleRad = (AVOIDANCE_MOMENTUM*(maxAvoidanceAngleRad-this.flightpathBearing) + this.flightpathBearing) % (2*Math.PI);
		this.geography.moveByVector(this, this.speedMPS*SIM_TICK_SECS, avoidanceAngleRad);
	}
	
	public void dropMarker() {
		Marker marker = new Marker(this.currentPosition);
		this.context.add(marker);
		Geometry currentPos = this.geography.getGeometry(this);
		((Geography)this.context.getProjection("airspace_geo")).move(marker, currentPos);
	}
	
	public abstract List<AirspaceAgent> getAirspaceObstacles();
	
	public void findPath() {
		System.out.println("Planning path");
		this.pathCoords.clear();
		
		List<AirspaceAgent> obstaclesList = getAirspaceObstacles();
		
		PathHelper pathHelper = new PathHelper(180, 90);
		
		for(AirspaceAgent obstacle : obstaclesList) {
			Coordinate[] coords = obstacle.getPolygon().getCoordinates();
			float[] coordArray = new float[coords.length*2];
			for(int i=0;i<coords.length;i++) {
				coordArray[2*i] = (float) coords[i].x;
				coordArray[(2*i)+1] = (float) coords[i].y;
			}
			pathHelper.addPolygon(coordArray);
		}
		
		FloatArray path = new FloatArray();
		pathHelper.findPath((float)this.currentPosition.x, (float)this.currentPosition.y,
				(float)this.destination.x, (float)this.destination.y,
				0, path);
		
		for(int i=0;i<path.size/2;i++) {
			Coordinate coord = new Coordinate((double)path.items[2*i], path.items[(2*i)+1]);
			this.pathCoords.add(coord);
		}
		
		//Add destination point on end
		this.pathCoords.add(this.destination);
		
		// Reset index for new path
		this.pathIndex = 0;
		this.onPath = true;
		
		if(this.pathCoords.size() <= 1) {
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
}
