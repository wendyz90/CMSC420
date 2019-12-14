package cmsc420.meeshquest.primitive;

public final class Naming {

	public static final String WHITE_TAG = "white";
	public static final String GRAY_TAG = "gray";
	public static final String BLACK_TAG = "black";

	public static final String CARDINALITY_TAG = "cardinality";
	public static final String ISOLATED_CITY_TAG = "isolatedCity";
	public static final String ROAD_TAG = "road";
	public static final String X_TAG = "x";
	public static final String Y_TAG = "y";

	// Part4 new added parameters
	public static final String LOCAL_X = "localX";
	public static final String LOCAL_Y = "localY";
	public static final String REMOTE_X = "remoteX";
	public static final String REMOTE_Y = "remoteY";
	
	// Global parameter
	public static final String LOCAL_WIDTH = "localSpatialWidth";
	public static final String LOCAL_HEIGHT = "localSpatialHeight";
	public static final String REMOTE_WIDTH = "remoteSpatialWidth";
	public static final String REMOTE_HEIGHT = "remoteSpatialHeight";
	public static final String G = "g";
	public static final String PM_ORDER = "pmOrder";

	// Create City Command
	public static final String CREATE_CITY_CMD = "createCity";
	public static final String CREATE_CITY_NAME = "name";
	public static final String CREATE_CITY_X = "x";
	public static final String CREATE_CITY_Y = "y";
	public static final String CREATE_CITY_RADIUS = "radius";
	public static final String CREATE_CITY_COLOR = "color";

	// List City Command
	public static final String LIST_CITY_CMD = "listCities";
	public static final String LIST_CITY_SORTBY = "sortBy";
	public static final String LIST_CITY_SORTBY_NAME = "name";
	public static final String LIST_CITY_SORTBY_COORDINATE = "coordinate";

	// Clear All Command
	public static final String CLEAR_ALL_CMD = "clearAll";

	// Print Avl Tree Command
	public static final String PRINT_AVL_TREE_CMD = "printAvlTree";

	// Map Road Command
	public static final String MAP_ROAD_CMD = "mapRoad";
	public static final String MAP_ROAD_START = "start";
	public static final String MAP_ROAD_END = "end";

	// Map City Command
	public static final String MAP_CITY_CMD = "mapCity";
	public static final String MAP_CITY_NAME = "name";

	// Delete City Command
	public static final String DELETE_CITY_CMD = "deleteCity";
	public static final String DELETE_CITY_NAME = "name";

	// Unmap City Command
	public static final String UNMAP_CITY_CMD = "unmapCity";
	public static final String UNMAP_CITY_NAME = "name";

	// Print PR Tree Command
	public static final String PRINT_PMQUADTREE_CMD = "printPMQuadtree";

	// Save Map Command
	public static final String SAVE_MAP_CMD = "saveMap";
	public static final String SAVE_MAP_NAME = "name";

	// Range City Command
	public static final String RANGE_CITY_CMD = "rangeCities";
	public static final String RANGE_CITY_X = "x";
	public static final String RANGE_CITY_Y = "y";
	public static final String RANGE_CITY_RADIUS = "radius";
	public static final String RANGE_CITY_SAVEMAP = "saveMap";

	// Range Road Command
	public static final String RANGE_ROAD_CMD = "rangeRoads";
	public static final String RANGE_ROAD_X = "x";
	public static final String RANGE_ROAD_Y = "y";
	public static final String RANGE_ROAD_RADIUS = "radius";
	public static final String RANGE_ROAD_SAVEMAP = "saveMap";

	// Nearest City Command
	public static final String NEAREST_CITY_CMD = "nearestCity";
	public static final String NEAREST_CITY_X = "x";
	public static final String NEAREST_CITY_Y = "y";

	// Nearest Isolated City Command
	public static final String NEAREST_ISOLATED_CITY_CMD = "nearestIsolatedCity";
	public static final String NEAREST_ISOLATED_CITY_X = "x";
	public static final String NEAREST_ISOLATED_CITY_Y = "y";

	// Nearest Road Command
	public static final String NEAREST_ROAD_CMD = "nearestRoad";
	public static final String NEAREST_ROAD_X = "x";
	public static final String NEAREST_ROAD_Y = "y";

	// Nearest City to Road Command
	public static final String NEAREST_CITY_TO_ROAD_CMD = "nearestCityToRoad";
	public static final String NEAREST_CITY_TO_ROAD_START = "start";
	public static final String NEAREST_CITY_TO_ROAD_END = "end";

	// Shortest Path Command
	public static final String SHORTEST_PATH_CMD = "shortestPath";
	public static final String SHORTEST_PATH_START = "start";
	public static final String SHORTEST_PATH_END = "end";
	public static final String SHORTEST_PATH_SAVE_MAP = "saveMap";
	public static final String SHORTEST_PATH_SAVE_HTML = "saveHTML";
	
	public static final String GLOBAL_RANGE_CITIES = "globalRangeCities";
	
	public static final String MAP_AIRPORT = "mapAirport";
	public static final String AIRPORT_NAME = "airportName";
	public static final String TERMINAL_NAME = "terminalName";
	public static final String TERMINAL_X = "terminalX";
	public static final String TERMINAL_Y = "terminalY";
	public static final String TERMINAL_CITY = "terminalCity";
	public static final String MAP_TERMINAL = "mapTerminal";
	public static final String UNMAP_ROAD = "unmapRoad";
	public static final String UNMAP_AIRPORT = "unmapAirport";
	public static final String UNMAP_TERMINAL = "unmapTerminal";
	public static final String CITY_NAME = "cityName";
	public static final String MST = "mst";
	public static final String UNMAP_ROAD_CMD = "unmapRoad";

	// Output tag
	public static final String SUCCESS_TAG = "success";
	public static final String ERROR_TAG = "error";
	public static final String VALUE_TAG = "value";
	public static final String PARAMETERS_TAG = "parameters";
	public static final String OUTPUT_TAG = "output";
	public static final String COMMAND_TAG = "command";
	public static final String COMMAND_ID_TAG = "id";
	public static final String COMMAND_NAME_TAG = "name";
	public static final String FATAL_ERROR_TAG = "fatalError";
	public static final String ERROR_TYPE_TAG = "type";
	public static final String CITY_LIST_TAG = "cityList";
	public static final String ROAD_LIST_TAG = "roadList";
	public static final String CITY_TAG = "city";
	public static final String CITY_UNMAPPED_TAG = "cityUnmapped";
	public static final String QUAD_TREE = "quadtree";
	public static final String ROAD_CREATED_TAG = "roadCreated";
	public static final String PATH_TAG = "path";
	public static final String PATH_LENGTH_TAG = "length";
	public static final String PATH_HOPS_TAG = "hops";
	public static final String LEFT_TAG = "left";
	public static final String RIGHT_TAG = "right";
	public static final String STRAIGHT_TAG = "straight";
	public static final String AVLGTREE_TAG = "AvlGTree";
	public static final String HEIGHT_TAG = "height";
	public static final String MAX_IMBALANCE = "maxImbalance";
	public static final String KEY_TAG = "key";
	public static final String EMPTY_CHILD_TAG = "emptyChild";
	public static final String NODE_TAG = "node";
	public static final String ORDER_TAG = "order";
	public static final String TERMINAL_TAG = "terminal";
	public static final String AIRPORT_TAG = "airport";
	public static final String ROAD_UNMAPPED_TAG = "roadUnmapped";
	public static final String ROAD_DELETED_TAG = "roadDeleted";
	public static final String TERMINAL_UNMAPPED_TAG = "terminalUnmapped";
	public static final String AIRPORT_UNMAPPED_TAG = "airportUnmapped";
	public static final String DISTANCE_SPANNED = "distanceSpanned";

	// Error tag
	public static final String DUPLICATE_CITY_COORDINATES = "duplicateCityCoordinates";
	public static final String DUPLICATE_CITY_NAME = "duplicateCityName";
	public static final String NO_CITIES_TO_LIST = "noCitiesToList";
	public static final String NAME_NOT_IN_DICTIONARY = "nameNotInDictionary";
	public static final String CITY_ALREADY_MAPPED = "cityAlreadyMapped";
	public static final String CITY_OUTOF_BOUNDS = "cityOutOfBounds";
	public static final String CITY_NOT_MAPPED = "cityNotMapped";
	public static final String MAP_IS_EMPTY = "mapIsEmpty";
	public static final String CITY_DOES_NOT_EXIST = "cityDoesNotExist";
	public static final String NO_CITIES_EXIST_IN_RANGE = "noCitiesExistInRange";
	public static final String NO_ROADS_EXIST_IN_RANGE = "noRoadsExistInRange";
	public static final String START_POINT_DOES_NOT_EXIST = "startPointDoesNotExist";
	public static final String END_POINT_DOES_NOT_EXIST = "endPointDoesNotExist";
	public static final String START_EQUALS_END = "startEqualsEnd";
	public static final String START_OR_END_IS_ISOLATED = "startOrEndIsIsolated";
	public static final String ROAD_ALREADY_MAPPED = "roadAlreadyMapped";
	public static final String ROAD_OUT_OF_BOUNDS = "roadOutOfBounds";
	public static final String CITY_NOT_FOUND = "cityNotFound";
	public static final String ROAD_NOT_FOUND = "roadNotFound";
	public static final String ROAD_IS_NOT_MAPPED = "roadIsNotMapped";
	public static final String NO_OTHER_CITIES_MAPPED = "noOtherCitiesMapped";
	public static final String NON_EXISTENT_START = "nonExistentStart";
	public static final String NON_EXISTENT_END = "nonExistentEnd";
	public static final String NO_PATH_EXISTS = "noPathExists";
	public static final String EMPTY_TREE = "emptyTree";
	public static final String ROAD_NOT_IN_ONE_METROPOLE = "roadNotInOneMetropole";
	public static final String DUPLICATE_AIRPORT_NAME = "duplicateAirportName";
	public static final String DUPLICATE_AIRPORT_COORDINATES = "duplicateAirportCoordinates";
	public static final String AIRPORT_OUT_OF_BOUNDS = "airportOutOfBounds";
	public static final String DUPLICATE_TERMINAL_NAME = "duplicateTerminalName";
	public static final String DUPLICATE_TERMINAL_COORDINATES = "duplicateTerminalCoordinates";
	public static final String TERMINAL_OUT_OF_BOUNDS = "terminalOutOfBounds";
	public static final String CONNECTING_CITY_DOES_NOT_EXIST = "connectingCityDoesNotExist";
	public static final String CONNECTING_CITY_NOT_IN_SAME_METROPOLE = "connectingCityNotInSameMetropole";
	public static final String ROAD_VIOLATES_PMRULES = "roadViolatesPMRules";
	public static final String AIRPORT_VIOLATES_PMRULES = "airportViolatesPMRules";
	public static final String CONNECTING_CITY_NOT_MAPPED = "connectingCityNotMapped";
	public static final String TERMINAL_VIOLATES_PMRULES = "terminalViolatesPMRules";
	public static final String ROAD_INTERSECTS_ANOTHER_ROAD = "roadIntersectsAnotherRoad";
	public static final String AIRPORT_DOES_NOT_EXIST = "airportDoesNotExist";
	public static final String TERMINAL_DOES_NOT_EXIST = "terminalDoesNotExist";
	public static final String AIRPORT_NOT_IN_SAME_METROPOLE = "airportNotInSameMetropole";
	public static final String ROAD_NOT_MAPPED = "roadNotMapped";
	public static final String METROPOLE_OUT_OF_BOUNDS = "metropoleOutOfBounds";
	public static final String METROPOLE_IS_EMPTY = "metropoleIsEmpty";
}
