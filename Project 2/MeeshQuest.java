package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	//
	private static final String WIDTH = "spatialWidth";
	private static final String HEIGHT = "spatialHeight";

	// Create City Command
	private static final String CREATE_CITY_CMD = "createCity";
	private static final String CREATE_CITY_NAME = "name";
	private static final String CREATE_CITY_X = "x";
	private static final String CREATE_CITY_Y = "y";
	private static final String CREATE_CITY_RADIUS = "radius";
	private static final String CREATE_CITY_COLOR = "color";

	// List City Command
	private static final String LIST_CITY_CMD = "listCities";
	private static final String LIST_CITY_SORTBY = "sortBy";
	private static final String LIST_CITY_SORTBY_NAME = "name";
	private static final String LIST_CITY_SORTBY_COORDINATE = "coordinate";

	// Clear All Command
	private static final String CLEAR_ALL_CMD = "clearAll";

	// Map City Command
	private static final String MAP_CITY_CMD = "mapCity";
	private static final String MAP_CITY_NAME = "name";

	// Delete City Command
	private static final String DELETE_CITY_CMD = "deleteCity";
	private static final String DELETE_CITY_NAME = "name";

	// Unmap City Command
	private static final String UNMAP_CITY_CMD = "unmapCity";
	private static final String UNMAP_CITY_NAME = "name";

	// Print PR Tree Command
	private static final String PRINT_PRQUADTREE_CMD = "printPRQuadtree";

	// Save Map Command
	private static final String SAVE_MAP_CMD = "saveMap";
	private static final String SAVE_MAP_NAME = "name";

	// Range City Command
	private static final String RANGE_CITY_CMD = "rangeCities";
	private static final String RANGE_CITY_X = "x";
	private static final String RANGE_CITY_Y = "y";
	private static final String RANGE_CITY_RADIUS = "radius";
	private static final String RANGE_CITY_SAVEMAP = "saveMap";

	// Nearest City Command
	private static final String NEAREST_CITY_CMD = "nearestCity";
	private static final String NEAREST_CITY_X = "x";
	private static final String NEAREST_CITY_Y = "y";

	// Output tag
	private static final String SUCCESS_TAG = "success";
	private static final String ERROR_TAG = "error";
	private static final String VALUE_TAG = "value";
	private static final String PARAMETERS_TAG = "parameters";
	private static final String OUTPUT_TAG = "output";
	private static final String COMMAND_TAG = "command";
	private static final String COMMAND_NAME_TAG = "name";
	private static final String FATAL_ERROR_TAG = "fatalError";
	private static final String ERROR_TYPE_TAG = "type";
	private static final String CITY_LIST_TAG = "cityList";
	private static final String CITY_TAG = "city";
	private static final String CITY_UNMAPPED_TAG = "cityUnmapped";
	private static final String QUAD_TREE = "quadtree";

	// Error tag
	private static final String DUPLICATE_CITY_COORDINATES = "duplicateCityCoordinates";
	private static final String DUPLICATE_CITY_NAME = "duplicateCityName";
	private static final String NO_CITIES_TO_LIST = "noCitiesToList";
	private static final String NAME_NOT_IN_DICTIONARY = "nameNotInDictionary";
	private static final String CITY_ALREADY_MAPPED = "cityAlreadyMapped";
	private static final String CITY_OUTOF_BOUNDS = "cityOutOfBounds";
	private static final String CITY_NOT_MAPPED = "cityNotMapped";
	private static final String MAP_IS_EMPTY = "mapIsEmpty";
	private static final String CITY_DOES_NOT_EXIST = "cityDoesNotExist";
	private static final String NO_CITIES_EXIST_IN_RANGE = "noCitiesExistInRange";

	// Private data structure
	private static Map<String, City> cityMap;
	private static Set<City> citySet;
	private static PrQuadTree<City> quadTree;
	private static Document wholeDoc;
	private static CanvasPlus canvas;
	private static Element result;
	private static Integer spaceWidth;
	private static Integer spaceHeight;

	static class City extends Point2D.Float {
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
	}

	static class CityNameComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			int i = o1.compareTo(o2);
			if (i > 0) {
				return -1;
			} else if (i < 0) {
				return 1;
			} else {
				return 0;
			}
		}

	}

	static class CityNameComparatorCity implements Comparator<City> {

		@Override
		public int compare(City c1, City c2) {
			return c2.name.compareTo(c1.name);
		}

	}

	static class CityCoordinateComparator implements Comparator<City> {

		@Override
		public int compare(City o1, City o2) {
			double x1 = o1.getX();
			double x2 = o2.getX();
			double y1 = o1.getY();
			double y2 = o2.getY();
			if (y1 != y2) {
				if (y1 < y2) {
					return -1;
				} else {
					return 1;
				}
			} else {
				if (x1 < x2) {
					return -1;
				} else if (x1 == x2) {
					return 0;
				} else {
					return 1;
				}
			}
		}
	}

	public static void main(String[] args)
			throws ParserConfigurationException, TransformerException {
		CityNameComparator cnc = new CityNameComparator();
		CityCoordinateComparator ccc = new CityCoordinateComparator();
		wholeDoc = XmlUtility.getDocumentBuilder().newDocument();
		result = wholeDoc.createElement("results");
		wholeDoc.appendChild(result);
		cityMap = new TreeMap<>(cnc);
		citySet = new TreeSet<>(ccc);
		try {
			Document doc = XmlUtility.validateNoNamespace(System.in);
			Element docElement = doc.getDocumentElement();
			spaceWidth = Integer.parseInt(docElement.getAttribute(WIDTH));
			spaceHeight = Integer.parseInt(docElement.getAttribute(HEIGHT));
			quadTree = new PrQuadTree<City>(spaceWidth, 0, spaceHeight, 0);
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
						Integer x = Integer.parseInt(xs);
						Integer y = Integer.parseInt(ys);
						Integer radius = Integer.parseInt(radiusString);
						createCity(name, x, y, radius, color);
					}
				} else if (n.getNodeName().equals(LIST_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String sortBy = ne.getAttribute(LIST_CITY_SORTBY);
						listCities(sortBy);
					}
				} else if (n.getNodeName().equals(CLEAR_ALL_CMD)) {
					// There is no parameter for this command.
					clearAll();
				} else if (n.getNodeName().equals(DELETE_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String cityName = ne.getAttribute(DELETE_CITY_NAME);
						deleteCity(cityName);
					}
				} else if (n.getNodeName().equals(MAP_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String cityName = ne.getAttribute(MAP_CITY_NAME);
						mapCity(cityName);
					}
				} else if (n.getNodeName().equals(UNMAP_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String cityName = ne.getAttribute(UNMAP_CITY_NAME);
						unMapCity(cityName);
					}
				} else if (n.getNodeName().equals(PRINT_PRQUADTREE_CMD)) {
					printPRQuadtree();
				} else if (n.getNodeName().equals(SAVE_MAP_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						String fileName = ne.getAttribute(SAVE_MAP_NAME);
						saveMap(fileName);
					}
				} else if (n.getNodeName().equals(RANGE_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(RANGE_CITY_X));
						Integer y = Integer.parseInt(ne.getAttribute(RANGE_CITY_Y));
						Integer radius = Integer.parseInt(ne.getAttribute(RANGE_CITY_RADIUS));
						String name = ne.getAttribute(RANGE_CITY_SAVEMAP);
						rangeSearch(x, y, radius, name);
					}
				} else if (n.getNodeName().equals(NEAREST_CITY_CMD)) {
					if (n instanceof Element) {
						Element ne = (Element) n;
						Integer x = Integer.parseInt(ne.getAttribute(NEAREST_CITY_X));
						Integer y = Integer.parseInt(ne.getAttribute(NEAREST_CITY_Y));
						nearestCity(x, y);
					}
				} else {
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

	private static void createCity(String name, Integer x, Integer y, Integer radius,
			String color) {
		// Preparer output format
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, CREATE_CITY_CMD);

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
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void listCities(String sortBy) throws ParserConfigurationException {
		// Prepare output format
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, LIST_CITY_CMD);

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

	private static void clearAll() {
		cityMap.clear();
		citySet.clear();
		quadTree = new PrQuadTree<City>(spaceWidth, 0, spaceHeight, 0);
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, CLEAR_ALL_CMD);
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void deleteCity(String cityName) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, DELETE_CITY_CMD);
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
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		if (quadTree.getNodeMap().containsKey(cityName)) {
			Boolean success = quadTree.remove(cityName);
			assert success;
			Element unmappedElt = wholeDoc.createElement(CITY_UNMAPPED_TAG);
			unmappedElt.setAttribute(CREATE_CITY_NAME, c.name);
			unmappedElt.setAttribute(CREATE_CITY_X, String.valueOf(Math.round(c.getX())));
			unmappedElt.setAttribute(CREATE_CITY_Y, String.valueOf(Math.round(c.getY())));
			unmappedElt.setAttribute(CREATE_CITY_COLOR, c.color);
			unmappedElt.setAttribute(CREATE_CITY_RADIUS, String.valueOf(c.radius));
			outputElt.appendChild(unmappedElt);
		}
		cityMap.remove(cityName);
		citySet.remove(c);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void mapCity(String cityName) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, MAP_CITY_CMD);
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
		if (quadTree.getNodeMap().containsKey(cityName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_ALREADY_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City c = cityMap.get(cityName);
		if (c.getX() < 0 || c.getX() >= spaceWidth || c.getY() < 0 || c.getY() >= spaceHeight) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_OUTOF_BOUNDS);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Boolean success = quadTree.insert(c, c.name);
		assert success;
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void unMapCity(String cityName) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, UNMAP_CITY_CMD);
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element cityNameElt = wholeDoc.createElement(UNMAP_CITY_NAME);
		cityNameElt.setAttribute(VALUE_TAG, cityName);
		parameterElt.appendChild(cityNameElt);
		if (!cityMap.containsKey(cityName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, NAME_NOT_IN_DICTIONARY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		if (!quadTree.getNodeMap().containsKey(cityName)) {
			errorElt.setAttribute(ERROR_TYPE_TAG, CITY_NOT_MAPPED);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Boolean success = quadTree.remove(cityName);
		assert success;
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void printPRQuadtree() {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, PRINT_PRQUADTREE_CMD);
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		if (quadTree.getNodeMap().isEmpty()) {
			errorElt.setAttribute(ERROR_TYPE_TAG, MAP_IS_EMPTY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element quadTreeElt = wholeDoc.createElement(QUAD_TREE);
		outputElt.appendChild(quadTreeElt);
		quadTree.print(quadTreeElt, wholeDoc);
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void saveMap(String name) throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, SAVE_MAP_CMD);
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element fileNameElt = wholeDoc.createElement(SAVE_MAP_NAME);
		fileNameElt.setAttribute(VALUE_TAG, name);
		parameterElt.appendChild(fileNameElt);
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		quadTree.drawMap(canvas);
		canvas.save(name);
		canvas.dispose();
		successElt.appendChild(commandElt);
		successElt.appendChild(parameterElt);
		successElt.appendChild(outputElt);
		result.appendChild(successElt);
	}

	private static void rangeSearch(Integer x, Integer y, Integer radius, String name)
			throws IOException {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, RANGE_CITY_CMD);
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
		List<City> inRange = quadTree.rangeSearch(x, y, radius);
		if (!name.equals("")) {
			quadTree.drawMap(canvas);
			quadTree.drawCircle(canvas, x, y, radius);
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
		CityNameComparatorCity cncc = new CityNameComparatorCity();
		TreeSet<City> sortedCityList = new TreeSet<City>(cncc);
		sortedCityList.addAll(inRange);
		List<City> cityList = new ArrayList<>();
		Iterator<City> it = sortedCityList.iterator();
		while (it.hasNext()) {
			cityList.add(it.next());
		}
		Element outputElt = wholeDoc.createElement(OUTPUT_TAG);
		Element cityListElt = wholeDoc.createElement(CITY_LIST_TAG);
		for (City city : cityList) {
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

	private static void nearestCity(Integer x, Integer y) {
		Element successElt = wholeDoc.createElement(SUCCESS_TAG);
		Element commandElt = wholeDoc.createElement(COMMAND_TAG);
		Element errorElt = wholeDoc.createElement(ERROR_TAG);
		commandElt.setAttribute(COMMAND_NAME_TAG, NEAREST_CITY_CMD);
		Element parameterElt = wholeDoc.createElement(PARAMETERS_TAG);
		Element xElt = wholeDoc.createElement(NEAREST_CITY_X);
		xElt.setAttribute(VALUE_TAG, String.valueOf(x));
		Element yElt = wholeDoc.createElement(NEAREST_CITY_Y);
		yElt.setAttribute(VALUE_TAG, String.valueOf(y));
		parameterElt.appendChild(xElt);
		parameterElt.appendChild(yElt);
		if (quadTree.getNodeMap().isEmpty()) {
			errorElt.setAttribute(ERROR_TYPE_TAG, MAP_IS_EMPTY);
			errorElt.appendChild(commandElt);
			errorElt.appendChild(parameterElt);
			result.appendChild(errorElt);
			return;
		}
		City city = quadTree.nearestPoint(x, y);
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
}
