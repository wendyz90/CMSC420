package cmsc420.meeshquest.primitive;

import java.util.Comparator;

public class CityCoordinateComparator implements Comparator<City> {

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
