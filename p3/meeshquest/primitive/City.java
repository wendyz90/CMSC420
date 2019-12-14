package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;

public class City extends Point2D.Float {
	private final String name;
	private final Integer radius;
	private final String color;

	public City(String name, float x, float y, Integer radius, String color) {
		this.name = name;
		this.radius = radius;
		this.color = color;
		this.x = x;
		this.y = y;
	}

	public String getName() {
		return name;
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
