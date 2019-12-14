package cmsc420.meeshquest.part1;

import java.awt.geom.Point2D;
import java.io.File;
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

import cmsc420.xml.XmlUtility;

public class MeeshQuest {

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

	// Error tag
	private static final String DUPLICATE_CITY_COORDINATES = "duplicateCityCoordinates";
	private static final String DUPLICATE_CITY_NAME = "duplicateCityName";
	private static final String NO_CITIES_TO_LIST = "noCitiesToList";

	// Private data structure
	private static Map<String, City> cityMap;
	private static Set<City> citySet;
	private static Document wholeDoc;
	private static Element result;

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
			NodeList nl = docElement.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
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
				}
			}
			XmlUtility.print(wholeDoc);
		
		} catch (ParserConfigurationException | IOException | SAXException e) {
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
}
