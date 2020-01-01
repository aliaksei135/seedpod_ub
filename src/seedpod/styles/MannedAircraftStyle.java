package seedpod.styles;

import static seedpod.constants.Filepaths.MANNED_ICON;

import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.WWTexture;
import seedpod.agents.BaseAircraftAgent;

public class MannedAircraftStyle extends BaseAircraft3DStyle {

	@Override
	public WWTexture getTexture(BaseAircraftAgent object, WWTexture texture) {
		if(texture == null) {
			if(this.texture == null) {
				this.texture = new BasicWWTexture(MANNED_ICON);
			}
			texture = this.texture;
		}
		
		return texture;
	}

}
