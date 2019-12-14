package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class PrQuadTree<T extends Point2D.Float> {

	private static final String WHITE_TAG = "white";
	private static final String GRAY_TAG = "gray";
	private static final String BLACK_TAG = "black";
	private static final String NAME_TAG = "name";
	private static final String X_TAG = "x";
	private static final String Y_TAG = "y";

	private class PrQuadNode {
		public double xHi;
		public double xLo;
		public double yHi;
		public double yLo;
		public PrQuadNode parent;

		public PrQuadNode(double xHi, double xLo, double yHi, double yLo, PrQuadNode p) {
			this.xHi = xHi;
			this.xLo = xLo;
			this.yHi = yHi;
			this.yLo = yLo;
			this.parent = p;
		}
	}

	private class PrQuadLeaf extends PrQuadNode {
		T ele;
		String name;

		public PrQuadLeaf(T ele, double xHi, double xLo, double yHi, double yLo, PrQuadNode p,
				String name) {
			super(xHi, xLo, yHi, yLo, p);
			this.ele = ele;
			this.name = name;
		}
	}

	private class PrQuadInternal extends PrQuadNode {
		PrQuadNode NE, NW, SE, SW;
		int numNodes;

		public PrQuadInternal(double xHi, double xLo, double yHi, double yLo, PrQuadNode p) {
			super(xHi, xLo, yHi, yLo, p);
			this.NE = null;
			this.NW = null;
			this.SE = null;
			this.SW = null;
			this.numNodes = 0;
		}
	}

	private class PriorityQueueElement {
		public double distance;
		public PrQuadNode node;
		public String name;

		public PriorityQueueElement(double d, PrQuadNode node, String n) {
			this.distance = d;
			this.node = node;
			this.name = n;
		}
	}

	private class PriorityQueueComparator implements Comparator<PriorityQueueElement> {

		@Override
		public int compare(PrQuadTree<T>.PriorityQueueElement o1,
				PrQuadTree<T>.PriorityQueueElement o2) {
			if (o1.distance < o2.distance) {
				return -1;
			} else if (o1.distance > o2.distance) {
				return 1;
			} else {
				if (o1.name == null && o2.name == null) {
					return 0;
				} else if (o1.name == null) {
					return -1;
				} else if (o2.name == null) {
					return 1;
				}
				return o2.name.compareTo(o1.name);
			}
		}

	}

	
	//开始写QUADTREE，前面是铺垫 等于LIU的QUADTREE
	private PrQuadNode root;
	private int minX, minY, maxX, maxY;
	private Map<String, PrQuadNode> nodeMap;

	public PrQuadTree(int maxX, int minX, int maxY, int minY) {
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
		root = null;
		nodeMap = new HashMap<>();
	}

	public boolean insert(T ele, String name) {
		PrQuadNode ret = insertHelper(root, ele, maxX, minX, maxY, minY, null, 0, name);
		if (ret == null) {
			return false;
		}
		return true;
	}

	private PrQuadNode insertHelper(PrQuadNode sRoot, T ele, double xHi, double xLo, double yHi,
			double yLo, PrQuadNode p, int dir, String name) {
				
		PrQuadNode ret = null;
		if (sRoot == null) {
			ret = new PrQuadLeaf(ele, xHi, xLo, yHi, yLo, p, name);
			nodeMap.put(name, ret);
			if (p == null) {
				root = ret;
				return ret;
			}
			PrQuadInternal pNode = (PrQuadInternal) p;
			switch (dir) {
			case 1:
				pNode.NW = ret;
				break;
			case 2:
				pNode.NE = ret;
				break;
			case 3:
				pNode.SW = ret;
				break;
			case 4:
				pNode.SE = ret;
				break;
			}
			pNode.numNodes++;
			return ret;
		}
		
		
		
		if (sRoot instanceof PrQuadTree.PrQuadLeaf) {
			PrQuadLeaf node = (PrQuadLeaf) sRoot;
			PrQuadInternal newNode = new PrQuadInternal(node.xHi, node.xLo, node.yHi, node.yLo,
					node.parent);
			if (node.parent == null) {
				root = newNode;
			} else {
				PrQuadInternal pNode = (PrQuadInternal) node.parent;
				switch (dir) {
				case 1:
					pNode.NW = newNode;
					break;
				case 2:
					pNode.NE = newNode;
					break;
				case 3:
					pNode.SW = newNode;
					break;
				case 4:
					pNode.SE = newNode;
					break;
				}
			}
			splitAndInsert(newNode, node.ele, node.name);
			ret = splitAndInsert(newNode, ele, name);
			// node.parent = null;
		} else if (sRoot instanceof PrQuadTree.PrQuadInternal) {
			PrQuadInternal node = (PrQuadInternal) sRoot;
			ret = splitAndInsert(node, ele, name);
		}
		return ret;
	}

	private PrQuadNode splitAndInsert(PrQuadInternal node, T ele, String name) {
		PrQuadNode ret = null;
		double xCenter = (node.xHi + node.xLo) / 2;
		double yCenter = (node.yHi + node.yLo) / 2;
		int direction = getDirection(ele, node.xHi, node.xLo, node.yHi, node.yLo);
		switch (direction) {
		case 1:
			ret = insertHelper(node.NW, ele, xCenter, node.xLo, node.yHi, yCenter, node, 1, name);
			break;
		case 2:
			ret = insertHelper(node.NE, ele, node.xHi, xCenter, node.yHi, yCenter, node, 2, name);
			break;
		case 3:
			ret = insertHelper(node.SW, ele, xCenter, node.xLo, yCenter, node.yLo, node, 3, name);
			break;
		case 4:
			ret = insertHelper(node.SE, ele, node.xHi, xCenter, yCenter, node.yLo, node, 4, name);
			break;
		}
		return ret;
	}
	
	
	

	public boolean remove(String cityName) {
		removeHelper(findElement(cityName));
		nodeMap.remove(cityName);
		return true;
	}

	private void removeHelper(PrQuadNode node) {
		if (node == null) {
			return;
		}
		if (node instanceof PrQuadTree.PrQuadInternal) {
			PrQuadInternal n = (PrQuadInternal) node;
			if (n.numNodes >= 2) {
				return;
			}
			PrQuadNode child = null;
			if (n.numNodes == 1) {
				// There should be only one non-null child
				if (n.SE != null) {
					child = n.SE;
				} else if (n.SW != null) {
					child = n.SW;
				} else if (n.NE != null) {
					child = n.NE;
				} else if (n.NW != null) {
					child = n.NW;
				}
			}
			if (child == null || child instanceof PrQuadTree.PrQuadLeaf) {
				if (node.parent == null) {
					root = child;
					if (child != null) {
						child.parent = null;
						child.xHi = maxX;
						child.xLo = minX;
						child.yHi = maxY;
						child.yLo = minY;
					}
				} else {
					PrQuadInternal parent = (PrQuadInternal) node.parent;
					if (parent.SE == n) {
						parent.SE = child;
						if (child != null) {
							child.xHi = parent.xHi;
							child.xLo = (parent.xHi + parent.xLo) / 2;
							child.yHi = (parent.yHi + parent.yLo) / 2;
							child.yLo = parent.yLo;
						}
					} else if (parent.SW == n) {
						parent.SW = child;
						if (child != null) {
							child.xHi = (parent.xHi + parent.xLo) / 2;
							child.xLo = parent.xLo;
							child.yHi = (parent.yHi + parent.yLo) / 2;
							child.yLo = parent.yLo;
						}
					} else if (parent.NE == n) {
						parent.NE = child;
						if (child != null) {
							child.xHi = parent.xHi;
							child.xLo = (parent.xHi + parent.xLo) / 2;
							child.yHi = parent.yHi;
							child.yLo = (parent.yHi + parent.yLo) / 2;
						}
					} else if (parent.NW == n) {
						parent.NW = child;
						if (child != null) {
							child.xHi = (parent.xHi + parent.xLo) / 2;
							child.xLo = parent.xLo;
							child.yHi = parent.yHi;
							child.yLo = (parent.yHi + parent.yLo) / 2;
						}
					}
					if (child != null) {
						child.parent = parent;
					}
					if (child == null) {
						parent.numNodes--;
					}
				}
			}
		} else if (node instanceof PrQuadTree.PrQuadLeaf) {
			if (node.parent == null) {
				root = null;
			} else {
				PrQuadInternal parent = (PrQuadInternal) node.parent;
				if (parent.SE == node) {
					parent.SE = null;
				} else if (parent.SW == node) {
					parent.SW = null;
				} else if (parent.NE == node) {
					parent.NE = null;
				} else if (parent.NW == node) {
					parent.NW = null;
				}
				parent.numNodes--;
			}
		}
		// node.parent = null;
		removeHelper(node.parent);
	}


	
	
	public void drawCircle(CanvasPlus canvas, Integer x, Integer y, Integer radius) {
		canvas.addCircle(x, y, radius, Color.BLUE, false);
	}

	public List<T> rangeSearch(Integer x, Integer y, Integer radius) {
		List<T> result = new ArrayList<>();
		rangeSearchHelper(root, x, y, radius, result);
		return result;
	}

	public T nearestPoint(Integer x, Integer y) {
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(0, root, null));
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (pqe.node instanceof PrQuadTree.PrQuadLeaf) {
				PrQuadLeaf node = (PrQuadLeaf) pqe.node;
				return node.ele;
			} else if (pqe.node instanceof PrQuadTree.PrQuadInternal) {
				PrQuadInternal node = (PrQuadInternal) pqe.node;
				if (node.NW != null) {
					double distance = calculateNodeDistance(node.NW, x, y);
					if (node.NW instanceof PrQuadTree.PrQuadLeaf) {
						PrQuadLeaf n = (PrQuadLeaf) node.NW;
						pq.add(new PriorityQueueElement(distance, node.NW, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.NW, null));
					}
				}
				if (node.NE != null) {
					double distance = calculateNodeDistance(node.NE, x, y);
					if (node.NE instanceof PrQuadTree.PrQuadLeaf) {
						PrQuadLeaf n = (PrQuadLeaf) node.NE;
						pq.add(new PriorityQueueElement(distance, node.NE, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.NE, null));
					}
				}
				if (node.SW != null) {
					double distance = calculateNodeDistance(node.SW, x, y);
					if (node.SW instanceof PrQuadTree.PrQuadLeaf) {
						PrQuadLeaf n = (PrQuadLeaf) node.SW;
						pq.add(new PriorityQueueElement(distance, node.SW, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.SW, null));
					}
				}
				if (node.SE != null) {
					double distance = calculateNodeDistance(node.SE, x, y);
					if (node.SE instanceof PrQuadTree.PrQuadLeaf) {
						PrQuadLeaf n = (PrQuadLeaf) node.SE;
						pq.add(new PriorityQueueElement(distance, node.SE, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.SE, null));
					}
				}
			}
		}
		return null;
	}

	private double calculateNodeDistance(PrQuadNode node, Integer x, Integer y) {
		if (node instanceof PrQuadTree.PrQuadLeaf) {
			PrQuadLeaf n = (PrQuadLeaf) node;
			return euclideanDistance(n.ele.getX(), n.ele.getY(), x, y);
		} else if (node instanceof PrQuadTree.PrQuadInternal) {
			if (x < node.xLo && y >= node.yHi) {
				return euclideanDistance(node.xLo, node.yHi, x, y);
			} else if (x >= node.xLo && x < node.xHi && y > node.yHi) {
				return y - node.yHi;
			} else if (x >= node.xHi && y >= node.yHi) {
				return euclideanDistance(node.xHi, node.yHi, x, y);
			} else if (x < node.xLo && y >= node.yLo && y < node.yHi) {
				return node.xLo - x;
			} else if (x >= node.xLo && x < node.xHi && y >= node.yLo && y < node.yHi) {
				return 0;
			} else if (x > node.xHi && y >= node.yLo && y < node.yHi) {
				return x - node.xHi;
			} else if (x < node.xLo && y < node.yLo) {
				return euclideanDistance(node.xLo, node.yLo, x, y);
			} else if (x >= node.xLo && x < node.xHi && y < node.yLo) {
				return node.yLo - y;
			} else if (x >= node.xHi && y < node.yLo) {
				return euclideanDistance(node.xHi, node.yLo, x, y);
			}
		}
		return 0;
	}

	private double euclideanDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public void print(Element parent, Document doc) {
		printTreeHelper(root, parent, doc);
	}

	private void printTreeHelper(PrQuadNode sRoot, Element parent, Document doc) {
		if (sRoot == null) {
			Element whiteElt = doc.createElement(WHITE_TAG);
			parent.appendChild(whiteElt);
			return;
		}
		if (sRoot instanceof PrQuadTree.PrQuadLeaf) {
			PrQuadLeaf node = (PrQuadLeaf) sRoot;
			Element blackElt = doc.createElement(BLACK_TAG);
			blackElt.setAttribute(NAME_TAG, node.name);
			blackElt.setAttribute(X_TAG, String.valueOf(Math.round(node.ele.getX())));
			blackElt.setAttribute(Y_TAG, String.valueOf(Math.round(node.ele.getY())));
			parent.appendChild(blackElt);
		} else if (sRoot instanceof PrQuadTree.PrQuadInternal) {
			PrQuadInternal node = (PrQuadInternal) sRoot;
			Element grayElt = doc.createElement(GRAY_TAG);
			grayElt.setAttribute(X_TAG, String.valueOf(Math.round((node.xHi + node.xLo) / 2)));
			grayElt.setAttribute(Y_TAG, String.valueOf(Math.round((node.yHi + node.yLo) / 2)));
			parent.appendChild(grayElt);
			printTreeHelper(node.NW, grayElt, doc);
			printTreeHelper(node.NE, grayElt, doc);
			printTreeHelper(node.SW, grayElt, doc);
			printTreeHelper(node.SE, grayElt, doc);
		}
	}
	
	
		public void drawMap(CanvasPlus canvas) {
		drawTreeHelper(root, canvas);
	}


	private void drawTreeHelper(PrQuadNode sRoot, CanvasPlus canvas) {
		if (sRoot == null) {
			return;
		}
		if (sRoot instanceof PrQuadTree.PrQuadLeaf) {
			PrQuadLeaf node = (PrQuadLeaf) sRoot;
			canvas.addPoint(node.name, node.ele.getX(), node.ele.getY(), Color.BLACK);
		} else if (sRoot instanceof PrQuadTree.PrQuadInternal) {
			PrQuadInternal node = (PrQuadInternal) sRoot;
			double yMiddle = (node.yHi + node.yLo) / 2;
			double xMiddle = (node.xHi + node.xLo) / 2;
			canvas.addLine(node.xLo, yMiddle, node.xHi, yMiddle, Color.BLACK);
			canvas.addLine(xMiddle, node.yLo, xMiddle, node.yHi, Color.BLACK);
			drawTreeHelper(node.NW, canvas);
			drawTreeHelper(node.NE, canvas);
			drawTreeHelper(node.SW, canvas);
			drawTreeHelper(node.SE, canvas);
		}
	}

	private PrQuadNode findElement(String cityName) {
		return nodeMap.get(cityName);
	}

	protected Map<String, PrQuadNode> getNodeMap() {
		return nodeMap;
	}

	// 1 NW
	// 2 NE
	// 3 SW
	// 4 SE
	private int getDirection(T ele, double xHi, double xLo, double yHi, double yLo) {
		int ret = 0;
		double xCenter = (xHi + xLo) / 2;
		double yCenter = (yHi + yLo) / 2;
		if (ele.getX() >= xCenter && ele.getY() >= yCenter) {
			ret = 2;
		} else if (ele.getX() < xCenter && ele.getY() >= yCenter) {
			ret = 1;
		} else if (ele.getX() < xCenter && ele.getY() < yCenter) {
			ret = 3;
		} else if (ele.getX() >= xCenter && ele.getY() < yCenter) {
			ret = 4;
		}
		return ret;
	}

	private Boolean inCircleRange(double x, double y, Integer centerX, Integer centerY,
			Integer radius) {
		double distance = euclideanDistance(x, y, centerX, centerY);
		if (distance <= radius) {
			return true;
		}
		return false;
	}

	private Boolean inRectRange(double xHi, double yHi, double xLo, double yLo, Integer x,
			Integer y) {
		if (x <= xHi && x >= xLo && y <= yHi && y >= yLo) {
			return true;
		}
		return false;
	}

	private Boolean nodeInRange(PrQuadNode node, Integer centerX, Integer centerY, Integer radius) {
		if (inCircleRange(node.xHi, node.yHi, centerX, centerY, radius)) {
			return true;
		}
		if (inCircleRange(node.xHi, node.yLo, centerX, centerY, radius)) {
			return true;
		}
		if (inCircleRange(node.xLo, node.yHi, centerX, centerY, radius)) {
			return true;
		}
		if (inCircleRange(node.xLo, node.yLo, centerX, centerY, radius)) {
			return true;
		}
		if (inRectRange(node.xHi, node.yHi, node.xLo, node.yLo, centerX - radius, centerY)) {
			return true;
		}
		if (inRectRange(node.xHi, node.yHi, node.xLo, node.yLo, centerX + radius, centerY)) {
			return true;
		}
		if (inRectRange(node.xHi, node.yHi, node.xLo, node.yLo, centerX, centerY + radius)) {
			return true;
		}
		if (inRectRange(node.xHi, node.yHi, node.xLo, node.yLo, centerX, centerY - radius)) {
			return true;
		}
		return false;
	}

	private void rangeSearchHelper(PrQuadNode sRoot, Integer centerX, Integer centerY,
			Integer radius, List<T> result) {
		// Node cannot be null here
		if (sRoot instanceof PrQuadTree.PrQuadLeaf) {
			PrQuadLeaf node = (PrQuadLeaf) sRoot;
			if (inCircleRange(node.ele.getX(), node.ele.getY(), centerX, centerY, radius)) {
				result.add(node.ele);
			}
		} else if (sRoot instanceof PrQuadTree.PrQuadInternal) {
			PrQuadInternal node = (PrQuadInternal) sRoot;
			if (node.NW != null && nodeInRange(node.NW, centerX, centerY, radius)) {
				rangeSearchHelper(node.NW, centerX, centerY, radius, result);
			}
			if (node.NE != null && nodeInRange(node.NE, centerX, centerY, radius)) {
				rangeSearchHelper(node.NE, centerX, centerY, radius, result);
			}
			if (node.SW != null && nodeInRange(node.SW, centerX, centerY, radius)) {
				rangeSearchHelper(node.SW, centerX, centerY, radius, result);
			}
			if (node.SE != null && nodeInRange(node.SE, centerX, centerY, radius)) {
				rangeSearchHelper(node.SE, centerX, centerY, radius, result);
			}
		}
	}

}
