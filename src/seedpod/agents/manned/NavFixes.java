package seedpod.agents.manned;

import com.vividsolutions.jts.geom.Coordinate;

public final class NavFixes {
	
	
	public static enum DepartureArrivalFixes {
		PEPIS(new Coordinate(-1.243667, 51.196667)), //North of HI, near Popham(HP)
		GWC(new Coordinate(-0.756833, 50.855167)), //West of HI, at Goodwood (HR)
		NEDUL(new Coordinate(-1.547833, 50.666167)); //South of HI, near The Needles
	
		private final Coordinate coordinate;
		
		private DepartureArrivalFixes(Coordinate coordinate) {
			this.coordinate = coordinate;
		}
		
		public Coordinate getCoordinate() {
			return this.coordinate;
		}
	}
	
	
	public static enum HoldingFixes {
		SAM(new Coordinate(-1.345000, 50.955333)); //Southampton VOR
		
		private final Coordinate coordinate;
		
		private HoldingFixes(Coordinate coordinate) {
			this.coordinate = coordinate;
		}
		
		public Coordinate getCoordinate() {
			return this.coordinate;
		}
	}
}
