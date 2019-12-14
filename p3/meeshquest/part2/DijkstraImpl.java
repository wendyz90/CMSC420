package cmsc420.meeshquest.part3;

import java.util.PriorityQueue;
import java.util.TreeMap;

import cmsc420.meeshquest.primitive.City;
import cmsc420.meeshquest.primitive.CityNameComparator;
import cmsc420.meeshquest.primitive.Path;
import cmsc420.meeshquest.primitive.Road;
import cmsc420.meeshquest.primitive.RoadAdjacencyList;

public class DijkstraImpl {

	protected RoadAdjacencyList roads;

	public DijkstraImpl(final RoadAdjacencyList roads) {
		this.roads = roads;
	}

	public Path getShortestPath(final City startCity, final City endCity) {
		final String endCityName = endCity.getName();

		final TreeMap<String, Double> settledCities = new TreeMap<String, Double>();

		final PriorityQueue<DijkstraCity> unsettledCities;

		if (roads.getNumberOfCities() > 0) {

			unsettledCities = new PriorityQueue<DijkstraCity>(roads
					.getNumberOfCities());
		} else {

			unsettledCities = new PriorityQueue<DijkstraCity>();
		}

		final TreeMap<City, City> previousCity = new TreeMap<City, City>(
				new CityNameComparator());

		final TreeMap<City, Double> shortestDistanceFound = new TreeMap<City, Double>(
				new CityNameComparator());

		boolean pathFound = false;

		for (City c : roads.getCitySet()) {
			shortestDistanceFound.put(c, Double.POSITIVE_INFINITY);
		}

		unsettledCities.add(new DijkstraCity(startCity, 0.0d));
		shortestDistanceFound.put(startCity, 0.0d);
		previousCity.put(startCity, null);

		while (!unsettledCities.isEmpty()) {

			final DijkstraCity cityToSettle = unsettledCities.poll();
			final String cityToSettleName = cityToSettle.getName();
			if (cityToSettleName.equals(endCityName)) {
				pathFound = true;
				break;
			}
			if (!settledCities.containsKey(cityToSettleName)) {
				settledCities.put(cityToSettleName, cityToSettle.getDistance());
				for (Road road : roads.getRoadSet(cityToSettle.getCity())) {
					/* get the adjacent city */
					final City adjacentCity = road
							.getOther(cityToSettleName);
					final String adjacentCityName = adjacentCity.getName();

					if (!settledCities.containsKey(adjacentCityName)) {
						final double adjacentCityDistance = shortestDistanceFound
								.get(adjacentCity);
						final double distanceViaCityToSettle = shortestDistanceFound
								.get(cityToSettle.getCity())
								+ road.getDistance();

						if (adjacentCityDistance > distanceViaCityToSettle) {
							shortestDistanceFound.put(adjacentCity,
									distanceViaCityToSettle);
							previousCity.put(adjacentCity, cityToSettle
									.getCity());
							unsettledCities.offer(new DijkstraCity(
									adjacentCity, distanceViaCityToSettle));
						}
					}
				}
			}
		}

		if (pathFound) {
			final Path path = new Path(shortestDistanceFound.get(endCity));

			City curr = endCity;
			while (curr != null) {
				path.addEdge(curr);
				curr = previousCity.get(curr);
			}

			return path;
		} else {
			return null;
		}
	}

	protected class DijkstraCity implements Comparable<DijkstraCity> {
		protected City city;
		protected double distance;

		public DijkstraCity(final City city, final double distance) {
			this.city = city;
			this.distance = distance;
		}

		public City getCity() {
			return city;
		}

		public String getName() {
			return city.getName();
		}

		public double getDistance() {
			return distance;
		}

		public int compareTo(final DijkstraCity other) {
			if (getDistance() < other.getDistance()) {
				return -1;
			} else if (getDistance() > other.getDistance()) {
				return 1;
			} else {
				return city.getName().compareTo(other.city.getName());
			}
		}
	}
}
