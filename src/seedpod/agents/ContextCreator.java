package seedpod.agents;

import static seedpod.constants.Constants.MAX_LAT;
import static seedpod.constants.Constants.MAX_LON;
import static seedpod.constants.Constants.MIN_LAT;
import static seedpod.constants.Constants.MIN_LON;
import static seedpod.constants.Filepaths.AERODROME_SHAPEFILE;
import static seedpod.constants.Filepaths.AIRSPACE_SHAPEFILE;
import static seedpod.constants.Filepaths.HOSPITAL_SHAPEFILE;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.projection.Adder;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.agents.ground.AerodromeAgent;
import seedpod.agents.ground.BaseGroundAgent;
import seedpod.agents.ground.HospitalAgent;
import seedpod.agents.manned.MannedAircraftAdder;
import seedpod.agents.manned.MannedAircraftAgent;
import seedpod.agents.manned.NavFixes;
import seedpod.agents.manned.NavFixes.DepartureArrivalFixes;
import seedpod.agents.meta.NavFixMarker;
import seedpod.agents.unmanned.UAVAdder;
import seedpod.agents.unmanned.UAVAgent;
import seedpod.constants.EAirspaceClass;

public class ContextCreator implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("SEEDPOD");

		// Create geo projection
		GeographyParameters<Object> geoParams = new GeographyParameters<>();
		geoParams.getAdder();
		GeographyFactory geographyFactory = GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> airspaceGeography = geographyFactory.createGeography("airspace_geo", context, geoParams);
		
		NetworkBuilder<Object> networkBuilder = new NetworkBuilder<Object>("routes", context, true);
		Network<Object> routeNetwork = networkBuilder.buildNetwork();

		GeometryFactory geometryFactory = new GeometryFactory();
//		new GISNetworkListener(context, airspaceGeography, routeNetwork);

		List<BaseGroundAgent> aerodromes = loadGroundFeatures(AERODROME_SHAPEFILE, context, airspaceGeography,
				AerodromeAgent.class);
		List<BaseGroundAgent> hospitals = loadGroundFeatures(HOSPITAL_SHAPEFILE, context, airspaceGeography,
				HospitalAgent.class);
		List<AirspaceAgent> airspace = loadAirspaceFeatures(AIRSPACE_SHAPEFILE, context, airspaceGeography);

		// Add entry/exit NavFixes
		for(DepartureArrivalFixes nf : NavFixes.DepartureArrivalFixes.values()) {
			context.add(new NavFixMarker(nf.getCoordinate()));
		}
		
		// Add agents
		int mannedACCount = 5;
		Adder mannedAdder = new MannedAircraftAdder(aerodromes);
		for(int i=0;i<mannedACCount;i++) {
			MannedAircraftAgent agent = new MannedAircraftAgent();
			context.add(agent);
			mannedAdder.add(airspaceGeography, agent);
		}

		int uavCount = 30;
		Adder uavAdder = new UAVAdder(hospitals);
		for (int i = 0; i < uavCount; i++) {
			UAVAgent agent = new UAVAgent();
			context.add(agent);
			uavAdder.add(airspaceGeography, agent);
		}
		
		RunEnvironment.getInstance().endAt(4000);

		return context;
	}

	private static List<SimpleFeature> loadFeaturesFromShapefile(String filename) {
		URL url = null;
		try {
			url = new File(filename).toURI().toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<>();

		// Try to load the shapefile
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();

			while (fiter.hasNext()) {
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fiter != null) {
				fiter.close();
			}
			store.dispose();
		}

		return features;
	}

	/**
	 * Loads features from the specified shapefile. The appropriate type of agents
	 * will be created depending on the geometry type in the shapefile (point, line,
	 * polygon).
	 * 
	 * @param filename   the name of the shapefile from which to load agents
	 * @param context    the context
	 * @param geography  the geography
	 */
	private static List<BaseGroundAgent> loadGroundFeatures(String filename, Context context, Geography geography,
			Class<? extends BaseGroundAgent> agentClass) {

		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);

		List<BaseGroundAgent> agents = new ArrayList<>();

		// For each feature in the file
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			BaseGroundAgent agent = null;

			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}

			Coordinate centroidCoordinate = null;
			if (geom instanceof MultiPolygon) {
				MultiPolygon mp = (MultiPolygon) feature.getDefaultGeometry();
				geom = mp.getGeometryN(0);
				centroidCoordinate = geom.getCoordinate();
			} else if (geom instanceof Point) {
				centroidCoordinate = geom.getCoordinate();
			}
			
			if(centroidCoordinate == null) continue;
				
			//Check if centroid within sim areas bounds
			if(centroidCoordinate.x > MAX_LON 
					|| centroidCoordinate.x < MIN_LON
					|| centroidCoordinate.y > MAX_LAT
					|| centroidCoordinate.y < MIN_LAT) {
				continue;
			}

			// Read the feature attributes
			String name = (String) feature.getAttribute("name");

			try {
				agent = agentClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			agent.setReadableName(name);
			agent.setId(UUID.randomUUID().toString());
			agent.setGeometry(geom);
			if (agent instanceof HospitalAgent) {
				boolean isEmergency = "yes".equals(((String) feature.getAttribute("emergency")).toLowerCase());
				((HospitalAgent) agent).setEmergency(isEmergency);
			}
			agents.add(agent);

			if (agent != null) {
				context.add(agent);
				geography.move(agent, geom);
			} else {
				System.out.println("Error creating agent for  " + geom);
			}
		}

		return agents;
	}

	private static List<AirspaceAgent> loadAirspaceFeatures(String filename, Context context, Geography geography) {

		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);

		List<AirspaceAgent> agents = new ArrayList<>();

		// For each feature in the file
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			AirspaceAgent agent = new AirspaceAgent();

			if (geom instanceof MultiPolygon) {
				MultiPolygon mp = (MultiPolygon) feature.getDefaultGeometry();
				geom = mp.getGeometryN(0);
				Coordinate centroidCoordinate = geom.getCoordinate();
				
				//Check if centroid within sim areas bounds
				if(centroidCoordinate.x > MAX_LON 
						|| centroidCoordinate.x < MIN_LON
						|| centroidCoordinate.y > MAX_LAT
						|| centroidCoordinate.y < MIN_LAT) {
					continue;
				}

				String airspaceClassString = ((String) feature.getAttribute("AirSpaceTy")).strip();
				EAirspaceClass airspaceClass = null;
				for (EAirspaceClass aClass : EAirspaceClass.values()) {
					if (aClass.equalsName(airspaceClassString)) {
						airspaceClass = aClass;
						break;
					}
				}
				if (airspaceClass == null)
					continue;

				double baseFT = (double) feature.getAttribute("Base");
				double baseM = baseFT * 0.3048;
				long extrudedFT = (long) feature.getAttribute("Extruded");
				double ceilingM = baseM + (0.3048 * extrudedFT);

				agent.setAirspaceClass(airspaceClass);
				agent.setAltitudes(baseM, ceilingM);
				agent.setCoords(geom.getCoordinates());
				agents.add(agent);

				context.add(agent);
				geography.move(agent, geom);
			}

		}

		return agents;
	}
}
