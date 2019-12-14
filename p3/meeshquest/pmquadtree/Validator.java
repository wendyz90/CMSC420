package cmsc420.meeshquest.pmquadtree;

import cmsc420.meeshquest.pmquadtree.PMQuadTree.Black;
import cmsc420.meeshquest.pmquadtree.PMQuadTree.White;

public interface Validator {

	public boolean ifValid(final Black b);

	public boolean ifValid(final White w);

}
