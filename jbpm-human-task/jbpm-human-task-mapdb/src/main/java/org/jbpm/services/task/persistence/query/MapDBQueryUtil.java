package org.jbpm.services.task.persistence.query;

import java.util.Map;
import java.util.Set;

public class MapDBQueryUtil {

	public static <T> void addAll(Set<Long> values, Map<T, long[]> map, T id) {
		if (id == null) {
			return;
		}
		if (map.containsKey(id)) {
			long[] v = map.get(id);
			if (v != null) {
				for (long value : v) {
					values.add(value);
				}
			}
		}
	}
	
	public static <T> void removeAll(Set<Long> values, Map<T, long[]> map, T id) {
		if (id == null) {
			return;
		}
		if (map.containsKey(id)) {
			long[] v = map.get(id);
			if (v != null) {
				for (long value : v) {
					if (values.contains(value)) {
						values.remove(value);
					}
				}
			}
		}
	}
}
