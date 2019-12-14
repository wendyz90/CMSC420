package cmsc420.meeshquest.part3;

import static cmsc420.meeshquest.primitive.Naming.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import cmsc420.meeshquest.primitive.City;
import cmsc420.meeshquest.primitive.CityCoordinateComparator;
import cmsc420.meeshquest.primitive.NameComparator;
import cmsc420.meeshquest.primitive.Path;
import cmsc420.meeshquest.primitive.Road;
import cmsc420.meeshquest.primitive.RoadAdjacencyList;
import cmsc420.sortedmap.AvlGTree;
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	// Private data structure
	private static Map<String, City> cityMap;
	private static Set<City> citySet;
	private static AvlGTree<String, City> avlTree;
	private static PMQuadTree quadTree;
	private static Document wholeDoc;
	private static RoadAdjacencyList roadAj;
	private static DijkstraImpl dijkstraImpl;
	private static CanvasPlus canvas;
	private static Element result;
	private static Integer spaceWidth;
	private static Integer spaceHeight;
	private static Integer g;
	private static Integer pmOrder;

	public static void main(String[] args)
			throws ParserConfigurationException, TransformerException {
		NameComparator nc = new NameComparator();
		CityCoordinateComparator ccc = new CityCoordinateComparator();
		wholeDoc = XmlUtility.getDocumentBuilder().newDocument();
		result = wholeDoc.createElement("results");
		wholeDoc.appendChild(result);
		cityMap = new TreeMap<>(nc);
		citySet = new TreeSet<>(ccc);
		roadAj = new RoadAdjacencyList();
		try {
			Document doc = XmlUtility.validateNoNamespace(System.in);
			Element docElement = doc.getDocumentElement();
			spaceWidth = Integer.parseInt(docElement.getAttribute(WIDTH));
			spaceHeight = Integer.parseInt(docElement.getAttribute(HEIGHT));
			g = Integer.parseInt(docElement.getAttribute(G));
			pmOrder = Integer.parseInt(docElement.getAttribute(PM_ORDER));
			quadTree = new PMQuadTree(new PM3Validator(), spaceWidth, spaceHeight, pmOrder);
			avlTree = new AvlGTree<>(new NameComparator(), g);
			canvas = new CanvasPlus("MeeshQuest", spaceWidth, spaceHeight);
			canvas.addRectangle(0, 0, spaceWidth, spaceHeight, Color.BLACK, false);
			NodeList nl = docElement.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				// System.out.println(n.getNodeName());
				if (n.getNodeName().equals(CREATE_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String name = ne.getAttribute(CREATE_CITY_NAME);
						String xs = ne.getAttribute(CREATE_CITY_X);
						String ys = ne.getAttribute(CREATE_CITY_Y);
						String radiusString = ne.getAttribute(CREATE_CITY_RADIUS);
						String color = ne.getAttribute(CREATE_CITY_COLOR);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						Integer x = Integer.parseInt(xs);
						Integer y = Integer.parseInt(ys);
						Integer radius = Integer.parseInt(radiusString);
						createCity(id, name, x, y, radius, color);
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
				} else if (n.getNodeName().equals(MAP_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String cityName = ne.getAttribute(MAP_CITY_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						mapCity(id, cityName);
					}
				} else if (n.getNodeName().equals(UNMAP_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String cityName = ne.getAttribute(UNMAP_CITY_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						unMapCity(id, cityName);
					}
				} else if (n.getNodeName().equals(PRINT_PMQUADTREE_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						printPMQuadtree(id);
					}
				} else if (n.getNodeName().equals(SAVE_MAP_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String fileName = ne.getAttribute(SAVE_MAP_NAME);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						saveMap(id, fileName);
					}
				} else if (n.getNodeName().equals(RANGE_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(RANGE_CITY_X));
						Integer y = Integer.parseInt(ne.getAttribute(RANGE_CITY_Y));
						Integer radius = Integer.parseInt(ne.getAttribute(RANGE_CITY_RADIUS));
						String name = ne.getAttribute(RANGE_CITY_SAVEMAP);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						rangeCities(id, x, y, radius, name);
					}
				} else if (n.getNodeName().equals(RANGE_ROAD_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(RANGE_ROAD_X));
						Integer y = Integer.parseInt(ne.getAttribute(RANGE_ROAD_Y));
						Integer radius = Integer.parseInt(ne.getAttribute(RANGE_ROAD_RADIUS));
						String name = ne.getAttribute(RANGE_ROAD_SAVEMAP);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						rangeRoads(id, x, y, radius, name);
					}
				} else if (n.getNodeName().equals(NEAREST_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(NEAREST_CITY_X));
						Integer y = Integer.parseInt(ne.getAttribute(NEAREST_CITY_Y));
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						nearestCity(id, x, y, false);
					}
				} else if (n.getNodeName().equals(NEAREST_ISOLATED_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(NEAREST_ISOLATED_CITY_X));
						Integer y = Integer.parseInt(ne.getAttribute(NEAREST_ISOLATED_CITY_Y));
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						nearestCity(id, x, y, true);
					}
				} else if (n.getNodeName().equals(NEAREST_ROAD_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(NEAREST_ROAD_X));
						Integer y = Integer.parseInt(ne.getAttribute(NEAREST_ROAD_Y));
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						nearestRoad(id, x, y);
					}
				} else if (n.getNodeName().equals(NEAREST_CITY_TO_ROAD_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String start = ne.getAttribute(NEAREST_CITY_TO_ROAD_START);
						String end = ne.getAttribute(NEAREST_CITY_TO_ROAD_END);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						nearestCityToRoad(id, start, end);
					}
				} else if (n.getNodeName().equals(SHORTEST_PATH_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String start = ne.getAttribute(SHORTEST_PATH_START);
						String end = ne.getAttribute(SHORTEST_PATH_END);
						String saveMap = ne.getAttribute(SHORTEST_PATH_SAVE_MAP);
						String saveHTML = ne.getAttribute(SHORTEST_PATH_SAVE_HTML);
						String ids = ne.getAttribute(COMMAND_ID_TAG);
						Integer id = null;
						if (!ids.equals("")) {
							id = Integer.parseInt(ids);
						}
						shortestPath(id, start, end, saveMap, saveHTML);
					}
				} else {
					// throw new ParserConfigurationException();
				}
			}
			XmlUtility.print(wholeDoc);
		} catch (ParserConfigurationException | IOException | SAXException | URISyntaxException e) {
			// System.out.println(e);
			wholeDoc = XmlUtility.getDocumentBuilder().newDocument();
			Element elt = wholeDoc.createElement(FATAL_ERROR_TAG);
			wholeDoc.appendChild(elt);
			XmlUtility.print(wholeDoc);
		}
	}

	private static void createCity(Integer id, String name, Integer x, Integer y, Integer radius,
			String color) {
		// Preparer output format
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, CREATE_CITY_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element nameElt = wholeDoc.createElement(CREATE_CITY_NAME);
		Element xElt = wholeDoc.createElement(CREATE_CITY_X);
		Element yElt = wholeDoc.createElement(CREATE_CITY_Y);
		Element radiusElt = wholeDoc.createElement(CREATE_CITY_RADIUS);
		Element colorElt = wholeDoc.createElement(CREATE_CITY_COLOR);
		nameElt.setAttribute(VALUE_TAG, name);
		xElt.setAttribute(VALUE_TAG, x.toString());
		yElt.setAttribute(VALUE_TAG, y.toString());
		radiusElt.setAttribute(VALUE_TAG, radius.toString());
		colorElt.setAttribute(VALUE_TAG, color);
		parameterElt.appendChild(nameElt);
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		parameterElt.appendChild(radiusElt);
		parameterElt.appendChild(colorElt);

		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);

		// Construct city object
		int success = 0;
		City c = new City(name, x.floatValue(), y.floatValue(), radius, color);
		if (citySet.contains(c)) {
			success = -1;
		} else if (cityMap.containsKey(name)) {
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
		cityMap.put(name, c);
		citySet.add(c);
		avlTree.put(name, c);
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

		if (cityMap.size() == 0) {
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

		List<City> cityList = new ArrayList<>();

		if (sortBy.equals(LIST_CITY_SORTBY_NAME)) {
			Set<Map.Entry<String, City>> set = cityMap.entrySet();
			Iterator<Entry<String, City>> it = set.iterator();
			while (it.hasNext()) {
				Map.Entry<String, City> me = (Map.Entry<String, City>) it.next();
				cityList.add(me.getValue());
			}
		} else if (sortBy.equals(LIST_CITY_SORTBY_COORDINATE)) {
			Iterator<City> it = citySet.iterator();
			while (it.hasNext()) {
				cityList.add(it.next());
			}
		} else {
			throw new ParserConfigurationException();
		}

		for (City city : cityList) {
			Element cityElt = wholeDoc.createElement(CITY_TAG);
			cityElt.setAttribute(CREATE_CITY_NAME, city.getName());
			cityElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(city.getX())));
			cityElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(city.getY())));
			cityElt.setAttribute(CREATE_CITY_RADIUS, city.getRadius().toString());
			cityElt.setAttribute(CREATE_CITY_COLOR, city.getColor());
			cityListElt.appendChild(cityElt);
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
		cityMap.clear();
		citySet.clear();
		quadTree = new PMQuadTree(new PM3Validator(), spaceWidth, spaceHeight, pmOrder);
		avlTree.clear();
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

		// Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		// Element errorElt = wholeDoc.createElement(ERROR_TAG);
		// Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		// commandElt.setAttribute(COMMAND_NAME_TAG, DELETE_CITY_CMD);
		// commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		// Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		// Element cityNameElt = wholeDoc.createElement(DELETE_CITY_NAME);
		// cityNameElt.setAttribute(VALUE_TAG, cityName);
		// parameterElt.appendChild(cityNameElt);
		// if (!cityMap.containsKey(cityName)) {
		// errorElt.setAttribute(ERROR_TYPE_TAG, CITY_DOES_NOT_EXIST);
		// errorElt.appendChild(commandElt);
		// errorElt.appendChild(parameterElt);
		// result.appendChild(errorElt);
		// return;
		// }
		// City c = cityMap.get(cityName);
		// Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		// if (quadTree.getNodeMap().containsKey(cityName)) {
		// Boolean success = quadTree.remove(cityName);
		// assert success;
		// Element unmappedElt = wholeDoc.createElement(CITY_UNMAPPED_TAG);
		// unmappedElt.setAttribute(CREATE_CITY_NAME, c.getName());
		// unmappedElt.setAttribute(CREATE_CITY_X,
		// String.valueOf(Math.round(c.getX())));
		// unmappedElt.setAttribute(CREATE_CITY_Y,
		// String.valueOf(Math.round(c.getY())));
		// unmappedElt.setAttribute(CREATE_CITY_COLOR, c.getColor());
		// unmappedElt.setAttribute(CREATE_CITY_RADIUS,
		// String.valueOf(c.getRadius()));
		// outputElt.appendChild(unmappedElt);
		// }
		// cityMap.remove(cityName);
		// citySet.remove(c);
		// successElt.appendChild(commandElt);
		// successElt.appendChild(parameterElt);
		// successElt.appendChild(outputElt);
		// result.appendChild(successElt);
		throw new UnsupportedOperationException();
	}

	private static void mapCity(Integer id, String cityName) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, MAP_CITY_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element cityNameElt = wholeDoc.createElement(MAP_CITY_NAME);
		cityNameElt.setAttribute(VALUE_TAG, cityName);
		parameterElt.appendChild(cityNameElt);
		if (!cityMap.containsKey(cityName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NAME_NOT_IN_DICTIONARY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City c = cityMap.get(cityName);
		if (quadTree.ifCityAlreadyMapped(c)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_ALREADY_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (quadTree.ifCityOutOfBounds(c)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_OUTOF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		quadTree.addIsolatedCity(c);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
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
		Road road = new Road(startCity, endCity);
		if (quadTree.ifStartOrEndIsIsolated(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, START_OR_END_IS_ISOLATED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (quadTree.ifRoadAlreadyMapped(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_ALREADY_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (quadTree.ifRoadOutOfBounds(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_OUT_OF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		quadTree.addRoad(road);
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

	private static void unMapCity(Integer id, String cityName) {
		// Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		// Element errorElt = wholeDoc.createElement(ERROR_TAG);
		// Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		// commandElt.setAttribute(COMMAND_NAME_TAG, UNMAP_CITY_CMD);
		// commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		// Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		// Element cityNameElt = wholeDoc.createElement(UNMAP_CITY_NAME);
		// cityNameElt.setAttribute(VALUE_TAG, cityName);
		// parameterElt.appendChild(cityNameElt);
		// if (!cityMap.containsKey(cityName)) {
		// errorElt.setAttribute(ERROR_TYPE_TAG, NAME_NOT_IN_DICTIONARY);
		// errorElt.appendChild(commandElt);
		// errorElt.appendChild(parameterElt);
		// result.appendChild(errorElt);
		// return;
		// }
		// if (!quadTree.getNodeMap().containsKey(cityName)) {
		// errorElt.setAttribute(ERROR_TYPE_TAG, CITY_NOT_MAPPED);
		// errorElt.appendChild(commandElt);
		// errorElt.appendChild(parameterElt);
		// result.appendChild(errorElt);
		// return;
		// }
		// Boolean success = quadTree.remove(cityName);
		// assert success;
		// Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		// successElt.appendChild(commandElt);
		// successElt.appendChild(parameterElt);
		// successElt.appendChild(outputElt);
		// result.appendChild(successElt);
		throw new UnsupportedOperationException();
	}

	private static void printPMQuadtree(Integer id) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, PRINT_PMQUADTREE_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		if (quadTree.ifEmpty()) {
			errorElt.setAttribute(ERROR_TYPE_TAG, MAP_IS_EMPTY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element quadTreeElt = wholeDoc.createElement(QUAD_TREE);
		quadTreeElt.setAttribute(ORDER_TAG, String.valueOf(pmOrder));
		outputElt.appendChild(quadTreeElt);
		quadTree.printPMQuadTree(quadTreeElt, wholeDoc);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void saveMap(Integer id, String name) throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, SAVE_MAP_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element fileNameElt = wholeDoc.createElement(SAVE_MAP_NAME);
		fileNameElt.setAttribute(VALUE_TAG, name);
		parameterElt.appendChild(fileNameElt);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		quadTree.drawMap(canvas);
		//canvas.draw();
		canvas.save(name);
		canvas.dispose();
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void rangeCities(Integer id, Integer x, Integer y, Integer radius, String name)
			throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, RANGE_CITY_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(RANGE_CITY_X);
		xElt.setAttribute(VALUE_TAG, String.valueOf(x));
		Element yElt = wholeDoc.createElement(RANGE_CITY_Y);
		yElt.setAttribute(VALUE_TAG, String.valueOf(y));
		Element radiusElt = wholeDoc.createElement(RANGE_CITY_RADIUS);
		radiusElt.setAttribute(VALUE_TAG, String.valueOf(radius));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		parameterElt.appendChild(radiusElt);
		if (!name.equals("")) {
			Element saveMapElt = wholeDoc.createElement(SAVE_MAP_CMD);
			saveMapElt.setAttribute(VALUE_TAG, name);
			parameterElt.appendChild(saveMapElt);
		}
		Set<City> inRange = quadTree.rangeCities(x, y, radius);
		if (!name.equals("")) {
			quadTree.drawMap(canvas);
			quadTree.drawRange(canvas, x, y, radius);
			canvas.save(name);
			canvas.dispose();
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
			cityElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(city.getX())));
			cityElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(city.getY())));
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

	private static void rangeRoads(Integer id, Integer x, Integer y, Integer radius, String name)
			throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, RANGE_ROAD_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(RANGE_ROAD_X);
		xElt.setAttribute(VALUE_TAG, String.valueOf(x));
		Element yElt = wholeDoc.createElement(RANGE_ROAD_Y);
		yElt.setAttribute(VALUE_TAG, String.valueOf(y));
		Element radiusElt = wholeDoc.createElement(RANGE_ROAD_RADIUS);
		radiusElt.setAttribute(VALUE_TAG, String.valueOf(radius));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		parameterElt.appendChild(radiusElt);
		if (!name.equals("")) {
			Element saveMapElt = wholeDoc.createElement(SAVE_MAP_CMD);
			saveMapElt.setAttribute(VALUE_TAG, name);
			parameterElt.appendChild(saveMapElt);
		}
		Set<Road> inRange = quadTree.rangeRoads(x, y, radius);
		if (!name.equals("")) {
			quadTree.drawMap(canvas);
			quadTree.drawRange(canvas, x, y, radius);
			canvas.save(name);
			canvas.dispose();
		}
		if (inRange.size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NO_ROADS_EXIST_IN_RANGE);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Iterator<Road> it = inRange.iterator();
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityListElt = wholeDoc.createElement(ROAD_LIST_TAG);
		while (it.hasNext()) {
			Road road = it.next();
			Element roadElt = wholeDoc.createElement(ROAD_TAG);
			roadElt.setAttribute(MAP_ROAD_START, road.getStart().getName());
			roadElt.setAttribute(MAP_ROAD_END, road.getEnd().getName());
			cityListElt.appendChild(roadElt);
		}
		outputElt.appendChild(cityListElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void nearestCity(Integer id, Integer x, Integer y, boolean ifIsolated) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG,
				ifIsolated ? NEAREST_ISOLATED_CITY_CMD : NEAREST_CITY_CMD);
				
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(NEAREST_CITY_X);
		xElt.setAttribute(VALUE_TAG, String.valueOf(x));
		Element yElt = wholeDoc.createElement(NEAREST_CITY_Y);
		yElt.setAttribute(VALUE_TAG, String.valueOf(y));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		City city = quadTree.nearestCity(x, y, ifIsolated);
		
		
		if (city == null) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_NOT_FOUND);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityElt = wholeDoc.createElement(ifIsolated ? ISOLATED_CITY_TAG : CITY_TAG);
		cityElt.setAttribute(CREATE_CITY_NAME, city.getName());
		cityElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(city.getX())));
		cityElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(city.getY())));
		cityElt.setAttribute(CREATE_CITY_RADIUS, city.getRadius().toString());
		cityElt.setAttribute(CREATE_CITY_COLOR, city.getColor());
		outputElt.appendChild(cityElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}
	
	
	
	
	
	

	private static void nearestRoad(Integer id, Integer x, Integer y) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, NEAREST_ROAD_CMD);
		if (id != null) {
			commandElt.setAttribute(COMMAND_ID_TAG, String.valueOf(id));
		}
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(NEAREST_ROAD_X);
		xElt.setAttribute(VALUE_TAG, String.valueOf(x));
		Element yElt = wholeDoc.createElement(NEAREST_ROAD_Y);
		yElt.setAttribute(VALUE_TAG, String.valueOf(y));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		Road road = quadTree.nearestRoad(x, y);
		if (road == null) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_NOT_FOUND);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element roadElt = wholeDoc.createElement(ROAD_TAG);
		roadElt.setAttribute(MAP_ROAD_START, road.getStart().getName());
		roadElt.setAttribute(MAP_ROAD_END, road.getEnd().getName());
		outputElt.appendChild(roadElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void nearestCityToRoad(Integer id, String s, String e) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, NEAREST_CITY_TO_ROAD_CMD);
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
		if (!cityMap.containsKey(s) || !cityMap.containsKey(e) || s.equals(e)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_IS_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City startCity = cityMap.get(s);
		City endCity = cityMap.get(e);
		Road road = new Road(startCity, endCity);
		if (!quadTree.ifRoadAlreadyMapped(road)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, ROAD_IS_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City city = quadTree.nearestCityToRoad(road);
		if (city == null) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NO_OTHER_CITIES_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityElt = wholeDoc.createElement(CITY_TAG);
		cityElt.setAttribute(CREATE_CITY_NAME, city.getName());
		cityElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(city.getX())));
		cityElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(city.getY())));
		cityElt.setAttribute(CREATE_CITY_RADIUS, city.getRadius().toString());
		cityElt.setAttribute(CREATE_CITY_COLOR, city.getColor());
		outputElt.appendChild(cityElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void shortestPath(Integer id, String s, String e, String saveMap, String saveHTML) 
			throws IOException, ParserConfigurationException, TransformerException, URISyntaxException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, SHORTEST_PATH_CMD);
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
		
		if (!saveMap.equals("")) {
			Element saveMapElt = wholeDoc.createElement(SHORTEST_PATH_SAVE_MAP);
			saveMapElt.setAttribute(VALUE_TAG, saveMap);
			parameterElt.appendChild(saveMapElt);
		}
		if (!saveHTML.equals("")) {
			Element saveHTMLElt = wholeDoc.createElement(SHORTEST_PATH_SAVE_HTML);
			saveHTMLElt.setAttribute(VALUE_TAG, saveHTML);
			parameterElt.appendChild(saveHTMLElt);
		}
		
		
		
		if (!cityMap.containsKey(s)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NON_EXISTENT_START);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City startCity = cityMap.get(s);
		if (!quadTree.ifCityExistent(startCity)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NON_EXISTENT_START);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!cityMap.containsKey(e)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NON_EXISTENT_END);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City endCity = cityMap.get(e);
		if (!quadTree.ifCityExistent(endCity)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NON_EXISTENT_END);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		
		dijkstraImpl = new DijkstraImpl(roadAj);
		Path path = dijkstraImpl.getShortestPath(startCity, endCity);
		if (path == null || path.getCityList().size() == 0) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NO_PATH_EXISTS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		
		
		
		
		if (!saveMap.equals("")) {
			CanvasPlus shortestPathCanvas = new CanvasPlus("MeeshQuest", spaceWidth, spaceHeight);
			shortestPathCanvas.addRectangle(0, 0, spaceWidth, spaceHeight, Color.BLACK, false);
			path.drawPath(shortestPathCanvas);
			shortestPathCanvas.save(saveMap);
			shortestPathCanvas.dispose();
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element pathElt = wholeDoc.createElement(PATH_TAG);
		pathElt.setAttribute(PATH_LENGTH_TAG, String.format("%.3f", path.getDistance()));
		pathElt.setAttribute(PATH_HOPS_TAG, String.valueOf(path.getHops()));
		path.printPath(pathElt, wholeDoc);
		outputElt.appendChild(pathElt);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		if (!saveHTML.equals("")) {
			Document shortestPathDoc = XmlUtility.getDocumentBuilder().newDocument();
			Node spNode = shortestPathDoc.importNode(successElt, true);
			shortestPathDoc.appendChild(spNode);
			//XmlUtility.transform(shortestPathDoc, new File(MeeshQuest.class.getResource("/shortestPath.xsl").toURI()), new File(saveHTML + ".html"));
		}
		result.appendChild(successElt);
	}
}
