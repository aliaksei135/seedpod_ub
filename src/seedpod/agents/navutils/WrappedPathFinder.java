package seedpod.agents.navutils;

import java.util.ArrayList;
import straightedge.geom.KPoint;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathData;
import straightedge.geom.path.PathFinder;

public class WrappedPathFinder extends PathFinder {
	
	NodeConnector nodeConnector;
	ArrayList<PathBlockingObstacleImpl> obstacles;
	
	
	public WrappedPathFinder(NodeConnector nodeConnector, ArrayList<PathBlockingObstacleImpl> obstacles) { //Must be ArrayList not interface type fsr
		super();
		this.nodeConnector = nodeConnector;
		this.obstacles = obstacles;
	}
	
	public PathData calc(KPoint start, KPoint end) {
		return this.calc(start, end, 0.2, this.nodeConnector, this.obstacles);
	}

}
