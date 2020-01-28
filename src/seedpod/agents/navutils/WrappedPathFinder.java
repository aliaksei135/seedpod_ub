package seedpod.agents.navutils;

import static seedpod.constants.Constants.*;
import java.util.ArrayList;

import straightedge.geom.KPoint;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathData;
import straightedge.geom.path.PathFinder;

public class WrappedPathFinder extends PathFinder {
	
	NodeConnector nodeConnector;
	ArrayList<PathBlockingObstacleImpl> obstacles;
	
	
	public WrappedPathFinder(NodeConnector nodeConnector, ArrayList<PathBlockingObstacleImpl> obstacles) { //Must be ArrayList not generic List type
		super();
		this.nodeConnector = nodeConnector;
		this.obstacles = obstacles;
	}
	
	public PathData calc(KPoint start, KPoint end) {
		// Check if start point is already inside airspace
		double scaledStartX = start.x*MESH_COORD_SCALE_FACTOR;
		double scaledStartY = start.y*MESH_COORD_SCALE_FACTOR;
		KPoint scaledStartPoint = new KPoint(scaledStartX, scaledStartY);
		KPoint scaledEndPoint = new KPoint(end.x*MESH_COORD_SCALE_FACTOR, end.y*MESH_COORD_SCALE_FACTOR);
		
		KPoint outsideOfAirspaceStartKPoint = null;
		for (PathBlockingObstacleImpl obstacleImpl : obstacles) {
			if (obstacleImpl.getOuterPolygon().contains(scaledStartX, scaledStartY)) {
				outsideOfAirspaceStartKPoint = obstacleImpl.getOuterPolygon().getBoundaryPointClosestTo(scaledStartX, scaledStartY);
			}
		}
		
		PathData pathData = null;
		if(outsideOfAirspaceStartKPoint == null) {
			pathData = this.calc(scaledStartPoint, scaledEndPoint, 5000, this.nodeConnector, this.obstacles);
		} else {
			pathData = this.calc(outsideOfAirspaceStartKPoint, end, 5000, this.nodeConnector, this.obstacles);
			pathData.points.add(0, start);
		}
		
		pathData.points.replaceAll(k -> new KPoint(k.x/MESH_COORD_SCALE_FACTOR, k.y/MESH_COORD_SCALE_FACTOR));
		
		return pathData;
	}

}
