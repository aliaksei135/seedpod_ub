package seedpod.agents;

import static seedpod.constants.Constants.DESTINATION_BOUNDARY;
import static seedpod.constants.Constants.ORIGIN_RSIVA_TICKS;
import static seedpod.constants.Constants.SIM_TICK_SECS;

import java.util.ArrayList;
import java.util.List;

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
	
//	@ScheduledMethod(start = 1)
//	public void setup() {
//		this.context = ContextUtils.getContext(this);
//		this.geography = (Geography)context.getProjection("airspace_geo");
//	}


	@ScheduledMethod(start = 1, interval = 1)
	public void fly() {
		
		// Prevent double stepping after avoidance manoeuvres
		if(this.inTCAS) {
			this.inTCAS = false;
			return;
		}
		
		this.context = ContextUtils.getContext(this);
		this.geography = (Geography)context.getProjection("airspace_geo");
		
		//Replan path if not on it
		if(!onPath) findPath();
		
		this.currentPosition = this.geography.getGeometry(this).getCoordinate();
		this.nextPoint = this.pathCoords.get(this.pathIndex);
		//Find angle between current position and next point in path
		//This assumes points are close together and far from poles
		//which are both true of solent region
		double dy = nextPoint.y - this.currentPosition.y;
		double dx = Math.cos(Math.PI/180*this.currentPosition.y)*(nextPoint.x-this.currentPosition.x);
		double dist = Math.sqrt(dy*dy + dx*dx);
		double angleRad = Math.atan2(dy, dx);
		if(angleRad < 0) {
			angleRad = angleRad + 2*Math.PI;
		}
		
		if(dist < DESTINATION_BOUNDARY) {
			if(this.pathCoords.size()-1 == this.pathIndex) {
				//Check if at final destination, if so destroy agent
				destroy();
				return;
			} else {
				this.pathIndex++;
			}
		}
		
		this.geography.moveByVector(this, this.speedMPS*SIM_TICK_SECS, angleRad);
		
		airborne = true;
		this.simTickLife++;
	}

	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		//Prevent self-detection
		if(conflictingAgent.hashCode() == this.hashCode()) return;
		
		if(this.simTickLife > ORIGIN_RSIVA_TICKS) {
			this.onPath = false;
			dropMarker();
			this.inTCAS = true;
			Coordinate conflicterCoordinate = conflictingAgent.currentPosition;
			double dy = conflicterCoordinate.y - this.currentPosition.y;
			double dx = Math.cos(Math.PI/180*this.currentPosition.y)*(conflicterCoordinate.x-this.currentPosition.x);
			double angleRad = Math.atan2(dy, dx);
			double avoidanceAngleRad = (angleRad + Math.PI) % (2*Math.PI);
			this.geography.moveByVector(this, this.speedMPS*SIM_TICK_SECS, avoidanceAngleRad);
		}
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
		Coordinate currentPos = this.geography.getGeometry(this).getCoordinate();
		
		List<AirspaceAgent> obstaclesList = getAirspaceObstacles();
		
		//TODO find path around obstacles defined by points
		this.pathCoords.add(this.destination);
		// Reset index for new path
		this.pathIndex = 0;
		this.onPath = true;
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
