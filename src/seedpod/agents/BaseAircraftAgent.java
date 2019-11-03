package seedpod.agents;

import java.awt.desktop.SystemSleepEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.taskdefs.Get;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.agents.ground.BaseGroundAgent;
import seedpod.constants.EAirspaceClass;

import static seedpod.constants.Constants.*;

public abstract class BaseAircraftAgent {
	
	protected Coordinate destination;
	protected ArrayList<Coordinate> pathCoords;
	protected int pathIndex = 0;
	
	protected boolean airborne = false;
	private boolean nextPointDestination = false;
	private boolean onPath = false;
	protected int speedMPS;
	
	protected Context context;
	protected Geography geography;
	
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
		
		this.context = ContextUtils.getContext(this);
		this.geography = (Geography)context.getProjection("airspace_geo");
		
		//Replan path if not on it
		if(!onPath) findPath();
		
		Coordinate currentPos = this.geography.getGeometry(this).getCoordinate();
		Coordinate nextPoint = this.pathCoords.get(this.pathIndex);
		//Find angle between current position and next point in path
		//This assumes points are close together and far from poles
		//which are both true of solent region
		double dy = nextPoint.y - currentPos.y;
		double dx = Math.cos(Math.PI/180*currentPos.y)*(nextPoint.x-currentPos.x);
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
	}

	public void onBufferInfringed(BaseAircraftAgent conflictingAgent) {
		onPath = false;
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
