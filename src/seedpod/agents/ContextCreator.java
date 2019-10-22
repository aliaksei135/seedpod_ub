package seedpod.agents;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.poi.xslf.model.geom.Guide;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import seedpod.agents.ground.AerodromeAgent;
import seedpod.agents.ground.BaseGroundAgent;
import seedpod.agents.ground.HospitalAgent;

public class ContextCreator implements ContextBuilder<Object> {
	
	static final String HOSPITAL_SHAPEFILE = "data/hospitals.shp";
	static final String AERODROME_SHAPEFILE = "data/aerodromes.shp";
	static final String GEOFEATURES_SHAPEFILE = "data/geofeatures.shp";

	@Override
	public Context build(Context<Object> context) {
		context.setId("seedpod");
		
		//Create geo projection
		GeographyParameters<Object> geoParams = new GeographyParameters<Object>();
		geoParams.getAdder();
		GeographyFactory geographyFactory = 
				GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> airspaceGeography = geographyFactory.createGeography("airspace_geo",
				context,
				geoParams);
//		Geography<Object> physicalGeography = geographyFactory.createGeography("physical_geo",
//				context,
//				geoParams);
		
		GeometryFactory geometryFactory = new GeometryFactory();
		
		loadFeatures(AERODROME_SHAPEFILE, context, airspaceGeography, AerodromeAgent.class);
		loadFeatures(HOSPITAL_SHAPEFILE, context, airspaceGeography, HospitalAgent.class);
		
		
		
		return context;
	}
	
	private List<SimpleFeature> loadFeaturesFromShapefile(String filename){
		URL url = null;
		try {
			url = new File(filename).toURI().toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
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
			fiter.close();
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
	private void loadFeatures (String filename, Context context, Geography geography, Class<? extends BaseGroundAgent> agentClass){

		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		
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
				double taxRate = (double)feature.getAttribute("Tax_Rate");

				try {
					agent = agentClass.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				agent.setReadableName(name);
				agent.setId(UUID.randomUUID().toString());
				agent.setGeometry(geom);
				if(agent instanceof HospitalAgent) {
					boolean isEmergency = (boolean)feature.getAttribute("emergency");
					((HospitalAgent)agent).setEmergency(isEmergency);
				}
				
			}

//			// For Points, create RadioTower agents
//			else if (geom instanceof Point){
//				geom = (Point)feature.getDefaultGeometry();				
//
//				// Read the feature attributes and assign to the ZoneAgent
//				String name = (String)feature.getAttribute("Name");
//				agent = new RadioTower(name);
//			}
//
//			// For Lines, create WaterLines
//			else if (geom instanceof MultiLineString){
//				MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
//				geom = (LineString)line.getGeometryN(0);
//
//				// Read the feature attributes and assign to the ZoneAgent
//				String name = (String)feature.getAttribute("Name");
//				double flowRate = (Long)feature.getAttribute("Flow_Rate");
//				agent = new WaterLine(name, flowRate);
//			}

			if (agent != null){
				context.add(agent);
				geography.move(agent, geom);
			}
			else{
				System.out.println("Error creating agent for  " + geom);
			}
		}				
	}

}
