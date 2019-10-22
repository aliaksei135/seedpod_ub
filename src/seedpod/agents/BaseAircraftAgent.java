package seedpod.agents;

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
	
	protected Geometry destination;
	protected ArrayList<Coordinate> pathCoords;
	protected int pathIndex = 0;
	
	protected boolean airborne = false;
	protected boolean onPath = true;
	protected int speedMPS;
	
	protected Context context;
	protected Geography geography;
	
	public BaseAircraftAgent(Geometry destination) {
		this.destination = destination;
		this.pathCoords = new ArrayList<>(10);
		this.context = ContextUtils.getContext(this);
		this.geography = (Geography)context.getProjection("airspace_geo");
	}


	@ScheduledMethod(start = 1, interval = 1)
	public void fly() {
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
		Coordinate currentPos = this.geography.getGeometry(this).getCoordinate();
		Coordinate destinationPos = this.destination.getCoordinate();
		
		Iterator objIterator = geography.getAllObjects().iterator();
		List<Object> obstaclesList = new ArrayList<>();
		while(objIterator.hasNext()) {
			Object obj = objIterator.next();
			if (obj instanceof BaseGroundAgent) { //TODO change this to airspace class
				obstaclesList.add(obj);
			}
		}
		
		//TODO find path around obstacles defined by points
		this.pathCoords.add(destinationPos);
		// Reset index for new path
		this.pathIndex = 0;
	}
}
