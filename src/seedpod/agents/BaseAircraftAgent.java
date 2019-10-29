package seedpod.agents;

import java.awt.desktop.SystemSleepEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import seedpod.agents.ground.BaseGroundAgent;
import static seedpod.constants.Constants.*;

public abstract class BaseAircraftAgent {
	
	protected Coordinate destination;
	protected ArrayList<Coordinate> pathCoords;
	protected int pathIndex = 0;
	
	protected boolean airborne = false;
	protected boolean onPath;
	protected int speedMPS;
	
	protected Context context;
	protected Geography geography;
	
	public BaseAircraftAgent() {
		this.pathCoords = new ArrayList<>(10);
		onPath = false;
	}
	
//	@ScheduledMethod(start = 1)
//	public void setup() {
//		this.context = ContextUtils.getContext(this);
//		this.geography = (Geography)context.getProjection("airspace_geo");
//	}


	@ScheduledMethod(start = 1, interval = 1)
	public void fly() {
		System.out.println("Flying");
		
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
		double angleRad = Math.atan2(dy, dx);
		if(angleRad < 0) {
			angleRad = angleRad + 2*Math.PI;
		}
		System.out.println(dy);
		System.out.println(dx);
		System.out.println(angleRad);
		
		this.geography.moveByVector(this, this.speedMPS*SIM_TICK_SECS, angleRad);
		this.pathIndex++;
		
		airborne = true;
	}

	public void onBufferInfringed() {
		onPath = false;
	}
	
	public void moveAlongPath() {
		
	}
	
//	public boolean isBufferInfringed() {
//		
//	}
	
	public void findPath() {
		System.out.println("Planning path");
		Coordinate currentPos = this.geography.getGeometry(this).getCoordinate();
		
		Iterator objIterator = geography.getAllObjects().iterator();
		List<Object> obstaclesList = new ArrayList<>();
		while(objIterator.hasNext()) {
			Object obj = objIterator.next();
			if (obj instanceof BaseGroundAgent) { //TODO change this to airspace class
				obstaclesList.add(obj);
			}
		}
		
		//TODO find path around obstacles defined by points
		this.pathCoords.add(this.destination);
		// Reset index for new path
		this.pathIndex = 0;
	}


	public Coordinate getDestination() {
		return destination;
	}


	public void setDestination(Coordinate destination) {
		this.destination = destination;
	}
}
