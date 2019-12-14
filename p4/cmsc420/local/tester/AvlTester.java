package cmsc420.local.tester;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;

import cmsc420.sortedmap.AvlGTree;

public class AvlTester {

	public static void main(String[] args) {
		SortedMap<String, Integer> avl = new AvlGTree<>(3);
		SortedMap<String, Integer> tree = new TreeMap<>();
		
		for (int i = 0; i < 26; i ++) {
			avl.put("Entry" + String.valueOf(i), i);
			tree.put("Entry" + String.valueOf(i), i);
			testFunctionality(avl, tree);
		}
		for (int i = 0; i < 26; i ++) {
			avl.remove("Entry" + String.valueOf(i));
			tree.remove("Entry" + String.valueOf(i));
			testFunctionality(avl, tree);
		}
		for (int i = 0; i < 26; i ++) {
			avl.put("Entry" + String.valueOf(i), i);
			tree.put("Entry" + String.valueOf(i), i);
			testFunctionality(avl, tree);
		}
		Set<Map.Entry<String, Integer>> e1 = avl.entrySet();
		Set<Map.Entry<String, Integer>> e2 = tree.entrySet();
		for (int i = 0; i < 26; i ++) {
			e1.remove("Entry" + String.valueOf(i));
			e2.remove("Entry" + String.valueOf(i));
			testFunctionality(avl, tree);
		}
		SortedMap<String, Integer> avls = avl.subMap("Entry1", "Entry20");
		SortedMap<String, Integer> trees = tree.subMap("Entry1", "Entry20");
		testFunctionality(avls, trees);
		for (int i = 10; i < 20; i ++) {
			avls.remove("Entry" + String.valueOf(i));
			trees.remove("Entry" + String.valueOf(i));
			testFunctionality(avls, trees);
		}
		System.out.println("Success");
	}
	
	private static void testFunctionality(SortedMap<String, Integer> avl, SortedMap<String, Integer> tree) {
		Set<Map.Entry<String, Integer>> e1 = avl.entrySet();
		Set<Map.Entry<String, Integer>> e2 = tree.entrySet();
		Assert.assertEquals(e1.toString(), e2.toString());
		Iterator<Map.Entry<String, Integer>> it1 = e1.iterator();
		Iterator<Map.Entry<String, Integer>> it2 = e2.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			Map.Entry<String, Integer> me1 = it1.next();
			Map.Entry<String, Integer> me2 = it2.next();
			Assert.assertTrue(me1.equals(me2));
		}
		Assert.assertTrue(!it1.hasNext() && !it2.hasNext());
		Assert.assertTrue(avl.equals(tree) && tree.equals(avl));
		Assert.assertTrue(e1.equals(e2) && e2.equals(e1));
		Assert.assertEquals(e1.hashCode(), e2.hashCode());
		Assert.assertEquals(e1.toString(), e2.toString());
	}

}
