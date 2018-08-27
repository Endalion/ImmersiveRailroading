package cam72cam.immersiverailroading.library;

import cam72cam.immersiverailroading.util.TextUtil;

public enum TrackItems {
	STRAIGHT,
	CROSSING,
	SLOPE,
	TURN,
	SWITCH,
	TURNTABLE,
	INSPECTION
	;
	
	@Override
	public String toString() {
	    return TextUtil.translate("track.immersiverailroading:class." + super.toString().toLowerCase()); 
	}

	public boolean isTurn() {
		return this == TURN;
	}
}