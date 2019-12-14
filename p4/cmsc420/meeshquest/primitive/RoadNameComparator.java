package cmsc420.meeshquest.primitive;

import java.util.Comparator;

public class RoadNameComparator implements Comparator<Road> {

	@Override
	public int compare(Road o1, Road o2) {
		if (o1.getStart().getName().compareTo(o2.getStart().getName()) < 0) {
			return 1;
		} else if (o1.getStart().getName().compareTo(o2.getStart().getName()) > 0) {
			return -1;
		} else {
			return o2.getEnd().getName().compareTo(o1.getEnd().getName());
		}
	}

}
