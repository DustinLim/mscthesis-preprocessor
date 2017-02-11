package nl.dustinlim.thesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ThesisTreeMap extends TreeMap<String, List<String>> {

	private static final long serialVersionUID = -2299766930371650978L;

	public ThesisTreeMap() {
		super();
	}
	
	public void append(String key, String value) {
		append(key, Arrays.asList(value));
	}

	public void append(String key, List<String> values) {
		for (String value : values) {
			if (this.containsKey(key)) {
				// Append to the key's list
				List<String> list = this.get(key);
				list.add(value);
			}
			else {
				// Set new key and list
				List<String> list = new ArrayList<>();
				list.add(value);
				this.put(key, list);
			}
		}
	}
	
	public static ThesisTreeMap mergeMaps(
			ThesisTreeMap map1, 
			ThesisTreeMap map2
			) {
		ThesisTreeMap merged = new ThesisTreeMap();

		for (Map.Entry<String, List<String>> entry : map1.entrySet()) {
			merged.append(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, List<String>> entry : map2.entrySet()) {
			merged.append(entry.getKey(), entry.getValue());
		}

		return merged;
	}

	
}
