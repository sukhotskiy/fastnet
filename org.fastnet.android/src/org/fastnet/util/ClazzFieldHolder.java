package org.fastnet.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class ClazzFieldHolder {

	Class<?> clazz;
	List<Field> fieldList = new ArrayList<Field>();

	ClazzFieldHolder(Class<?> klass) throws InstantiationException, IllegalAccessException {
		clazz = klass;
		exploreFields(clazz);
	}

	public ClazzFieldHolder(String klassName) throws ClassNotFoundException {
		clazz = Class.forName(klassName);
		exploreFields(clazz);
	}

	void exploreFields(Class<?> clazz) {
		fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		Iterator<Field> iterator = fieldList.iterator();
		while (iterator.hasNext()) {
			Field next = iterator.next();
			if (Modifier.isTransient(next.getModifiers()) || Modifier.isStatic(next.getModifiers())) {
				iterator.remove();
				continue;
			}
			next.setAccessible(true);
		}
		if (clazz.getSuperclass() != null) {
			exploreFields(clazz.getSuperclass());
		}
		Collections.sort(fieldList, new Comparator<Field>() {

			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
}