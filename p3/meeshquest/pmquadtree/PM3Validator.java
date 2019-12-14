package cmsc420.meeshquest.pmquadtree;

import cmsc420.meeshquest.pmquadtree.PMQuadTree.Black;
import cmsc420.meeshquest.pmquadtree.PMQuadTree.White;

public final class PM3Validator implements Validator {

	@Override
	public boolean ifValid(Black b) {
		return b.getCityList().size() + b.getIsolatedCityList().size() <= 1;
	}

	@Override
	public boolean ifValid(White w) {
		return true;
	}

}
