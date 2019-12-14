package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;

public abstract class GeomPoint extends Point2D.Float {
	
	protected String name;
	protected final Point2D.Float rp;
	
	public GeomPoint(String name, float x, float y, float rx, float ry) {
		this.x = x;
		this.y = y;
		this.name = name;
		this.rp = new Point2D.Float(rx, ry);
	}
	
	public String getName() {
		return name;
	}
	
	public Point2D.Float getRemotePoint() {
		return rp;
	}

}
