package cmsc420.meeshquest.primitive;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cmsc420.meeshquest.pmquadtree.PM1Validator;
import cmsc420.meeshquest.pmquadtree.PM3Validator;
import cmsc420.meeshquest.pmquadtree.PMQuadTree;
import cmsc420.meeshquest.pmquadtree.Validator;

public class Metropole extends Point2D.Float {
	public Map<String, City> cityMap;
	public Set<City> citySet;
	public Set<GeomPoint> geomPointSet;
	public PMQuadTree quadTree;
	
	public Metropole(float x, float y, int localSpatialWidth, int localSpacialHeight, int pmOrder) {
		cityMap = new TreeMap<>(new NameComparator());
		citySet = new TreeSet<>(new PointComparator());
		this.x = x;
		this.y = y;
		Validator v;
		if (pmOrder == 1) {
			v = new PM1Validator();
		} else {
			v = new PM3Validator();
		}
		quadTree = new PMQuadTree(v, localSpatialWidth, localSpacialHeight, pmOrder);
		geomPointSet = new TreeSet<>(new PointComparator());
	}
}
