package seedpod.styles;

import java.awt.Color;
import java.awt.Font;

import org.geotools.coverage.processing.operation.Scale;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.WWTexture;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.EditedMarkStyle;
import repast.simphony.visualization.gis3D.style.MarkStyle;
import seedpod.agents.BaseAircraftAgent;

public abstract class BaseAircraft3DStyle implements MarkStyle<BaseAircraftAgent> {
	
	protected WWTexture texture;

	@Override
	public abstract WWTexture getTexture(BaseAircraftAgent object, WWTexture texture);

	@Override
	public PlaceMark getPlaceMark(BaseAircraftAgent object, PlaceMark mark) {
		if (mark == null) {
			mark = new PlaceMark();
		}
	
		mark.setAltitudeMode(WorldWind.ABSOLUTE);
		mark.setLineEnabled(false);
	
		return mark;
	}

	@Override
	public Offset getIconOffset(BaseAircraftAgent obj) {
		return Offset.CENTER;
	}

	@Override
	public double getElevation(BaseAircraftAgent obj) {
		return obj.getCurrentAltitude();
	}

	@Override
	public double getScale(BaseAircraftAgent obj) {
		return 0.3;
	}

	@Override
	public double getHeading(BaseAircraftAgent obj) {
		return Math.toRadians(obj.getFlightpathBearing());
	}

	@Override
	public String getLabel(BaseAircraftAgent obj) {
		return "";
	}

	@Override
	public Color getLabelColor(BaseAircraftAgent obj) {
		return null;
	}

	@Override
	public Font getLabelFont(BaseAircraftAgent obj) {
		return null;
	}

	@Override
	public Offset getLabelOffset(BaseAircraftAgent obj) {
		return null;
	}

	@Override
	public double getLineWidth(BaseAircraftAgent obj) {
		return 0.01;
	}

	@Override
	public Material getLineMaterial(BaseAircraftAgent obj, Material lineMaterial) {
		if (lineMaterial == null) {
			lineMaterial = Material.BLACK;
		}
		return lineMaterial;
	}
}
