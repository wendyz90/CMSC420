package cmsc420.meeshquest.pmquadtree;

import static cmsc420.meeshquest.primitive.Naming.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.meeshquest.primitive.Airport;
import cmsc420.meeshquest.primitive.City;
import cmsc420.meeshquest.primitive.GeomNameComparator;
import cmsc420.meeshquest.primitive.GeomPoint;
import cmsc420.meeshquest.primitive.Road;
import cmsc420.meeshquest.primitive.RoadNameComparator;
import cmsc420.meeshquest.primitive.Terminal;

public class PMQuadTree {

	private static final int WHITE = 1;
	private static final int BLACK = 2;
	private static final int GRAY = 3;

	private White white = new White();

	private Validator validator;
	private Node root;

	private int spatialWidth;
	private int spatialHeight;
	private Point2D.Float origin;
	private Rectangle2D.Float region;

	private Set<GeomPoint> airportSet;
	private Set<Road> roadSet;
	
	private boolean violated;

	private class PriorityQueueElement {
		Node n;
		City c;
		double distance;

		public PriorityQueueElement(Node n, double d, City c) {
			this.n = n;
			this.distance = d;
			this.c = c;
		}
	}

	private class PriorityQueueComparator implements Comparator<PriorityQueueElement> {

		@Override
		public int compare(PriorityQueueElement o1, PriorityQueueElement o2) {
			if (o1.distance < o2.distance) {
				return -1;
			} else if (o1.distance > o2.distance) {
				return 1;
			} else {
				if (o1.c != null && o2.c != null) {
					return o2.c.getName().compareTo(o1.c.getName());
				}
				if (o1.c == null) {
					return -1;
				} else if (o2.c == null) {
					return 1;
				}
				return 0;
			}
		}
	}

	abstract class Node {

		private final int type;

		public Node(final int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public Node addRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			throw new UnsupportedOperationException();
		}

		public Node addAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			throw new UnsupportedOperationException();
		}

		public void printXML(Element parent, Document doc) {
			throw new UnsupportedOperationException();
		}
		
		public Node removeRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			throw new UnsupportedOperationException();
		}
		
		public Node removeAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			throw new UnsupportedOperationException();
		}
		
	}

	class White extends Node {

		public White() {
			super(WHITE);
		}

		@Override
		public Node addRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			final Black blackNode = new Black();
			return blackNode.addRoad(r, origin, width, height);
		}

		@Override
		public Node addAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			final Black blackNode = new Black();
			return blackNode.addAirport(a, origin, width, height);
		}

		@Override
		public void printXML(Element parent, Document doc) {
			Element whiteElt = doc.createElement(WHITE_TAG);
			parent.appendChild(whiteElt);
		}
		
		@Override
		public Node removeRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			return this;
		}
		
		@Override
		public Node removeAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			return this;
		}
	}

	class Black extends Node {

		private Set<Road> roadList;
		// All GeomPoints in roadList
		private List<GeomPoint> geomList;
		
		// Only isolated airports
		private List<GeomPoint> airportList;

		public Black() {
			super(BLACK);
			roadList = new TreeSet<Road>(new RoadNameComparator());
			geomList = new ArrayList<>();
			airportList = new ArrayList<>();
		}

		public Set<Road> getRoadList() {
			return roadList;
		}

		public List<GeomPoint> getAirportList() {
			return airportList;
		}
		
		public List<GeomPoint> getGeomList() {
			return geomList;
		}
		
		public GeomPoint getGeomPoint() {
			if (geomList.size() == 0) {
				return null;
			}
			return geomList.get(0);
		}

		public int getCardinality() {
			return roadList.size() + geomList.size() + airportList.size();
		}

		@Override
		public Node addRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			final Rectangle2D.Float rect = new Rectangle2D.Float(origin.x, origin.y, width, height);
			if (Inclusive2DIntersectionVerifier.intersects(r.getStart(), rect)) {
				if (!geomList.contains(r.getStart())) {
					geomList.add(r.getStart());
				}
			}
			if (Inclusive2DIntersectionVerifier.intersects(r.getEnd(), rect)) {
				if (!geomList.contains(r.getEnd())) {
					geomList.add(r.getEnd());
				}
			}
			roadList.add(r);
			if (validator.ifValid(this)) {
				return this;
			} else if (width == 1 && height == 1) {
				violated = true;
				return this;
			} else {
				return partition(origin, width, height);
			}
		}

		@Override
		public Node addAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			airportList.add(a);
			if (validator.ifValid(this)) {
				return this;
			} else if (width == 1 && height == 1) {
				violated = true;
				return this;
			} else {
				return partition(origin, width, height);
			}
		}
		
		@Override
		public Node removeRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			roadList.remove(r);
			if (geomList.contains(r.getStart()) && !ifGeomAlreadyMapped(r.getStart())) {
				geomList.remove(r.getStart());
			}
			if (geomList.contains(r.getEnd()) && !ifGeomAlreadyMapped(r.getEnd())) {
				geomList.remove(r.getEnd());
			}
			if (getCardinality() == 0) {
				return white;
			}
			return this;
		}
		
		@Override
		public Node removeAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			airportList.remove(a);
			if (getCardinality() == 0) {
				return white;
			}
			return this;
		}

		@Override
		public void printXML(Element parent, Document doc) {
			Element blackElt = doc.createElement(BLACK_TAG);
			blackElt.setAttribute(CARDINALITY_TAG, String.valueOf(getCardinality()));
			parent.appendChild(blackElt);
			if (geomList.size() != 0) {
				GeomPoint gp = geomList.get(0);
				if (gp instanceof City) {
					City c = (City)gp;
					Element cityElt = doc.createElement(CITY_TAG);
					cityElt.setAttribute(CREATE_CITY_NAME, c.getName());
					cityElt.setAttribute(LOCAL_X, String.valueOf(Math.round(c.getX())));
					cityElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(c.getY())));
					cityElt.setAttribute(REMOTE_X, String.valueOf(Math.round(c.getRemotePoint().getX())));
					cityElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(c.getRemotePoint().getY())));
					cityElt.setAttribute(CREATE_CITY_COLOR, c.getColor());
					cityElt.setAttribute(CREATE_CITY_RADIUS, String.valueOf(c.getRadius()));
					blackElt.appendChild(cityElt);
				} else if (gp instanceof Terminal) {
					Terminal t = (Terminal)gp;
					Element terminalElt = doc.createElement(TERMINAL_TAG);
					terminalElt.setAttribute(AIRPORT_NAME, t.airport.getName());
					terminalElt.setAttribute(LOCAL_X, String.valueOf(Math.round(t.getX())));
					terminalElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(t.getY())));
					terminalElt.setAttribute(REMOTE_X, String.valueOf(Math.round(t.getRemotePoint().getX())));
					terminalElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(t.getRemotePoint().getY())));
					terminalElt.setAttribute(CITY_NAME, t.connectedCity.getName());
					terminalElt.setAttribute(COMMAND_NAME_TAG, t.getName());
					blackElt.appendChild(terminalElt);
				}
			}
			if (airportList.size() != 0) {
				Airport a = (Airport)airportList.get(0);
				Element airportElt = doc.createElement(AIRPORT_TAG);
				airportElt.setAttribute(COMMAND_NAME_TAG, a.getName());
				airportElt.setAttribute(LOCAL_X, String.valueOf(Math.round(a.getX())));
				airportElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(a.getY())));
				airportElt.setAttribute(REMOTE_X, String.valueOf(Math.round(a.getRemotePoint().getX())));
				airportElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(a.getRemotePoint().getY())));
				blackElt.appendChild(airportElt);
			}
			for (Road r : roadList) {
				Element roadElt = doc.createElement(ROAD_TAG);
				roadElt.setAttribute(MAP_ROAD_START, r.getStart().getName());
				roadElt.setAttribute(MAP_ROAD_END, r.getEnd().getName());
				blackElt.appendChild(roadElt);
			}
		}

		private Node partition(final Point2D.Float origin, final int width, final int height) {
			Node gray = new Gray(origin, width, height);
			for (Road r : roadList) {
				gray.addRoad(r, origin, width, height);
			}
			for (GeomPoint c : airportList) {
				gray.addAirport(c, origin, width, height);
			}
			return gray;
		}
	}

	class Gray extends Node {

		private Node[] children;
		private Rectangle2D.Float[] childRegions;
		private Point2D.Float origin;
		private Point2D.Float[] childOrigin;
		private int halfWidth;
		private int halfHeight;

		public Gray(final Point2D.Float origin, final int width, final int height) {
			super(GRAY);
			this.origin = origin;

			children = new Node[4];
			children[0] = white;
			children[1] = white;
			children[2] = white;
			children[3] = white;

			halfWidth = width >> 1;
			halfHeight = height >> 1;

			childOrigin = new Point2D.Float[4];
			childOrigin[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
			childOrigin[1] = new Point2D.Float(origin.x + halfWidth, origin.y + halfHeight);
			childOrigin[2] = new Point2D.Float(origin.x, origin.y);
			childOrigin[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

			childRegions = new Rectangle2D.Float[4];
			for (int i = 0; i < 4; i++) {
				childRegions[i] = new Rectangle2D.Float(childOrigin[i].x, childOrigin[i].y,
						halfWidth, halfHeight);
			}
		}

		@Override
		public Node addRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(r, childRegions[i])) {
					children[i] = children[i].addRoad(r, childOrigin[i], halfWidth, halfHeight);
				}
			}
			return this;
		}

		@Override
		public Node addAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(a, childRegions[i])) {
					children[i] = children[i].addAirport(a, childOrigin[i], halfWidth,
							halfHeight);
				}
			}
			return this;
		}
		
		@Override
		public Node removeRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			int whiteCount = 0, grayCount = 0, blackCount = 0;
			for (int i = 0; i < 4; i ++) {
				if (Inclusive2DIntersectionVerifier.intersects(r, childRegions[i])) {
					children[i] = children[i].removeRoad(r, childOrigin[i], halfWidth, halfHeight);
				}
				if (children[i].type == WHITE) {
					whiteCount ++;
				} else if (children[i].type == BLACK) {
					blackCount ++;
				} else {
					grayCount ++;
				}
			}
			if (whiteCount == 4) {
				return white;
			}
			if (whiteCount == 3 && blackCount == 1) {
				for (int i = 0; i < 4; i ++) {
					if (children[i].type == BLACK) {
						return children[i];
					}
				}
			}
			if (grayCount != 4) {
				Black b = new Black();
				b = addAllGeometry(b, this);
				if (validator.ifValid(b)) {
					return b;
				}
			}
			return this;
		}
		
		@Override
		public Node removeAirport(final GeomPoint a, final Point2D.Float origin, final int width,
				final int height) {
			int whiteCount = 0, grayCount = 0, blackCount = 0;
			for (int i = 0; i < 4; i ++) {
				if (Inclusive2DIntersectionVerifier.intersects(a, childRegions[i])) {
					children[i] = children[i].removeAirport(a, childOrigin[i], halfWidth, halfHeight);
				}
				if (children[i].type == WHITE) {
					whiteCount ++;
				} else if (children[i].type == BLACK) {
					blackCount ++;
				} else {
					grayCount ++;
				}
			}
			if (whiteCount == 4) {
				return white;
			}
			if (whiteCount == 3 && blackCount == 1) {
				for (int i = 0; i < 4; i ++) {
					if (children[i].type == BLACK) {
						return children[i];
					}
				}
			}
			if (grayCount != 4) {
				Black b = new Black();
				b = addAllGeometry(b, this);
				if (validator.ifValid(b)) {
					return b;
				}
			}
			return this;
		}
		
		private Black addAllGeometry(Black b, Node n) {
			if (n.type == BLACK) {
				Black bNode = (Black)n;
				for (Road r : bNode.roadList) {
					if (!b.roadList.contains(r)) {
						b.roadList.add(r);
					}
				}
				for (GeomPoint gp : bNode.geomList) {
					if (!b.geomList.contains(gp)) {
						b.geomList.add(gp);
					}
				}
				for (GeomPoint gp : bNode.airportList) {
					if (!b.airportList.contains(gp)) {
						b.airportList.add(gp);
					}
				}
			} else if (n.type == GRAY) {
				Gray gNode = (Gray)n;
				for (Node node : gNode.children) {
					b = addAllGeometry(b, node);
				}
			}
			return b;
		}

		@Override
		public void printXML(Element parent, Document doc) {
			Element grayElt = doc.createElement(GRAY_TAG);
			grayElt.setAttribute(X_TAG, String.valueOf(Math.round(origin.getX()+halfWidth)));
			grayElt.setAttribute(Y_TAG, String.valueOf(Math.round(origin.getY()+halfHeight)));
			parent.appendChild(grayElt);
			for (Node n : children) {
				n.printXML(grayElt, doc);
			}
		}

		public Rectangle2D.Float[] getChildRegions() {
			return childRegions;
		}
	}

	public PMQuadTree(final Validator validator, final int spatialWidth, final int spatialHeight,
			final int order) {
		root = white;
		this.validator = validator;
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
		this.origin = new Point2D.Float(0.0f, 0.0f);
		this.region = new Rectangle2D.Float(origin.x, origin.y, spatialWidth, spatialHeight);
		airportSet = new HashSet<>();
		roadSet = new HashSet<>();
		violated = false;
	}

	public boolean addAirport(final GeomPoint a) {
		violated = false;
		root = root.addAirport(a, origin, spatialWidth, spatialHeight);
		if (violated) {
			root = root.removeAirport(a, origin, spatialWidth, spatialHeight);
		} else {
			airportSet.add(a);
		}
		return !violated;
	}

	public boolean addRoad(final Road r) {
		violated = false;
		root = root.addRoad(r, origin, spatialWidth, spatialHeight);
		if (violated) {
			root = root.removeRoad(r, origin, spatialWidth, spatialHeight);
		} else {
			roadSet.add(r);
		}
		return !violated;
	}
	
	public void removeRoad(final Road r) {
		roadSet.remove(r);
		root = root.removeRoad(r, origin, spatialWidth, spatialHeight);
	}
	
	public void removeAirport(final Airport a) {
		airportSet.remove(a);
		root = root.removeAirport(a, origin, spatialWidth, spatialHeight);
	}
	
	public Set<Road> removeCity(final City c) {
		Set<Road> ret = new TreeSet<>(new RoadNameComparator());
		Set<Road> copyRoadSet = new HashSet<>(roadSet);
		for (Road r : copyRoadSet) {
			if (r.getStart().equals(c) || r.getEnd().equals(c)) {
				removeRoad(r);
				ret.add(r);
			}
		}
		return ret;
	}
	
	public boolean ifPointonExistingRoad(GeomPoint g) {
		for (Road r : roadSet) {
			if (r.ptLineDist(g) == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean ifGeomPointOutOfBounds(GeomPoint g) {
		return !Inclusive2DIntersectionVerifier.intersects(g, region);
	}

	public boolean ifRoadAlreadyMapped(Road r) {
		return roadSet.contains(r);
	}
	
	public boolean ifGeomAlreadyMapped(GeomPoint gp) {
		for (Road r : roadSet) {
			if (gp.x == r.getStart().x && gp.y == r.getStart().y) {
				return true;
			}
			if (gp.x == r.getEnd().x && gp.y == r.getEnd().y) {
				return true;
			}
		}
		return false;
	}

	public boolean ifRoadOutOfBounds(Road r) {
		return !Inclusive2DIntersectionVerifier.intersects(r, region);
	}
	
	public boolean ifRoadIntersects(Road road) {
		for (Road r : roadSet) {
			if (Inclusive2DIntersectionVerifier.intersects(r, road)) {
				if (r.getStart().getName().equals(road.getStart().getName()) ||
						r.getEnd().getName().equals(road.getStart().getName()) || 
						r.getStart().getName().equals(road.getEnd().getName()) || 
						r.getEnd().getName().equals(road.getEnd().getName())) {
					continue;
				} else {
					return true;
				}
			}
		}
		return false;
	}
	
	public Set<City> getCitySet() {
		Set<City> ret = new HashSet<>();
		for (Road r : roadSet) {
			GeomPoint start = r.getStart();
			GeomPoint end = r.getEnd();
			if (start instanceof City) {
				ret.add((City)start);
			}
			if (end instanceof City) {
				ret.add((City)end);
			}
		}
		return ret;
	}

	public boolean ifEmpty() {
		return root == white;
	}

	public void printPMQuadTree(Element parent, Document doc) {
		root.printXML(parent, doc);
	}

	public City nearestCity(Integer x, Integer y, boolean ifIsolated) {
		Point2D.Float pt = new Point2D.Float(x, y);
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(root, 0.0, null));
		double distance;
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (pqe.n.getType() == WHITE) {
				continue;
			} else if (pqe.n.getType() == BLACK) {
				return pqe.c;
			} else if (pqe.n.getType() == GRAY) {
				Gray node = (Gray) pqe.n;
				for (int i = 0; i < 4; i++) {
					if (node.children[i].getType() == BLACK) {
						Black blackNode = (Black) node.children[i];
						GeomPoint gp = blackNode.getGeomPoint();
						if (gp != null && gp instanceof City) {
							City c = (City)gp;
							distance = pt.distance(c);
							pq.add(new PriorityQueueElement(blackNode, distance, c));
						}
					} else {
						distance = Shape2DDistanceCalculator.distance(pt, node.childRegions[i]);
						pq.add(new PriorityQueueElement(node.children[i], distance, null));
					}
				}
			}
		}
		return null;

	}
	
	public void drawMap(CanvasPlus canvas) {
		drawMapHelper(root, canvas);
		for (Road r : roadSet) {
			canvas.addLine(r.x1, r.y1, r.x2, r.y2, Color.BLACK);
			canvas.addPoint(r.getStart().getName(), r.getStart().x, r.getStart().y, Color.BLACK);
			canvas.addPoint(r.getEnd().getName(), r.getEnd().x, r.getEnd().y, Color.BLACK);
		}
		for (GeomPoint c : airportSet) {
			canvas.addPoint(c.getName(), c.x, c.y, Color.BLACK);
		}
	}
	
	public void drawRange(CanvasPlus canvas, Integer x, Integer y, Integer radius) {
		canvas.addCircle(x, y, radius, Color.BLUE, false);
	}

	private void drawMapHelper(Node n, CanvasPlus canvas) {
		if (n.getType() == WHITE || n.getType() == BLACK) {
			return;
		}
		if (n.getType() == GRAY) {
			Gray grayNode = (Gray) n;
			Rectangle2D.Float[] children = grayNode.childRegions;
			canvas.addLine(children[0].getMinX(), children[0].getMinY(), children[1].getMaxX(), children[1].getMinY(), Color.GRAY);
			canvas.addLine(children[0].getMaxX(), children[0].getMaxY(), children[2].getMaxX(), children[2].getMinY(), Color.GRAY);
			for (Node node : grayNode.children) {
				drawMapHelper(node, canvas);
			}
		}
	}
}
