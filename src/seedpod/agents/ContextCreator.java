package seedpod.agents;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;

public class ContextCreator implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("seedpod");
		
		//Create geo projection
		GeographyParameters<Object> geoParams = new GeographyParameters<Object>();
		geoParams.getAdder()
		GeographyFactory geographyFactory = 
				GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> geography = geographyFactory.createGeography("solent_geo",
				context,
				geoParams);
		
		
		
		return context;
	}

}
