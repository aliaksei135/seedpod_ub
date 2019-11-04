package seedpod.agents.meta;

import com.vividsolutions.jts.geom.Coordinate;

public class Marker {

	Coordinate coordinate;

	public Marker(Coordinate coordinate) {
		super();
		this.coordinate = coordinate;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}
	
}