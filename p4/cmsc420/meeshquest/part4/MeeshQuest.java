package cmsc420.meeshquest.part4;

import static cmsc420.meeshquest.primitive.Naming.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.meeshquest.pmquadtree.PM3Validator;
import cmsc420.meeshquest.pmquadtree.PMQuadTree;
import cmsc420.meeshquest.primitive.Airport;
import cmsc420.meeshquest.primitive.City;
import cmsc420.meeshquest.primitive.GeomNameComparator;
import cmsc420.meeshquest.primitive.GeomPoint;
import cmsc420.meeshquest.primitive.Metropole;
import cmsc420.meeshquest.primitive.PointComparator;
import cmsc420.meeshquest.primitive.NameComparator;
import cmsc420.meeshquest.primitive.Path;
import cmsc420.meeshquest.primitive.Road;
import cmsc420.meeshquest.primitive.RoadAdjacencyList;
import cmsc420.meeshquest.primitive.Terminal;
import cmsc420.meeshquest.prquadtree.PrQuadTree;
import cmsc420.sortedmap.AvlGTree;
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	// Private data structure
	private static PrQuadTree<Metropole> worldTree;
	private static TreeMap<Point2D.Float, Metropole> worldMap;
	private static Set<Point2D.Float> metropoleMapped;
	private static AvlGTree<String, City> avlTree;
	private static Map<String, City> cityMap;
	private static Map<String, Airport> airportMap;
	private static Map<String, Terminal> terminalMap;
	private static Map<String, Metropole> geomNameMap;
	private static Document wholeDoc;
	private static RoadAdjacencyList roadAj;
	private static CanvasPlus canvas;
	private static Element result;
	private static Integer localSpatialWidth;
	private static Integer localSpatialHeight;
	private static Integer remoteSpatialHeight;
	private static Integer remoteSpatialWidth;
	private static Integer g;
	private static Integer pmOrder;

	public static void main(String[] args)
			throws ParserConfigurationException, TransformerException {
		metropoleMapped = new HashSet<>();
		worldMap = new TreeMap<Point2D.Float, Metropole>(new PointComparator());
		geomNameMap = new TreeMap<>(new NameComparator());
		cityMap = new HashMap<>();
		airportMap = new HashMap<>();
		terminalMap = new HashMap<>();
		wholeDoc = XmlUtility.getDocumentBuilder().newDocument();
		result = wholeDoc.createElement("results");
		wholeDoc.appendChild(result);
		roadAj = new RoadAdjacencyList();
		try {
			Document doc = XmlUtility.validateNoNamespace(System.in);
			Element docElement = doc.getDocumentElement();
			
			// Parse value
			localSpatialWidth = Integer.parseInt(docElement.getAttribute(LOCAL_WIDTH));
			localSpatialHeight = Integer.parseInt(docElement.getAttribute(LOCAL_HEIGHT));
			remoteSpatialWidth = Integer.parseInt(docElement.getAttribute(REMOTE_WIDTH));
			remoteSpatialHeight = Integer.parseInt(docElement.getAttribute(REMOTE_HEIGHT));
			g = Integer.parseInt(docElement.getAttribute(G));
			pmOrder = Integer.parseInt(docElement.getAttribute(PM_ORDER));
			avlTree = new AvlGTree<>(new NameComparator(), g);
			// Construct data structure
			worldTree = new PrQuadTree<Metropole>(remoteSpatialWidth, 0, remoteSpatialHeight, 0);
			canvas = new CanvasPlus("MeeshQuest", localSpatialWidth, localSpatialHeight);
			canvas.addRectangle(0, 0, localSpatialWidth, localSpatialHeight, Color.BLACK, false);
			NodeList nl = docElement.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				// System.out.println(n.getNodeName());
				if (n.getNodeName().equals(CREATE_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String name = ne.getAttribute(CREATE_CITY_NAME);
						String xs = ne.getAttribute(LOCAL_X);
						String ys = ne.getAttribute(LOCAL_Y);
						String xr = ne.getAttribute(REMOTE_X);
						String yr = ne.getAttribute(REMOTE_Y);
						String radiusString = ne.getAttribute(CREATE_CITY_RADIUS);
						String color = ne.getAttribute(CREATE_CITY_COLOR);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						Integer lx = Integer.parseInt(xs);
						Integer ly = Integer.parseInt(ys);
						Integer rx = Integer.parseInt(xr);
						Integer ry = Integer.parseInt(yr);
						Integer radius = Integer.parseInt(radiusString);
						createCity(id, name, lx, ly, rx, ry, radius, color);
					}
					
					
					
					
					
					
				} else if (n.getNodeName().equals(LIST_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String sortBy = ne.getAttribute(LIST_CITY_SORTBY);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						listCities(id, sortBy);
					}
					
					
					
					
				} else if (n.getNodeName().equals(CLEAR_ALL_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						clearAll(id);
					}
				} else if (n.getNodeName().equals(PRINT_AVL_TREE_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						printAvlTree(id);
					}
				} else if (n.getNodeName().equals(DELETE_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String cityName = ne.getAttribute(DELETE_CITY_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						deleteCity(id, cityName);
					}
				} else if (n.getNodeName().equals(MAP_ROAD_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String start = ne.getAttribute(MAP_ROAD_START);
						String end = ne.getAttribute(MAP_ROAD_END);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						mapRoad(id, start, end);
					}
				} else if (n.getNodeName().equals(UNMAP_ROAD_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String start = ne.getAttribute(MAP_ROAD_START);
						String end = ne.getAttribute(MAP_ROAD_END);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						unMapRoad(id, start, end);
					}
				} else if (n.getNodeName().equals(PRINT_PMQUADTREE_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer rx = Integer.parseInt(ne.getAttribute(REMOTE_X));
						Integer ry = Integer.parseInt(ne.getAttribute(REMOTE_Y));
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						printPMQuadtree(id, rx, ry);
					}
				} else if (n.getNodeName().equals(SAVE_MAP_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String fileName = ne.getAttribute(SAVE_MAP_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer rx = Integer.parseInt(ne.getAttribute(REMOTE_X));
						Integer ry = Integer.parseInt(ne.getAttribute(REMOTE_Y));
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						saveMap(id, fileName, rx, ry);
					}
				} else if (n.getNodeName().equals(GLOBAL_RANGE_CITIES)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(REMOTE_X));
						Integer y = Integer.parseInt(ne.getAttribute(REMOTE_Y));
						Integer radius = Integer.parseInt(ne.getAttribute(RANGE_CITY_RADIUS));
						//String name = ne.getAttribute(RANGE_CITY_SAVEMAP);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						globalRangeCities(id, x, y, radius);
					}
				} else if (n.getNodeName().equals(NEAREST_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer lx = Integer.parseInt(ne.getAttribute(LOCAL_X));
						Integer ly = Integer.parseInt(ne.getAttribute(LOCAL_Y));
						Integer rx = Integer.parseInt(ne.getAttribute(REMOTE_X));
						Integer ry = Integer.parseInt(ne.getAttribute(REMOTE_Y));
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						nearestCity(id, lx, ly, rx, ry);
					}
				} else if (n.getNodeName().equals(MAP_AIRPORT)){
					if (n instanceof Element) {
						Element ne = (Element) n;
						String name = ne.getAttribute(CREATE_CITY_NAME);
						Integer lx = Integer.parseInt(ne.getAttribute(LOCAL_X));
						Integer ly = Integer.parseInt(ne.getAttribute(LOCAL_Y));
						Integer rx = Integer.parseInt(ne.getAttribute(REMOTE_X));
						Integer ry = Integer.parseInt(ne.getAttribute(REMOTE_Y));
						String terminalName = ne.getAttribute(TERMINAL_NAME);
						Integer tx = Integer.parseInt(ne.getAttribute(TERMINAL_X));
						Integer ty = Integer.parseInt(ne.getAttribute(TERMINAL_Y));
						String terminalCity = ne.getAttribute(TERMINAL_CITY);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						mapAirport(id, name, lx, ly, rx, ry, terminalName, tx, ty, terminalCity);
					}
				} else if (n.getNodeName().equals(MAP_TERMINAL)){
					if (n instanceof Element) {
						Element ne = (Element) n;
						String name = ne.getAttribute(CREATE_CITY_NAME);
						Integer lx = Integer.parseInt(ne.getAttribute(LOCAL_X));
						Integer ly = Integer.parseInt(ne.getAttribute(LOCAL_Y));
						Integer rx = Integer.parseInt(ne.getAttribute(REMOTE_X));
						Integer ry = Integer.parseInt(ne.getAttribute(REMOTE_Y));
						String cityName = ne.getAttribute(CITY_NAME);
						String airportName = ne.getAttribute(AIRPORT_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						mapTerminal(id, name, lx, ly, rx, ry, cityName, airportName);
					}
				} else if (n.getNodeName().equals(UNMAP_AIRPORT)){
					if (n instanceof Element) {
						Element ne = (Element) n;
						String name = ne.getAttribute(CREATE_CITY_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						unmapAirport(id, name);
					}
				} else if (n.getNodeName().equals(UNMAP_TERMINAL)){
					if (n instanceof Element) {
						Element ne = (Element) n;
						String name = ne.getAttribute(CREATE_CITY_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						unmapTerminal(id, name);
					}
				}
				else if (n.getNodeName().equals(MST)){
					if (n instanceof Element) {
						Element ne = (Element) n;
						String start = ne.getAttribute(MAP_ROAD_START);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						mst(id, start);
					}
				}
				else {
					// throw new ParserConfigurationException();
				}
			}
			XmlUtility.print(wholeDoc);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			// System.out.println(e);
			wholeDoc = XmlUtility.getDocumentBuilder().newDocument();
			Element elt = wholeDoc.createElement(FATAL_ERROR_TAG);
			wholeDoc.appendChild(elt);
			XmlUtility.print(wholeDoc);
		}
	}

	private static void createCity(Integer id, String name, Integer lx, Integer ly, Integer rx, Integer ry, Integer radius,
			String color) {
		// Preparer output format
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, CREATE_CITY_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element nameElt = wholeDoc.createElement(CREATE_CITY_NAME);
		Element lxElt = wholeDoc.createElement(LOCAL_X);
		Element lyElt = wholeDoc.createElement(LOCAL_Y);
		Element rxElt = wholeDoc.createElement(REMOTE_X);
		Element ryElt = wholeDoc.createElement(REMOTE_Y);
		Element radiusElt = wholeDoc.createElement(CREATE_CITY_RADIUS);
		Element colorElt = wholeDoc.createElement(CREATE_CITY_COLOR);
		nameElt.setAttribute(VALUE_TAG, name);
		lxElt.setAttribute(VALUE_TAG, lx.toString());
		lyElt.setAttribute(VALUE_TAG, ly.toString());
		rxElt.setAttribute(VALUE_TAG, rx.toString());
		ryElt.setAttribute(VALUE_TAG, ry.toString());
		radiusElt.setAttribute(VALUE_TAG, radius.toString());
		colorElt.setAttribute(VALUE_TAG, color);
		parameterElt.appendChild(nameElt);
		parameterElt.appendChild(lxElt);
		parameterElt.appendChild(lyElt);
		parameterElt.appendChild(rxElt);
		parameterElt.appendChild(ryElt);
		parameterElt.appendChild(radiusElt);
		parameterElt.appendChild(colorElt);

		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);

		// Construct city object
		Point2D.Float p = new Point2D.Float(rx, ry);
		Metropole m;
		if (worldMap.containsKey(p)) {
			m = worldMap.get(p);
		} else {
			m = new Metropole(rx.floatValue(), ry.floatValue(), localSpatialWidth, localSpatialHeight, pmOrder);
			worldMap.put(p, m);
		}
		City c = new City(name, lx.floatValue(), ly.floatValue(), rx.floatValue(), ry.floatValue(), radius, color);
		int success = 0;
		if (m.geomPointSet.contains(c)) {
			success = -1;
		} else if (geomNameMap.containsKey(name)) {
			success = -2;
		}
		if (success != 0) {
			Element errorElt = wholeDoc.createElement(ERROR_TAG);
			errorElt.setAttribute(ERROR_TYPE_TAG,
					success == -1 ? DUPLICATE_CITY_COORDINATES : DUPLICATE_CITY_NAME);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		m.cityMap.put(name, c);
		m.citySet.add(c);
		m.geomPointSet.add(c);
		avlTree.put(name, c);
		geomNameMap.put(name, m);
		cityMap.put(name, c);
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	
	
	
	
	
	
	
	
	
	private static void listCities(Integer id, String sortBy) throws ParserConfigurationException {
		// Prepare output format
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, LIST_CITY_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}

		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element sortByElt = wholeDoc.createElement(LIST_CITY_SORTBY);
		sortByElt.setAttribute(VALUE_TAG, sortBy);
		parameterElt.appendChild(sortByElt);

		if (worldMap.size() == 0) {
			Element errorElt = wholeDoc.createElement(ERROR_TAG);
			errorElt.setAttribute(ERROR_TYPE_TAG, NO_CITIES_TO_LIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}

		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityListElt = wholeDoc.createElement(CITY_LIST_TAG);
		outputElt.appendChild(cityListElt);

		if (sortBy.equals(LIST_CITY_SORTBY_NAME)) {
			for (Map.Entry<String, Metropole> e : geomNameMap.entrySet()) {
				if (!cityMap.containsKey(e.getKey())) {
					continue;
				}
				Element cityElt = wholeDoc.createElement(CITY_TAG);
				cityElt.setAttribute(CREATE_CITY_NAME, cityMap.get(e.getKey()).getName());
				cityElt.setAttribute(LOCAL_X, String.valueOf(Math.round(cityMap.get(e.getKey()).getX())));
				cityElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(cityMap.get(e.getKey()).getY())));
				cityElt.setAttribute(CREATE_CITY_RADIUS, cityMap.get(e.getKey()).getRadius().toString());
				cityElt.setAttribute(CREATE_CITY_COLOR, cityMap.get(e.getKey()).getColor());
				cityElt.setAttribute(REMOTE_X, String.valueOf(Math.round(e.getValue().getX())));
				cityElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(e.getValue().getY())));
				cityListElt.appendChild(cityElt);
			}
		} else if (sortBy.equals(LIST_CITY_SORTBY_COORDINATE)) {
			for (Map.Entry<Point2D.Float, Metropole> e : worldMap.entrySet()) {
				Iterator<City> it = e.getValue().citySet.iterator();
				double rx = e.getKey().getX();
				double ry = e.getKey().getY();
				while (it.hasNext()) {
					City c = it.next();
					Element cityElt = wholeDoc.createElement(CITY_TAG);
					cityElt.setAttribute(CREATE_CITY_NAME, c.getName());
					cityElt.setAttribute(LOCAL_X, String.valueOf(Math.round(c.getX())));
					cityElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(c.getY())));
					cityElt.setAttribute(REMOTE_X, String.valueOf(Math.round(rx)));
					cityElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(ry)));
					cityElt.setAttribute(CREATE_CITY_RADIUS, c.getRadius().toString());
					cityElt.setAttribute(CREATE_CITY_COLOR, c.getColor());
					cityListElt.appendChild(cityElt);
				}
			}
		} else {
			throw new ParserConfigurationException();
		}
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void printAvlTree(Integer id) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, PRINT_AVL_TREE_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		if (avlTree.size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, EMPTY_TREE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		avlTree.printXml(outputElt, wholeDoc);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void clearAll(Integer id) {
		worldTree = new PrQuadTree<Metropole>(remoteSpatialWidth, 0, remoteSpatialHeight, 0);
		worldMap.clear();
		avlTree.clear();
		cityMap.clear();
		geomNameMap.clear();
		airportMap.clear();
		terminalMap.clear();
		roadAj.clear();
		metropoleMapped.clear();
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, CLEAR_ALL_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void deleteCity(Integer id, String cityName) {
		 Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		 Element errorElt = wholeDoc.createElement(ERROR_TAG);
		 Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		 commandElt.setAttribute(COMMAND_NAME_TAG, DELETE_CITY_CMD);
		 if (id != null) {
				commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
			}
		 Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		 Element cityNameElt = wholeDoc.createElement(DELETE_CITY_NAME);
		 cityNameElt.setAttribute(VALUE_TAG, cityName);
		 parameterElt.appendChild(cityNameElt);
		 if (!cityMap.containsKey(cityName)) {
			 errorElt.setAttribute(ERROR_TYPE_TAG, CITY_DOES_NOT_EXIST);
			 errorElt.appendChild(commandElt);
			 errorElt.appendChild(parameterElt);
			 result.appendChild(errorElt);
			 return;
		 }
		 City c = cityMap.get(cityName);
		 Metropole m = worldMap.get(new Point2D.Float(c.getRemotePoint().x, c.getRemotePoint().y));
		 Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		 Set<Road> roadList = m.quadTree.removeCity(c);
		 if (roadList.size() != 0) {
			 Element unmappedElt = wholeDoc.createElement(CITY_UNMAPPED_TAG);
			 unmappedElt.setAttribute(CREATE_CITY_NAME, c.getName());
			 unmappedElt.setAttribute(LOCAL_X, String.valueOf(Math.round(c.getX())));
			 unmappedElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(c.getY())));
			 unmappedElt.setAttribute(REMOTE_X, String.valueOf(Math.round(c.getRemotePoint().getX())));
			 unmappedElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(c.getRemotePoint().getY())));
			 unmappedElt.setAttribute(CREATE_CITY_COLOR, c.getColor());
			 unmappedElt.setAttribute(CREATE_CITY_RADIUS, String.valueOf(c.getRadius()));
			 outputElt.appendChild(unmappedElt);
		 }
		 for (Road r : roadList) {
			 roadAj.removeRoad(r);
			 Element roadUnmappedElt = wholeDoc.createElement(ROAD_UNMAPPED_TAG);
			 roadUnmappedElt.setAttribute(MAP_ROAD_START, r.getStart().getName());
			 roadUnmappedElt.setAttribute(MAP_ROAD_END, r.getEnd().getName());
			 outputElt.appendChild(roadUnmappedElt);
		 }

		 cityMap.remove(cityName);
		 geomNameMap.remove(cityName);
		 m.cityMap.remove(cityName);
		 m.citySet.remove(c);
		 m.geomPointSet.remove(c);
		 avlTree.remove(cityName);
		 successElt.appendChild(commandElt);
		 successElt.appendChild(parameterElt);
		 successElt.appendChild(outputElt);
		 result.appendChild(successElt);
	}

	private static void mapRoad(Integer id, String s, String e) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, MAP_ROAD_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element startElt = wholeDoc.createElement(MAP_ROAD_START);
		startElt.setAttribute(VALUE_TAG, s);
		Element endElt = wholeDoc.createElement(MAP_ROAD_END);
		endElt.setAttribute(VALUE_TAG, e);
		parameterElt.appendChild(startElt);
		parameterElt.appendChild(endElt);
		if (!cityMap.containsKey(s)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, START_POINT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!cityMap.containsKey(e)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, END_POINT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (s.equals(e)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, START_EQUALS_END);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City startCity = cityMap.get(s);
		City endCity = cityMap.get(e);
		if (geomNameMap.get(e) != geomNameMap.get(s)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_NOT_IN_ONE_METROPOLE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Metropole m = geomNameMap.get(e);
		if (m.getX() < 0 || m.getY() < 0 || m.getX() >= remoteSpatialWidth || m.getY() >= remoteSpatialHeight) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		
		Road road = new Road(startCity, endCity);
		if (m.quadTree.ifRoadOutOfBounds(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m.quadTree.ifRoadAlreadyMapped(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_ALREADY_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m.quadTree.ifRoadIntersects(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_INTERSECTS_ANOTHER_ROAD);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!m.quadTree.addRoad(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_VIOLATES_PMRULES);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!metropoleMapped.contains(m)) {
			worldTree.insert(m, "Dummy");
			metropoleMapped.add(m);
		}
		
		roadAj.addRoad(road);
		
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		Element roadCreatedElt = wholeDoc.createElement(ROAD_CREATED_TAG);
		roadCreatedElt.setAttribute(MAP_ROAD_START, s);
		roadCreatedElt.setAttribute(MAP_ROAD_END, e);
		outputElt.appendChild(roadCreatedElt);
		result.appendChild(successElt);
	}

	private static void unMapRoad(Integer id, String start, String end) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, UNMAP_ROAD_CMD);
		commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element startElt = wholeDoc.createElement(MAP_ROAD_START);
		startElt.setAttribute(VALUE_TAG, start);
		Element endElt = wholeDoc.createElement(MAP_ROAD_END);
		endElt.setAttribute(VALUE_TAG, end);
		parameterElt.appendChild(startElt);
		parameterElt.appendChild(endElt);
		if (!cityMap.containsKey(start)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, START_POINT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!cityMap.containsKey(end)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, END_POINT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (start.equals(end)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, START_EQUALS_END);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City startCity = cityMap.get(start);
		City endCity = cityMap.get(end);
		if (geomNameMap.get(start) != geomNameMap.get(end)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Metropole m = geomNameMap.get(start);
		Road r = new Road(startCity, endCity);
		if (!m.quadTree.ifRoadAlreadyMapped(r)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		m.quadTree.removeRoad(r);
		roadAj.removeRoad(r);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		Element roadDeletedElt = wholeDoc.createElement(ROAD_DELETED_TAG);
		roadDeletedElt.setAttribute(MAP_ROAD_START, start);
		roadDeletedElt.setAttribute(MAP_ROAD_END, end);
		outputElt.appendChild(roadDeletedElt);
		result.appendChild(successElt);
	}

	private static void printPMQuadtree(Integer id, Integer rx, Integer ry) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, PRINT_PMQUADTREE_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(REMOTE_X);
		Element yElt = wholeDoc.createElement(REMOTE_Y);
		xElt.setAttribute(VALUE_TAG, String.valueOf(rx));
		yElt.setAttribute(VALUE_TAG, String.valueOf(ry));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		if (rx < 0 || ry < 0 || rx >= remoteSpatialWidth || ry >= remoteSpatialHeight) {
			errorElt.setAttribute(ERROR_TYPE_TAG, METROPOLE_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Metropole m = worldMap.get(new Point2D.Float(rx, ry));
		if (m == null || m.quadTree.ifEmpty()) {
			errorElt.setAttribute(ERROR_TYPE_TAG, METROPOLE_IS_EMPTY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element quadTreeElt = wholeDoc.createElement(QUAD_TREE);
		quadTreeElt.setAttribute(ORDER_TAG, String.valueOf(pmOrder));
		outputElt.appendChild(quadTreeElt);
		m.quadTree.printPMQuadTree(quadTreeElt, wholeDoc);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void saveMap(Integer id, String name, Integer rx, Integer ry) throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, SAVE_MAP_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(REMOTE_X);
		Element yElt = wholeDoc.createElement(REMOTE_Y);
		Element fileNameElt = wholeDoc.createElement(SAVE_MAP_NAME);
		xElt.setAttribute(VALUE_TAG, String.valueOf(rx));
		yElt.setAttribute(VALUE_TAG, String.valueOf(ry));
		fileNameElt.setAttribute(VALUE_TAG, name);
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		parameterElt.appendChild(fileNameElt);
		if (rx < 0 || ry < 0 || rx >= remoteSpatialWidth || ry >= remoteSpatialHeight) {
			errorElt.setAttribute(ERROR_TYPE_TAG, METROPOLE_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Metropole m = worldMap.get(new Point2D.Float(rx, ry));
		if (m == null || m.geomPointSet.size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, METROPOLE_IS_EMPTY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		m.quadTree.drawMap(canvas);
		//canvas.draw();
		//canvas.save(name);
		//canvas.dispose();
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void globalRangeCities(Integer id, Integer x, Integer y, Integer radius)
			throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, GLOBAL_RANGE_CITIES);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(REMOTE_X);
		xElt.setAttribute(VALUE_TAG, String.valueOf(x));
		Element yElt = wholeDoc.createElement(REMOTE_Y);
		yElt.setAttribute(VALUE_TAG, String.valueOf(y));
		Element radiusElt = wholeDoc.createElement(RANGE_CITY_RADIUS);
		radiusElt.setAttribute(VALUE_TAG, String.valueOf(radius));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		parameterElt.appendChild(radiusElt);
		List<Metropole> inRangeMetropole = worldTree.rangeSearch(x, y, radius);
		if (inRangeMetropole.size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NO_CITIES_EXIST_IN_RANGE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		TreeSet<City> inRange = new TreeSet<City>(new GeomNameComparator());
		for (Metropole m : inRangeMetropole) {
			for (City c : m.quadTree.getCitySet()) {
				inRange.add(c);
			}
		}
		if (inRange.size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NO_CITIES_EXIST_IN_RANGE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Iterator<City> it = inRange.iterator();
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityListElt = wholeDoc.createElement(CITY_LIST_TAG);
		while (it.hasNext()) {
			City city = it.next();
			Element cityElt = wholeDoc.createElement(CITY_TAG);
			cityElt.setAttribute(CREATE_CITY_NAME, city.getName());
			cityElt.setAttribute(LOCAL_X, String.valueOf(Math.round(city.getX())));
			cityElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(city.getY())));
			cityElt.setAttribute(REMOTE_X, String.valueOf(Math.round(city.getRemotePoint().getX())));
			cityElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(city.getRemotePoint().getY())));
			cityElt.setAttribute(CREATE_CITY_RADIUS, city.getRadius().toString());
			cityElt.setAttribute(CREATE_CITY_COLOR, city.getColor());
			cityListElt.appendChild(cityElt);
		}
		outputElt.appendChild(cityListElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void nearestCity(Integer id, Integer lx, Integer ly, Integer rx, Integer ry) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, NEAREST_CITY_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element lxElt = wholeDoc.createElement(LOCAL_X);
		lxElt.setAttribute(VALUE_TAG, String.valueOf(lx));
		Element lyElt = wholeDoc.createElement(LOCAL_Y);
		lyElt.setAttribute(VALUE_TAG, String.valueOf(ly));
		Element rxElt = wholeDoc.createElement(REMOTE_X);
		rxElt.setAttribute(VALUE_TAG, String.valueOf(rx));
		Element ryElt = wholeDoc.createElement(REMOTE_Y);
		ryElt.setAttribute(VALUE_TAG, String.valueOf(ry));
		parameterElt.appendChild(lxElt);
		parameterElt.appendChild(lyElt);
		parameterElt.appendChild(rxElt);
		parameterElt.appendChild(ryElt);
		Metropole m = worldMap.get(new Point2D.Float(rx, ry));
		if (m == null || m.citySet.size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_NOT_FOUND);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City city = m.quadTree.nearestCity(lx, ly, false);
		if (city == null) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_NOT_FOUND);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityElt = wholeDoc.createElement(CITY_TAG);
		cityElt.setAttribute(CREATE_CITY_NAME, city.getName());
		cityElt.setAttribute(LOCAL_X, String.valueOf(Math.round(city.getX())));
		cityElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(city.getY())));
		cityElt.setAttribute(REMOTE_X, String.valueOf(Math.round(m.getX())));
		cityElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(m.getY())));
		cityElt.setAttribute(CREATE_CITY_RADIUS, city.getRadius().toString());
		cityElt.setAttribute(CREATE_CITY_COLOR, city.getColor());
		outputElt.appendChild(cityElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}
	
	private static void mapAirport(Integer id, String name, Integer lx, Integer ly, Integer rx, 
			Integer ry, String terminalName, Integer tx, Integer ty, String terminalCity) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, MAP_AIRPORT);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element nameElt = wholeDoc.createElement(CREATE_CITY_NAME);
		nameElt.setAttribute(VALUE_TAG, name);
		Element lxElt = wholeDoc.createElement(LOCAL_X);
		lxElt.setAttribute(VALUE_TAG, String.valueOf(lx));
		Element lyElt = wholeDoc.createElement(LOCAL_Y);
		lyElt.setAttribute(VALUE_TAG, String.valueOf(ly));
		Element rxElt = wholeDoc.createElement(REMOTE_X);
		rxElt.setAttribute(VALUE_TAG, String.valueOf(rx));
		Element ryElt = wholeDoc.createElement(REMOTE_Y);
		ryElt.setAttribute(VALUE_TAG, String.valueOf(ry));
		Element terminalNameElt = wholeDoc.createElement(TERMINAL_NAME);
		terminalNameElt.setAttribute(VALUE_TAG, terminalName);
		Element txElt = wholeDoc.createElement(TERMINAL_X);
		txElt.setAttribute(VALUE_TAG, String.valueOf(tx));
		Element tyElt = wholeDoc.createElement(TERMINAL_Y);
		tyElt.setAttribute(VALUE_TAG, String.valueOf(ty));
		Element terminalCityElt = wholeDoc.createElement(TERMINAL_CITY);
		terminalCityElt.setAttribute(VALUE_TAG, terminalCity);
		parameterElt.appendChild(nameElt);
		parameterElt.appendChild(lxElt);
		parameterElt.appendChild(lyElt);
		parameterElt.appendChild(rxElt);
		parameterElt.appendChild(ryElt);
		parameterElt.appendChild(terminalNameElt);
		parameterElt.appendChild(txElt);
		parameterElt.appendChild(tyElt);
		parameterElt.appendChild(terminalCityElt);

		if (geomNameMap.containsKey(name)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, DUPLICATE_AIRPORT_NAME);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Metropole m = worldMap.get(new Point2D.Float(rx.floatValue(), ry.floatValue()));
		Airport a = new Airport(lx.floatValue(), ly.floatValue(), rx.floatValue(), ry.floatValue(), name);
		if (m != null) {
			if (m.geomPointSet.contains(a)) {
				errorElt.setAttribute(ERROR_TYPE_TAG, DUPLICATE_AIRPORT_COORDINATES);
				errorElt.appendChild(commandElt);
				errorElt.appendChild(parameterElt);
				result.appendChild(errorElt);
				return;
			}
		}
		if (rx < 0 || ry < 0 || rx >= remoteSpatialWidth || ry >= remoteSpatialHeight) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m != null && m.quadTree.ifGeomPointOutOfBounds(a)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (geomNameMap.containsKey(terminalName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, DUPLICATE_TERMINAL_NAME);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Terminal t = new Terminal(tx.floatValue(), ty.floatValue(), rx.floatValue(), ry.floatValue(), terminalName);
		t.setAirport(a);
		if (m != null) {
			if (m.geomPointSet.contains(t)) {
				errorElt.setAttribute(ERROR_TYPE_TAG, DUPLICATE_TERMINAL_COORDINATES);
				errorElt.appendChild(commandElt);
				errorElt.appendChild(parameterElt);
				result.appendChild(errorElt);
				return;
			}
		}
		if (m != null && m.quadTree.ifGeomPointOutOfBounds(t)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, TERMINAL_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!cityMap.containsKey(terminalCity)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CONNECTING_CITY_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City c = cityMap.get(terminalCity);
		if (geomNameMap.get(terminalCity) != m) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CONNECTING_CITY_NOT_IN_SAME_METROPOLE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m.quadTree.ifPointonExistingRoad(a)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_VIOLATES_PMRULES);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!m.quadTree.addAirport(a)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_VIOLATES_PMRULES);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m == null || !m.quadTree.ifGeomAlreadyMapped(c)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CONNECTING_CITY_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		t.setConnectedCity(c);
		Road r = new Road(c, t);
		if (m.quadTree.ifRoadIntersects(r)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_INTERSECTS_ANOTHER_ROAD);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!m.quadTree.addRoad(r)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, TERMINAL_VIOLATES_PMRULES);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		a.addTerminal(t);
		t.setConnectedRoad(r);
		airportMap.put(name, a);
		terminalMap.put(terminalName, t);
		geomNameMap.put(name, m);
		geomNameMap.put(terminalName, m);
		m.geomPointSet.add(a);
		m.geomPointSet.add(t);
		
		roadAj.addRoad(r);
		Road directRoad = new Road(t, a);
		roadAj.addRoad(directRoad);
		
		for (Map.Entry<String, Airport> e : airportMap.entrySet()) {
			if (e.getKey().equals(name)) {
				continue;
			}
			Metropole other = geomNameMap.get(e.getKey());
			if (other != m) {
				roadAj.addEdge(name, e.getKey(), other.distance(m));
			} else {
				roadAj.addEdge(name, e.getKey(), a.distance(e.getValue()));
			}
		}
		
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}
	
	private static void mapTerminal(Integer id, String name, Integer lx, Integer ly, 
			Integer rx, Integer ry, String cityName, String airportName) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, MAP_TERMINAL);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element nameElt = wholeDoc.createElement(CREATE_CITY_NAME);
		nameElt.setAttribute(VALUE_TAG, name);
		Element lxElt = wholeDoc.createElement(LOCAL_X);
		lxElt.setAttribute(VALUE_TAG, String.valueOf(lx));
		Element lyElt = wholeDoc.createElement(LOCAL_Y);
		lyElt.setAttribute(VALUE_TAG, String.valueOf(ly));
		Element rxElt = wholeDoc.createElement(REMOTE_X);
		rxElt.setAttribute(VALUE_TAG, String.valueOf(rx));
		Element ryElt = wholeDoc.createElement(REMOTE_Y);
		ryElt.setAttribute(VALUE_TAG, String.valueOf(ry));
		Element cityNameElt = wholeDoc.createElement(CITY_NAME);
		cityNameElt.setAttribute(VALUE_TAG, cityName);
		Element airportNameElt = wholeDoc.createElement(AIRPORT_NAME);
		airportNameElt.setAttribute(VALUE_TAG, airportName);
		parameterElt.appendChild(nameElt);
		parameterElt.appendChild(lxElt);
		parameterElt.appendChild(lyElt);
		parameterElt.appendChild(rxElt);
		parameterElt.appendChild(ryElt);
		parameterElt.appendChild(cityNameElt);
		parameterElt.appendChild(airportNameElt);
		
		if (geomNameMap.containsKey(name)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, DUPLICATE_TERMINAL_NAME);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Terminal t = new Terminal(lx.floatValue(), ly.floatValue(), rx.floatValue(), ry.floatValue(), name);
		Metropole m = worldMap.get(new Point2D.Float(rx.floatValue(), ry.floatValue()));
		if (m != null) {
			if (m.geomPointSet.contains(t)) {
				errorElt.setAttribute(ERROR_TYPE_TAG, DUPLICATE_TERMINAL_COORDINATES);
				errorElt.appendChild(commandElt);
				errorElt.appendChild(parameterElt);
				result.appendChild(errorElt);
				return;
			}
		}
		if (rx < 0 || ry < 0 || rx >= remoteSpatialWidth || ry >= remoteSpatialHeight) {
			errorElt.setAttribute(ERROR_TYPE_TAG, TERMINAL_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m != null && m.quadTree.ifGeomPointOutOfBounds(t)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, TERMINAL_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!airportMap.containsKey(airportName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (geomNameMap.get(airportName) != m) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_NOT_IN_SAME_METROPOLE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Airport a = airportMap.get(airportName);
		if (!cityMap.containsKey(cityName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CONNECTING_CITY_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City c = cityMap.get(cityName);
		if (geomNameMap.get(cityName) != m) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CONNECTING_CITY_NOT_IN_SAME_METROPOLE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (m == null || !m.quadTree.ifGeomAlreadyMapped(c)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CONNECTING_CITY_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Road r = new Road(c, t);
		if (m.quadTree.ifRoadIntersects(r)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_INTERSECTS_ANOTHER_ROAD);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!m.quadTree.addRoad(r)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, TERMINAL_VIOLATES_PMRULES);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		t.setConnectedCity(c);
		t.setAirport(a);
		t.setConnectedRoad(r);
		a.addTerminal(t);
		terminalMap.put(name, t);
		geomNameMap.put(name, m);
		m.geomPointSet.add(t);
		
		roadAj.addRoad(r);
		Road directRoad = new Road(t, a);
		roadAj.addRoad(directRoad);
		
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}
	
	private static void unmapAirport(Integer id, String name) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, UNMAP_AIRPORT);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element airportNameElt = wholeDoc.createElement(DELETE_CITY_NAME);
		airportNameElt.setAttribute(VALUE_TAG, name);
		parameterElt.appendChild(airportNameElt);
		if (!airportMap.containsKey(name)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Airport a = airportMap.get(name);
		Metropole m = worldMap.get(new Point2D.Float(a.getRemotePoint().x, a.getRemotePoint().y));
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		for (Terminal t : a.associatedTerminals) {
			Element unmappedElt = wholeDoc.createElement(TERMINAL_UNMAPPED_TAG);
			unmappedElt.setAttribute(CREATE_CITY_NAME, t.getName());
			unmappedElt.setAttribute(AIRPORT_NAME, t.airport.getName());
			unmappedElt.setAttribute(CITY_NAME, t.connectedCity.getName());
			unmappedElt.setAttribute(LOCAL_X, String.valueOf(Math.round(t.x)));
			unmappedElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(t.y)));
			unmappedElt.setAttribute(REMOTE_X, String.valueOf(Math.round(t.getRemotePoint().x)));
			unmappedElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(t.getRemotePoint().y)));
			outputElt.appendChild(unmappedElt);
			terminalMap.remove(t.getName());
			geomNameMap.remove(t.getName());
			m.geomPointSet.remove(t);
			m.quadTree.removeRoad(t.connectedRoad);
			roadAj.removeGeomPoint(t.getName());
		}
		airportMap.remove(name);
		geomNameMap.remove(name);
		m.geomPointSet.remove(a);
		m.quadTree.removeAirport(a);
		roadAj.removeGeomPoint(name);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}
	
	private static void unmapTerminal(Integer id, String name) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, UNMAP_TERMINAL);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element airportNameElt = wholeDoc.createElement(DELETE_CITY_NAME);
		airportNameElt.setAttribute(VALUE_TAG, name);
		parameterElt.appendChild(airportNameElt);
		if (!terminalMap.containsKey(name)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, AIRPORT_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Terminal t = terminalMap.get(name);
		Metropole m = worldMap.get(new Point2D.Float(t.getRemotePoint().x, t.getRemotePoint().y));
		m.quadTree.removeRoad(t.connectedRoad);
		roadAj.removeGeomPoint(t.getName());
		t.airport.removeTerminal(t);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		if (t.airport.associatedTerminals.size() == 0) {
			airportMap.remove(t.airport.getName());
			geomNameMap.remove(t.airport.getName());
			m.geomPointSet.remove(t.airport);
			roadAj.removeGeomPoint(t.airport.getName());
			m.quadTree.removeAirport(t.airport);
			Element airportUnmappedElt = wholeDoc.createElement(AIRPORT_UNMAPPED_TAG);
			airportUnmappedElt.setAttribute(CREATE_CITY_NAME, t.airport.getName());
			airportUnmappedElt.setAttribute(LOCAL_X, String.valueOf(Math.round(t.airport.x)));
			airportUnmappedElt.setAttribute(LOCAL_Y, String.valueOf(Math.round(t.airport.y)));
			airportUnmappedElt.setAttribute(REMOTE_X, String.valueOf(Math.round(t.airport.getRemotePoint().x)));
			airportUnmappedElt.setAttribute(REMOTE_Y, String.valueOf(Math.round(t.airport.getRemotePoint().y)));
			outputElt.appendChild(airportUnmappedElt);
		}
		terminalMap.remove(name);
		geomNameMap.remove(name);
		m.geomPointSet.remove(t);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}
	
	private static void mst(Integer id, String start) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, MST);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element startNameElt = wholeDoc.createElement(MAP_ROAD_START);
		startNameElt.setAttribute(VALUE_TAG, start);
		parameterElt.appendChild(startNameElt);
		if (!cityMap.containsKey(start)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_DOES_NOT_EXIST);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City c = cityMap.get(start);
		Metropole m = geomNameMap.get(start);
		if (m == null || !m.quadTree.ifGeomAlreadyMapped(c)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		MSTSolver solver = new MSTSolver();
		solver.solveMST(geomNameMap, roadAj, c);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element mstElt = wholeDoc.createElement(MST);
		mstElt.setAttribute(DISTANCE_SPANNED, String.format("%.3f", solver.getTotal()));
		outputElt.appendChild(mstElt);
		solver.printXML(start, wholeDoc, mstElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

}
