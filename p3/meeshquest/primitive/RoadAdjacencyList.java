package cmsc420.meeshquest.primitive;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class RoadAdjacencyList {

	/** map of city names to connected roads */
	final protected TreeMap<City, TreeSet<Road>> adjacencyList = new TreeMap<City, TreeSet<Road>>(
			new CityNameComparator());

	public Road addRoad(final City one, final City two) {
		return addRoad(new Road(one, two));
	}

	public Road addRoad(Road road) {
		final City start, end;

		start = road.getStart();
		end = road.getEnd();

		/* check if we need to flip start and end */
		if (start.getName().compareTo(end.getName()) > 0) {
			road = new Road(end, start);
		}

		addRoadForCity(road, start);
		addRoadForCity(road, end);

		return road;
	}

	public boolean containsRoad(final Road road) {
		TreeSet<Road> roadsForCity = adjacencyList.get(road.getStart());
		if (roadsForCity == null) {
			return false;
		} else {
			return roadsForCity.contains(road);
		}
	}

	protected void addRoadForCity(final Road road, final City city) {
		TreeSet<Road> roadsForCity = adjacencyList.get(city);

		if (roadsForCity == null) {
			roadsForCity = new TreeSet<Road>(new RoadNameComparator());
			adjacencyList.put(city, roadsForCity);
		}

		roadsForCity.add(road);
	}

	public Road removeRoad(final City one, final City two) {
		return removeRoad(new Road(one, two));
	}

	public Road removeRoad(Road road) {
		if (!containsRoad(road)) {
			return null;
		} else {
			final City start, end;

			start = road.getStart();
			end = road.getEnd();

			// check if we need to flip start and end
			if (start.getName().compareTo(end.getName()) > 0) {
				road = new Road(end, start);
			}

			removeRoadForCity(road, start);
			removeRoadForCity(road, end);

			return road;
		}
	}

	protected void removeRoadForCity(final Road road, final City city) {
		TreeSet<Road> roadsForCity = adjacencyList.get(city);

		if (roadsForCity != null) {
			roadsForCity.remove(road);
			if (roadsForCity.size() == 0) {
				adjacencyList.remove(city);
			}
		}
	}
	
	public void removeRoadsForCity(final City city) {
		TreeSet<Road> roadsForCity = adjacencyList.get(city);
		
		if (roadsForCity != null) {
			adjacencyList.remove(city);
			for (Road r : roadsForCity) {
				if (r.getStart().equals(city)) {
					removeRoadForCity(r, r.getEnd());
				} else if (r.getEnd().equals(city)) {
					removeRoadForCity(r, r.getStart());
				}
			}
		}
	}

	public TreeSet<Road> getRoadSet(final City city) {
		final TreeSet<Road> roadsForCity = adjacencyList.get(city);

		if (roadsForCity == null) {
			return new TreeSet<Road>(new RoadNameComparator());
		} else {
			return roadsForCity;
		}
	}

	public TreeSet<Road> deleteCity(final City city) {
		final String cityName = city.getName();
		TreeSet<Road> roadsForCity = adjacencyList.remove(city);

		if (roadsForCity == null){
			roadsForCity = new TreeSet<Road>();
		}
		
		for (Road road : roadsForCity) {
			// remove the road from the TreeSet of the City other than city
			final City otherCity = road.getOther(cityName);
			//adjacencyList.get(otherCity).remove(road);
			removeRoadForCity(road, otherCity);
		}

		return roadsForCity;
		
	}

	public int getNumberOfCities() {
		return adjacencyList.size();
	}

	public Set<City> getCitySet() {
		return adjacencyList.keySet();
	}

	public void clear() {
		adjacencyList.clear();
	}

	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (final City city : adjacencyList.keySet()) {
			final String cityName = city.getName();
			s.append(cityName);
			s.append(":\n");
			for (Road r : adjacencyList.get(city)) {
				s.append(r.getCityNameString());
				s.append("\n");
			}
			s.append("\n");
		}
		return s.toString();
	}
}

