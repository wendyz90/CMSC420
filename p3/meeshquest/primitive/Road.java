package cmsc420.meeshquest.primitive;

import java.awt.geom.Line2D;

public class Road extends Line2D.Float {

	private City start;
	private City end;
	private double distance;
	private CityNameComparator cnc = new CityNameComparator();
	
	public Road(City start, City end) {
		
		if (cnc.compare(start, end) > 0) {
			this.start = start;
			this.end = end;
		} else {
			this.end = start;
			this.start = end;
		}
		this.x1 = this.start.x;
		this.x2 = this.end.x;
		this.y1 = this.start.y;
		this.y2 = this.end.y;
		this.distance = start.distance(end);
	}
	
	public City getStart() {
		return start;
	}
	
	public City getEnd() {
		return end;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public City getOther(String cityName) {
		if (start.getName().equals(cityName)) {
			return end;
		} else if (end.getName().equals(cityName)) {
			return start;
		} else {
			return null;
		}
	}
	
	public String getCityNameString() {
		return start.getName() + "-" + end.getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			Road r = (Road) obj;
			return (start.equals(r.start) && end.equals(r.end) && distance == r.distance);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final long dBits = java.lang.Double.doubleToLongBits(distance);
		int hash = 31;
		hash = 37 * hash + start.hashCode();
		hash = 37 * hash + end.hashCode();
		return 37 * hash + (int) (dBits ^ (dBits >>> 32));
	}
}
