package seedpod.agents.meta;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class BaseMarker{

	public Coordinate coordinate;

	public BaseMarker(Coordinate coordinate) {
		super();
		this.coordinate = coordinate;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	
}