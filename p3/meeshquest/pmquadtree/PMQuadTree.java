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
import cmsc420.meeshquest.primitive.City;
import cmsc420.meeshquest.primitive.CityNameComparator;
import cmsc420.meeshquest.primitive.Road;
import cmsc420.meeshquest.primitive.RoadNameComparator;

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

	private Set<City> isolatedCitySet;
	private Set<Road> roadSet;

	private class PriorityQueueElement {  //PriorityQueueData
		Node n;
		City c;
		Road r;
		double distance;

		public PriorityQueueElement(Node n, double d, City c, Road r) {
			this.n = n;
			this.distance = d;
			this.c = c;
			this.r = r;
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
				} else if (o1.r != null && o2.r != null) {
					RoadNameComparator rnc = new RoadNameComparator();
					return rnc.compare(o1.r, o2.r);
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

		public Node addIsolatedCity(final City c, final Point2D.Float origin, final int width,
				final int height) {
			throw new UnsupportedOperationException();
		}

		public void printXML(Element parent, Document doc) {
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
		public Node addIsolatedCity(final City c, final Point2D.Float origin, final int width,
				final int height) {
			final Black blackNode = new Black();
			return blackNode.addIsolatedCity(c, origin, width, height);
		}

		@Override
		public void printXML(Element parent, Document doc) {
			Element whiteElt = doc.createElement(WHITE_TAG);
			parent.appendChild(whiteElt);
		}
	}

	class Black extends Node {

		private Set<Road> roadList;
		private List<City> cityList;
		private List<City> isolatedCityList;

		public Black() {
			super(BLACK);
			roadList = new TreeSet<Road>(new RoadNameComparator());
			cityList = new ArrayList<City>();
			isolatedCityList = new ArrayList<City>();
		}

		public Set<Road> getRoadList() {
			return roadList;
		}

		public List<City> getCityList() {
			return cityList;
		}

		public List<City> getIsolatedCityList() {
			return isolatedCityList;
		}

		public City getCity() {
			if (cityList.size() == 0 && isolatedCityList.size() != 0) {
				return isolatedCityList.get(0);
			}
			if (cityList.size() != 0 && isolatedCityList.size() == 0) {
				return cityList.get(0);
			}
			return null;
		}

		public City getCertainCity(boolean isolated) {
			if (isolated) {
				return isolatedCityList.size() == 0 ? null : isolatedCityList.get(0);
			} else {
				return cityList.size() == 0 ? null : cityList.get(0);
			}
		}

		public int getCardinality() {
			return roadList.size() + cityList.size() + isolatedCityList.size();
		}

		@Override
		public Node addRoad(final Road r, final Point2D.Float origin, final int width,
				final int height) {
			final Rectangle2D.Float rect = new Rectangle2D.Float(origin.x, origin.y, width, height);
			if (Inclusive2DIntersectionVerifier.intersects(r.getStart(), rect)) {
				if (!cityList.contains(r.getStart())) {
					cityList.add(r.getStart());
				}
			}
			if (Inclusive2DIntersectionVerifier.intersects(r.getEnd(), rect)) {
				if (!cityList.contains(r.getEnd())) {
					cityList.add(r.getEnd());
				}
			}
			roadList.add(r);
			if (validator.ifValid(this)) {
				return this;
			} else {
				return partition(origin, width, height);
			}
		}

		@Override
		public Node addIsolatedCity(final City c, final Point2D.Float origin, final int width,
				final int height) {
			isolatedCityList.add(c);
			if (validator.ifValid(this)) {
				return this;
			} else {
				return partition(origin, width, height);
			}
		}

		@Override
		public void printXML(Element parent, Document doc) {
			Element blackElt = doc.createElement(BLACK_TAG);
			blackElt.setAttribute(CARDINALITY_TAG, String.valueOf(getCardinality()));
			parent.appendChild(blackElt);
			for (City c : cityList) {
				Element cityElt = doc.createElement(CITY_TAG);
				cityElt.setAttribute(CREATE_CITY_NAME, c.getName());
				cityElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(c.getX())));
				cityElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(c.getY())));
				cityElt.setAttribute(CREATE_CITY_COLOR, c.getColor());
				cityElt.setAttribute(CREATE_CITY_RADIUS, String.valueOf(c.getRadius()));
				blackElt.appendChild(cityElt);
			}
			for (City c : isolatedCityList) {
				Element cityElt = doc.createElement(ISOLATED_CITY_TAG);
				cityElt.setAttribute(CREATE_CITY_NAME, c.getName());
				cityElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(c.getX())));
				cityElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(c.getY())));
				cityElt.setAttribute(CREATE_CITY_COLOR, c.getColor());
				cityElt.setAttribute(CREATE_CITY_RADIUS, String.valueOf(c.getRadius()));
				blackElt.appendChild(cityElt);
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
			for (City c : isolatedCityList) {
				gray.addIsolatedCity(c, origin, width, height);
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
		public Node addIsolatedCity(final City c, final Point2D.Float origin, final int width,
				final int height) {
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(c, childRegions[i])) {
					children[i] = children[i].addIsolatedCity(c, childOrigin[i], halfWidth,
							halfHeight);
				}
			}
			return this;
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
		if (order != 3) {
			throw new IllegalArgumentException("Order should be 3");
		}
		root = white;
		this.validator = validator;
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
		this.origin = new Point2D.Float(0.0f, 0.0f);
		this.region = new Rectangle2D.Float(origin.x, origin.y, spatialWidth, spatialHeight);
		isolatedCitySet = new HashSet<>();
		roadSet = new HashSet<>();
	}

	public void addIsolatedCity(final City c) {
		isolatedCitySet.add(c);
		root = root.addIsolatedCity(c, origin, spatialWidth, spatialHeight);
	}

	public void addRoad(final Road r) {
		roadSet.add(r);
		root = root.addRoad(r, origin, spatialWidth, spatialHeight);
	}

	public boolean ifCityAlreadyMapped(City c) {
		if (isolatedCitySet.contains(c)) {
			return true;
		} else {
			for (Road r : roadSet) {
				if (c.equals(r.getStart()) || c.equals(r.getEnd())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean ifCityOutOfBounds(City c) {
		return !Inclusive2DIntersectionVerifier.intersects(c, region);
	}

	public boolean ifStartOrEndIsIsolated(Road r) {
		return isolatedCitySet.contains(r.getStart()) || isolatedCitySet.contains(r.getEnd());
	}

	public boolean ifRoadAlreadyMapped(Road r) {
		return roadSet.contains(r);
	}

	public boolean ifRoadOutOfBounds(Road r) {
		return !Inclusive2DIntersectionVerifier.intersects(r, region);
	}

	public boolean ifEmpty() {
		return root == white;
	}
	
	public boolean ifCityExistent(City c) {
		for (City ci : isolatedCitySet) {
			if (ci.equals(c)) {
				return true;
			}
		}
		for (Road r : roadSet) {
			if (r.getStart().equals(c) || r.getEnd().equals(c)) {
				return true;
			}
		}
		return false;
	}

	public void printPMQuadTree(Element parent, Document doc) {
		root.printXML(parent, doc);
	}

	public Set<City> rangeCities(Integer x, Integer y, Integer radius) {
		Set<City> ret = new TreeSet<>(new CityNameComparator());
		Circle2D.Float circle = new Circle2D.Float(new Point2D.Float(x, y), radius);
		rangeCitiesHelper(root, circle, ret);
		return ret;
	}

	public Set<Road> rangeRoads(Integer x, Integer y, Integer radius) {
		Set<Road> ret = new TreeSet<>(new RoadNameComparator());
		Circle2D.Float circle = new Circle2D.Float(new Point2D.Float(x, y), radius);
		rangeRoadsHelper(root, circle, ret);
		return ret;
	}

	//
	public City nearestCity(Integer x, Integer y, boolean ifIsolated) {
		Point2D.Float pt = new Point2D.Float(x, y);
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(root, 0.0, null, null));
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
						City c = blackNode.getCertainCity(ifIsolated);
						if (c != null) {
							distance = pt.distance(c);
							pq.add(new PriorityQueueElement(blackNode, distance, c, null));
						}
					} else {
						distance = Shape2DDistanceCalculator.distance(pt, node.childRegions[i]);
						pq.add(new PriorityQueueElement(node.children[i], distance, null, null));
					}
				}
			}
		}
		return null;

	}

	public Road nearestRoad(Integer x, Integer y) {
		Point2D.Float pt = new Point2D.Float(x, y);
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(root, 0.0, null, null));
		double distance;
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (pqe.n.getType() == WHITE) {
				continue;
			} else if (pqe.n.getType() == BLACK) {
				return pqe.r;
			} else if (pqe.n.getType() == GRAY) {
				Gray node = (Gray) pqe.n;
				for (int i = 0; i < 4; i++) {
					if (node.children[i].getType() == BLACK) {
						Black blackNode = (Black) node.children[i];
						double minDistance = Double.MAX_VALUE;
						Road ret = null;
						for (Road r : blackNode.roadList) {
							double dist = r.ptSegDist(pt);
							if (dist < minDistance) {
								minDistance = dist;
								ret = r;
							}
						}
						pq.add(new PriorityQueueElement(blackNode, minDistance, null, ret));
					} else {
						distance = Shape2DDistanceCalculator.distance(pt, node.childRegions[i]);
						pq.add(new PriorityQueueElement(node.children[i], distance, null, null));
					}
				}
			}
		}
		return null;
	}

	public City nearestCityToRoad(Road r) {
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(root, 0.0, null, null));
		double distance;
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (pqe.n.getType() == WHITE) {
				continue;
			} else if (pqe.n.getType() == BLACK) {
				Black node = (Black) pqe.n;
				City ret = node.getCity();
				if (ret != null) {
					return ret;
				}
			} else if (pqe.n.getType() == GRAY) {
				Gray node = (Gray) pqe.n;
				for (int i = 0; i < 4; i++) {
					if (node.children[i].getType() == BLACK) {
						Black blackNode = (Black) node.children[i];
						City c = blackNode.getCity();
						if (c != null && !c.equals(r.getStart()) && !c.equals(r.getEnd())) {
							distance = r.ptSegDist(c);
							pq.add(new PriorityQueueElement(blackNode, distance, c, null));
						}
					} else {
						distance = Shape2DDistanceCalculator.distance(r, node.childRegions[i]);
						pq.add(new PriorityQueueElement(node.children[i], distance, null, null));
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
		for (City c : isolatedCitySet) {
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
	
	private void rangeCitiesHelper(Node n, Circle2D.Float circle, Set<City> ret) {
		if (n.getType() == WHITE) {
			return;
		}
		if (n.getType() == GRAY) {
			Gray node = (Gray) n;
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(node.childRegions[i], circle)) {
					rangeCitiesHelper(node.children[i], circle, ret);
				}
			}
		} else if (n.getType() == BLACK) {
			Black node = (Black) n;
			City c = node.getCity();
			if (c != null && Inclusive2DIntersectionVerifier.intersects(c, circle)) {
				ret.add(c);
			}
		}
	}

	private void rangeRoadsHelper(Node n, Circle2D.Float circle, Set<Road> ret) {
		if (n.getType() == WHITE) {
			return;
		}
		if (n.getType() == GRAY) {
			Gray node = (Gray) n;
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(node.childRegions[i], circle)) {
					rangeRoadsHelper(node.children[i], circle, ret);
				}
			}
		} else if (n.getType() == BLACK) {
			Black node = (Black) n;
			for (Road r : node.roadList) {
				if (r.ptSegDist(circle.getCenter()) <= circle.getRadius()) {
					ret.add(r);
				}
			}
		}
	}
}
