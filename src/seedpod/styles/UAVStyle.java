package seedpod.styles;

import static seedpod.constants.Filepaths.UAV_ICON;

import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.WWTexture;
import seedpod.agents.BaseAircraftAgent;

public class UAVStyle extends BaseAircraft3DStyle {

	@Override
	public WWTexture getTexture(BaseAircraftAgent object, WWTexture texture) {
		if(texture == null) {
			if(this.texture == null) {
				this.texture = new BasicWWTexture(UAV_ICON);
			}
			texture = this.texture;
		}
		
		return texture;
	}

}
