package cmsc420.meeshquest.primitive;

import java.util.Comparator;

public class CityNameComparator implements Comparator<City> {

	@Override
	public int compare(City c1, City c2) {
		return c2.getName().compareTo(c1.getName());
	}

}
