package seedpod.agents;

import static seedpod.constants.Filepaths.AERODROME_SHAPEFILE;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.projection.Adder;
import seedpod.agents.airspace.AirspaceAgent;
import seedpod.agents.ground.AerodromeAgent;
import seedpod.agents.ground.BaseGroundAgent;
import seedpod.agents.ground.HospitalAgent;
import seedpod.agents.unmanned.UAVAdder;
import seedpod.agents.unmanned.UAVAgent;
import seedpod.constants.EAirspaceClass;
import seedpod.constants.Filepaths;

public class ContextCreator implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("SEEDPOD");
		
		//Create geo projection
		GeographyParameters<Object> geoParams = new GeographyParameters<Object>();
		geoParams.getAdder();
		GeographyFactory geographyFactory = 
				GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> airspaceGeography = geographyFactory.createGeography("airspace_geo",
				context,
				geoParams);
		
		GeometryFactory geometryFactory = new GeometryFactory();
		
		List<Geometry> aerodromes = loadGroundFeatures(AERODROME_SHAPEFILE, context, airspaceGeography, AerodromeAgent.class);
		List<Geometry> hospitals = loadGroundFeatures(HOSPITAL_SHAPEFILE, context, airspaceGeography, HospitalAgent.class);
		
		List<Geometry> airspace = new ArrayList<>();
		for(int i=0;i<EAirspaceClass.values().length;i++) {
			System.out.println("Loading " + EAirspaceClass.values()[i]);
			airspace.addAll(loadAirspaceFeatures(Filepaths.AIRSPACE_PATH_STRINGS[i], context, airspaceGeography, AirspaceAgent.class, EAirspaceClass.values()[i]));
		}
		
//		int mannedACCount = 5;
//		airspaceGeography.setAdder(new MannedAircraftAdder(aerodromes));
//		Adder mannedAdder = airspaceGeography.getAdder();
//		for(int i=0;i<mannedACCount;i++) {
//			mannedAdder.add(airspaceGeography, new MannedAircraftAgent());
//		}
		
		int uavCount = 20;
		Adder uavAdder = new UAVAdder(hospitals);
		for(int i=0;i<uavCount;i++) {
			UAVAgent agent = new UAVAgent();
			context.add(agent);
			uavAdder.add(airspaceGeography, agent);
		}
		
		return context;
	}
	
	private List<SimpleFeature> loadFeaturesFromShapefile(String filename){
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

			while(fiter.hasNext()){
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(fiter != null) {
				fiter.close();
			}
			store.dispose();
		}
		
		return features;
	}
	
	
	/**
	 * Loads features from the specified shapefile.  The appropriate type of agents
	 * will be created depending on the geometry type in the shapefile (point, 
	 * line, polygon).
	 * 
	 * @param filename the name of the shapefile from which to load agents
	 * @param context the context
	 * @param geography the geography
	 * @param agentClass class of agent to create
	 */
	private List<Geometry> loadGroundFeatures(String filename, Context context, Geography geography, Class<? extends BaseGroundAgent> agentClass){

		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		
		List<Geometry> geometries = new ArrayList<>();
		
		// For each feature in the file
		for (SimpleFeature feature : features){
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			BaseGroundAgent agent = null;

			if (!geom.isValid()){
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			if (geom instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);

				// Read the feature attributes
				String name = (String)feature.getAttribute("name");
				

				try {
					agent = agentClass.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				agent.setReadableName(name);
				agent.setId(UUID.randomUUID().toString());
				agent.setGeometry(geom);
				if(agent instanceof HospitalAgent) {
					boolean isEmergency = "yes".equals(((String)feature.getAttribute("emergency")).toLowerCase());
					((HospitalAgent)agent).setEmergency(isEmergency);
				}
				geometries.add(geom);
			}

			if (agent != null){
				context.add(agent);
				geography.move(agent, geom);
			}
			else{
				System.out.println("Error creating agent for  " + geom);
			}
		}
		
		return geometries;
	}
	
	/**
	 * Loads features from the specified shapefile.  The appropriate type of agents
	 * will be created depending on the geometry type in the shapefile (point, 
	 * line, polygon).
	 * 
	 * @param filename the name of the shapefile from which to load agents
	 * @param context the context
	 * @param geography the geography
	 * @param agentClass class of agent to create
	 */
	private List<Geometry> loadAirspaceFeatures(String filename, Context context, Geography geography, Class<? extends AirspaceAgent> agentClass, EAirspaceClass airspaceClass){

		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		
		List<Geometry> geometries = new ArrayList<>();
		
		// For each feature in the file
		for (SimpleFeature feature : features){
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			AirspaceAgent agent = null;

			if (!geom.isValid()){
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			if (geom instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);				

				try {
					agent = agentClass.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				agent.setAirspaceClass(airspaceClass);
				agent.setPolygon(geom);
				geometries.add(geom);
			}

			if (agent != null){
				context.add(agent);
				geography.move(agent, geom);
			}
			else{
				System.out.println("Error creating agent for  " + geom);
			}
		}
		
		return geometries;
	}

}
