package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;

public class Terminal extends GeomPoint {
	public City connectedCity;
	public Road connectedRoad;
	public Airport airport;
	
	public Terminal(float x, float y, float rx, float ry, String name) {
		super(name, x, y, rx, ry);
		this.connectedCity = null;
		this.airport = null;
		this.connectedRoad = null;
	}
	
	public void setConnectedCity(City c) {
		this.connectedCity = c;
	}
	
	public void setConnectedRoad(Road r) {
		this.connectedRoad = r;
	}
	
	public void setAirport(Airport a) {
		this.airport = a;
	}
}
