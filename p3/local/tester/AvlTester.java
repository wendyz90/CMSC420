package cmsc420.local.tester;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import cmsc420.sortedmap.AvlGTree;

public class AvlTester {

	public static void main(String[] args) {
		SortedMap<String, Integer> avl = new AvlGTree<>(3);
		SortedMap<String, Integer> tree = new TreeMap<>();
		
		for (int i = 0; i < 26; i ++) {
			avl.put("Entry" + String.valueOf(i), i);
			tree.put("Entry" + String.valueOf(i), i);
		}
		Set<Map.Entry<String, Integer>> e1 = avl.entrySet();
		Set<Map.Entry<String, Integer>> e2 = tree.entrySet();
		avl.put("Hello", 1);
		tree.put("Hello", 1);
		System.out.println(e1.toString());
		Iterator<Map.Entry<String, Integer>> it1 = e1.iterator();
		Iterator<Map.Entry<String, Integer>> it2 = e2.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			Map.Entry<String, Integer> me1 = it1.next();
			Map.Entry<String, Integer> me2 = it2.next();
			if (!me1.equals(me2)) {
				System.out.println("No");
			} else {
				System.out.println("Yes");
			}
		}
		if (it1.hasNext() || it2.hasNext()) {
			System.out.println("No");
		}
		if (avl.equals(tree) && tree.equals(avl)) {
			System.out.println("Yes");
		} else {
			System.out.println("No");
		}
		if (e1.equals(e2) && e2.equals(e1)) {
			System.out.println("Yes");
		} else {
			System.out.println("No");
		}
		if (e1.hashCode() == e2.hashCode()) {
			System.out.println("Yes");
		} else {
			System.out.println("No");
		}
		if (e1.toString().equals(e2.toString())) {
			System.out.println("Yes");
		} else {
			System.out.println("No");
		}
		System.out.println(avl.toString());
		System.out.println(tree.toString());
		System.out.println(e1.toString());
		System.out.println(e2.toString());
		System.out.println(avl.lastKey());
		System.out.println(tree.lastKey());
		System.out.println(avl.hashCode());
		System.out.println(tree.hashCode());
	}

}
