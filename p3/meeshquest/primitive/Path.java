package cmsc420.meeshquest.primitive;

import static cmsc420.meeshquest.primitive.Naming.*;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class Path {
	public LinkedList<City> pathList;
	protected double distance;

	public Path(final double distance) {
		pathList = new LinkedList<City>();
		setDistance(distance);
	}

	public void addEdge(final City city) {
		pathList.addFirst(city);
	}

	public int getHops() {
		return pathList.size() - 1;
	}

	public double getDistance() {
		return distance;
	}

	public LinkedList<City> getCityList() {
		return pathList;
	}

	public void setDistance(double distance) {
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		this.distance = Double.valueOf(df.format(Double.valueOf(distance)));
	}
	
	public void printPath(Element parent, Document doc) {
		if (pathList.size() == 0) {
			return;
		}
		City s, e, t;
		s = pathList.getFirst();
		for (int i = 1; i < pathList.size(); i ++) {
			e = pathList.get(i);
			Element roadElt = doc.createElement(ROAD_TAG);
			roadElt.setAttribute(MAP_ROAD_START, s.getName());
			roadElt.setAttribute(MAP_ROAD_END, e.getName());
			parent.appendChild(roadElt);
			if (i != pathList.size()-1) {
				Element dirElt;
				t = pathList.get(i+1);
				Arc2D.Float arc = new Arc2D.Float();
				arc.setArcByTangent(s, e, t, 1);
				double a = arc.getAngleExtent();
				if (a > 45 && a < 180) {
					dirElt = doc.createElement(RIGHT_TAG);
				} else if (a < -45 && a > -180) {
					dirElt = doc.createElement(LEFT_TAG);
				} else {
					dirElt = doc.createElement(STRAIGHT_TAG);
				}
				parent.appendChild(dirElt);
			}
			s = e;
		}
	}
	
	public void drawPath(CanvasPlus canvas) {
		City s = pathList.getFirst();
		City e = pathList.getLast();
		canvas.addPoint(s.getName(), s.x, s.y, Color.GREEN);
		canvas.addPoint(e.getName(), e.x, e.y, Color.RED);
		for (int i = 0; i < pathList.size()-1; i ++) {
			City c = pathList.get(i);
			if (i != 0) {
				canvas.addPoint(c.getName(), c.x, c.y, Color.BLUE);
			}
			City next = pathList.get(i+1);
			canvas.addLine(c.x, c.y, next.x, next.y, Color.BLUE);
		}
	}
}