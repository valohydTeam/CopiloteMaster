package com.valohyd.copilotemaster.models;

import com.google.android.gms.maps.model.LatLng;

public class POI {

	private int id;
	private int type;
	private LatLng location;

	public POI(int type, LatLng location) {
		super();
		this.type = type;
		this.location = location;
	}

	public POI() {
		this.type = -1;
		this.location = null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "POI [ID=" + id + " type=" + type + ", location=" + location
				+ "]";
	}

}
