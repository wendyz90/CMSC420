package cmsc420.meeshquest.part4;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.meeshquest.primitive.Airport;
import cmsc420.meeshquest.primitive.City;
import cmsc420.meeshquest.primitive.GeomNameComparator;
import cmsc420.meeshquest.primitive.GeomPoint;
import cmsc420.meeshquest.primitive.Metropole;
import cmsc420.meeshquest.primitive.NameComparator;
import cmsc420.meeshquest.primitive.Road;
import cmsc420.meeshquest.primitive.RoadAdjacencyList;
import cmsc420.meeshquest.primitive.Terminal;
import static cmsc420.meeshquest.primitive.Naming.*;

public class MSTSolver {
	
	private class PriorityQueueElement {
		String name;
		double distance;
		String from;

		public PriorityQueueElement(String n, double d, String from) {
			this.name = n;
			this.distance = d;
			this.from = from;
		}
	}

	private class PriorityQueueComparator implements Comparator<PriorityQueueElement> {

		@Override
		public int compare(PriorityQueueElement o1, PriorityQueueElement o2) {
			if (o1.distance < o2.distance) {
				return -1;
			} else if (o1.distance > o2.distance) {
				return 1;
			} else {
				if (o1.name.equals(o2.name)) {
					return o2.from.compareTo(o1.from);
				} else {
					return o2.name.compareTo(o1.name);
				}
			}
		}
	}
	
	private PriorityQueue<PriorityQueueElement> pq;
	private Map<String, TreeSet<String>> ret;
	private double total;
	private Set<String> printed;
	private Set<String> found;
	
	public MSTSolver() {
		pq = new PriorityQueue<>(11, new PriorityQueueComparator());
		ret = new TreeMap<>(new NameComparator());
		total = 0;
		printed = new TreeSet<>(new NameComparator());
		found = new TreeSet<>(new NameComparator());
	}
	
	public void solveMST(Map<String, Metropole> geomNameMap, RoadAdjacencyList roadAj, City start) {
		for (Map.Entry<String, Metropole> e : geomNameMap.entrySet()) {
			if (e.getKey().equals(start.getName())) {
				pq.add(new PriorityQueueElement(e.getKey(), 0, null));
			} else {
				pq.add(new PriorityQueueElement(e.getKey(), Double.MAX_VALUE, null));
			}
		}
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (found.contains(pqe.name)) {
				continue;
			}
			if (pqe.distance == Double.MAX_VALUE) {
				continue;
			}
			total += pqe.distance;
			found.add(pqe.name);
			Map<String, Double> adjMap = roadAj.getRoadSet(pqe.name);
			for (Map.Entry<String, Double> e : adjMap.entrySet()) {
				if (found.contains(e.getKey())) {
					continue;
				}
				pq.add(new PriorityQueueElement(e.getKey(), e.getValue(), pqe.name));
			}
			if (pqe.from != null) {
				if (!ret.containsKey(pqe.from)) {
					ret.put(pqe.from, new TreeSet<String>(new NameComparator()));
				}
				Set<String> adj = ret.get(pqe.from);
				adj.add(pqe.name);
			}
		}
	}
	
	public void printXML(String start, Document wholeDoc, Element parent) {
		if (printed.contains(start)) {
			return;
		}
		printed.add(start);
		Element nodeElt = wholeDoc.createElement(NODE_TAG);
		nodeElt.setAttribute(CREATE_CITY_NAME, start);
		parent.appendChild(nodeElt);
		if (!ret.containsKey(start)) {
			return;
		}
		for (String s : ret.get(start)) {
			printXML(s, wholeDoc, nodeElt);
		}
	}
	
	public double getTotal() {
		return total;
	}
}
