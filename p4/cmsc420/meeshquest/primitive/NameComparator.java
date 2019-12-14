package cmsc420.meeshquest.primitive;

import java.util.Comparator;

public class NameComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		int i = o1.compareTo(o2);
		if (i > 0) {
			return -1;
		} else if (i < 0) {
			return 1;
		} else {
			return 0;
		}
	}

}
