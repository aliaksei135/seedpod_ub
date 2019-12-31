package seedpod.agents.navutils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static seedpod.constants.Constants.MESH_LAYER_SPACING_M;

import org.apache.commons.lang3.tuple.Pair;

import com.vividsolutions.jts.geom.GeometryFactory;

import gov.nasa.worldwind.geom.LatLon;
import seedpod.agents.AirspaceObstacleFetchCallback;
import seedpod.agents.airspace.AirspaceAgent;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;

public class PathFinderFactory {
	
	// Map node connectors and their associated obstacles (navPairs) to a 500ft ceiling-rounded altitude
	private Map<Double, SoftReference<Pair<NodeConnector, ArrayList<PathBlockingObstacleImpl>>>> cache =
			new HashMap<>();
	
	private static PathFinderFactory instance = null;
	
	private PathFinderFactory() {}
	
	public static PathFinderFactory getInstance() {
		if(PathFinderFactory.instance == null) {
			PathFinderFactory.instance = new PathFinderFactory();
		}
		
		return PathFinderFactory.instance;
	}
	
	public WrappedPathFinder getPathFinder(Double altitude, AirspaceObstacleFetchCallback callback) {
		Pair<NodeConnector, ArrayList<PathBlockingObstacleImpl>> navPair = returnFromCacheOrNull(altitude);
		if(navPair == null) {
			navPair = createNavPair(altitude, callback);
		}
		return new WrappedPathFinder(navPair.getLeft(), navPair.getRight());
	}

	private Pair<NodeConnector, ArrayList<PathBlockingObstacleImpl>> createNavPair(Double altitude, AirspaceObstacleFetchCallback callback) {
		List<AirspaceAgent> obstacleAgents = callback.fetchAirspaceObstacles();
		ArrayList<PathBlockingObstacleImpl> polygonObstacles = new ArrayList<>();
		GeometryFactory factory = new GeometryFactory();
		
		for(AirspaceAgent obstacleAgent : obstacleAgents) {
			ArrayList<KPoint> points = new ArrayList<>();
			for(LatLon pointLatLon : obstacleAgent.getLocations()) {
				points.add(new KPoint(pointLatLon.longitude.getDegrees(), pointLatLon.latitude.getDegrees()));
			}
			KPolygon poly = new KPolygon(points);
			polygonObstacles.add(PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly));
		}
				
		NodeConnector nodeConnector = new NodeConnector();
		// Do not connect nodes further apart than this in the Navmesh
		double maxNodeConnectionDistanceDeg = 0.2; // ~22km //TODO Play with this value
		for(PathBlockingObstacleImpl obstacle : polygonObstacles) {
			nodeConnector.addObstacle(obstacle, polygonObstacles, maxNodeConnectionDistanceDeg);
		}
		
		Pair<NodeConnector, ArrayList<PathBlockingObstacleImpl>> navPair = Pair.of(nodeConnector, polygonObstacles);
		
		double roundedAlt = Math.ceil(altitude/MESH_LAYER_SPACING_M)*MESH_LAYER_SPACING_M;
		this.cache.put(roundedAlt, new SoftReference<>(navPair));
		
		return navPair;
	}

	private Pair<NodeConnector, ArrayList<PathBlockingObstacleImpl>> returnFromCacheOrNull(Double altitude) {
		double roundedAlt = Math.ceil(altitude/MESH_LAYER_SPACING_M)*MESH_LAYER_SPACING_M; //Ceiling-round to nearest 500ft

		return this.cache.containsKey(roundedAlt) ? this.cache.get(roundedAlt).get() : null;
	}

}
