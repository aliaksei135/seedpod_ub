package seedpod.agents.meta;

import com.vividsolutions.jts.geom.Coordinate;

public class AirproxMarker extends BaseMarker {
	
	public boolean isSameTypeConflict = true;
	public boolean isUAVConflictType = true;
	
	public double elevation = 0;

	public AirproxMarker(Coordinate coordinate) {
		super(coordinate);
	}

	public double getElevation() {
		return this.elevation;
	}
}
