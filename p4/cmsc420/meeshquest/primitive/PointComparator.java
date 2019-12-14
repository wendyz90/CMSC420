package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.Comparator;

public class PointComparator implements Comparator<Point2D.Float> {

	@Override
	public int compare(Float o1, Float o2) {
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
