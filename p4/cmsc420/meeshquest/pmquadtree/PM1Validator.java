package cmsc420.meeshquest.pmquadtree;

import cmsc420.meeshquest.pmquadtree.PMQuadTree.Black;
import cmsc420.meeshquest.pmquadtree.PMQuadTree.White;
import cmsc420.meeshquest.primitive.GeomPoint;
import cmsc420.meeshquest.primitive.Road;

public class PM1Validator implements Validator {

	@Override
	public boolean ifValid(Black b) {
		int num = b.getGeomList().size() + b.getAirportList().size();
		if (num > 1) {
			return false;
		} else if (num == 0) {
			return b.getRoadList().size() == 1;
		} else {
			if (b.getAirportList().size() == 1) {
				return b.getRoadList().size() == 0;
			} else {
				GeomPoint gp = b.getGeomPoint();
				for (Road r : b.getRoadList()) {
					if (!r.getStart().equals(gp) && !r.getEnd().equals(gp)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean ifValid(White w) {
		return true;
	}

}
