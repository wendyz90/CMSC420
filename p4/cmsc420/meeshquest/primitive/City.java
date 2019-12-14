package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;

public class City extends GeomPoint {

	private final Integer radius;
	private final String color;

	public City(String name, float x, float y, float rx, float ry, Integer radius, String color) {
		super(name, x, y, rx, ry);
		this.radius = radius;
		this.color = color;
	}

	public Integer getRadius() {
		return radius;
	}

	public String getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", Math.round(this.x), Math.round(this.y));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			City c = (City) obj;
			return (name.equals(c.getName()) && x == c.x && y == c.y);
		}
		return false;
	}
}
