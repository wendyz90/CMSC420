package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Airport extends GeomPoint {

	public List<Terminal> associatedTerminals;
	
	public Airport(float x, float y, float rx, float ry, String name) {
		super(name, x, y, rx, ry);
		this.associatedTerminals = new ArrayList<>();
	}
	
	public void addTerminal(Terminal t) {
		this.associatedTerminals.add(t);
	}
	
	public void removeTerminal(Terminal t) {
		this.associatedTerminals.remove(t);
	}
}
