package org.fastnet.transport.marshaller;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.langcom.log.LookLog;

public class StringSerializer {

	private static final String NULL = "null";
	private static final String BYTE = "byte";
	private static final String SHORT = "short";
	private static final String FLOAT = "float";
	private static final String DOUBLE = "double";
	private static final String CHAR = "char";
	private static final String LONG = "long";
	private static final String BOOLEAN = "boolean";
	private static final String INT = "int";
	private static final String NN = "";
	private static final String CLASS2 = "[class ";
	private static final String ARR = "[";
	private static final String ALIST = "&L";
	private static final String CLASS = "class=";
	private static final String FTYPE = "<=>";
	private static final String FVALUE = "<v>";
	private static final String FIELD = "#";
	private static final String CLAZZ = "@";
	private static final String FIELD1 = "##";
	private static final String CLAZZ1 = "@@";
	public static final String OBJ_FIELD = "<#>";
	public static final String OBJ_CLAZZ = "<@>";
	private Map<String, ClazzFieldHolder> hMap = new ConcurrentHashMap<String, ClazzFieldHolder>();

	public String serialize(Object obj) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return serialize(obj, OBJ_CLAZZ, OBJ_FIELD);
	}

	public <T> T deserialize(String str) throws InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		return deserialize(str, OBJ_CLAZZ, OBJ_FIELD);
	}

	private String serialize(Object obj, String cd, String fd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (cd.contains("@@@@@@")) {
			// System.out.println("----------serialize more------" + obj);
			return null;
		}
		Class<?> class1 = obj.getClass();
		if (isPrimitive(class1)) {
			StringBuilder builder = new StringBuilder();
			builder.append(FVALUE);
			builder.append(class1.getName());
			builder.append(FTYPE);
			if (class1.equals(Date.class)) {
				builder.append(((Date) obj).getTime());
			} else {
				builder.append(obj.toString());
			}
			return builder.toString();
		}
		String ccd = cd.replaceFirst(CLAZZ, CLAZZ1);
		String ffd = fd.replaceFirst(FIELD, FIELD1);
		if (List.class.isAssignableFrom(class1)) {
			List<?> list = (List<?>) obj;
			StringBuilder builder = new StringBuilder();
			builder.append(ALIST);
			builder.append(class1.getName());
			builder.append(cd);
			for (Object value : list) {
				addSerValue(builder, ccd, ffd, value);
				builder.append(fd);
			}
			return builder.toString();
		} else if (class1.isArray()) {
			StringBuilder builder = new StringBuilder();
			builder.append(ARR);
			builder.append(class1.getComponentType());
			builder.append(cd);
			int length = Array.getLength(obj);
			for (int i = 0; i < length; i++) {
				Object value = Array.get(obj, i);
				addSerValue(builder, ccd, ffd, value);
				builder.append(fd);
			}
			return builder.toString();
		} else {
			ClazzFieldHolder hh = getHolder(class1.getName());
			StringBuilder builder = new StringBuilder();
			builder.append(class1.getName());
			builder.append(cd);
			for (Field next : hh.fieldList) {
				Object value = next.get(obj);
				addSerValue(builder, ccd, ffd, value);
				builder.append(fd);
			}
			return builder.toString();
		}
	}

	private ClazzFieldHolder getHolder(String klassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ClazzFieldHolder hh = hMap.get(klassName);
		if (hh == null) {
			hh = new ClazzFieldHolder(klassName);
			hMap.put(klassName, hh);
		}
		return hh;
	}

	// @SuppressWarnings("rawtypes")
	// private void addSerValue(StringBuilder builder, String ccd, String ffd, Object value) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	// if (value != null) {
	// if (value instanceof Class) {
	// builder.append(((Class) value).getName());
	// } else {
	// builder.append(serialize(value, ccd, ffd));
	// }
	// }
	// }
	@SuppressWarnings("rawtypes")
	private void addSerValue(StringBuilder builder, String ccd, String ffd, Object value) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (value == null) {
			builder.append(NULL);
		} else if (value instanceof Class) {
			builder.append(CLASS);
			builder.append(((Class) value).getName());
		} else if (isPrimitive(value.getClass())) {
			if (value.getClass().equals(Date.class)) {
				builder.append(((Date) value).getTime());
			} else {
				builder.append(value.toString());
			}
		} else {
			builder.append(serialize(value, ccd, ffd));
		}
	}

	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	private <T> T deserialize(String str, String cd, String fd) throws InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		T result = null;
		if (str.equals(NULL) || str.trim().length() == 0) {
			return null;
		}
		if (str.startsWith(CLASS)) {
			return (T) Class.forName(str.replace(CLASS, NN));
		}
		if (str.startsWith(FVALUE)) {
			str = str.replace(FVALUE, NN);
			String[] split2 = str.split(FTYPE);
			Class<?> forName = Class.forName(split2[0]);
			return (T) getValue(forName, split2[1]);
		}
		if (str.equals(INT)) {
			return (T) int.class;
		} else if (str.equals(BOOLEAN)) {
			return (T) boolean.class;
		} else if (str.equals(LONG)) {
			return (T) long.class;
		} else if (str.equals(CHAR)) {
			return (T) char.class;
		} else if (str.equals(DOUBLE)) {
			return (T) double.class;
		} else if (str.equals(FLOAT)) {
			return (T) float.class;
		} else if (str.equals(SHORT)) {
			return (T) short.class;
		} else if (str.equals(BYTE)) {
			return (T) byte.class;
		}
		str = str.replace(CLASS2, ARR);
		String[] split = str.split(cd);
		String classname = split[0];
		if (split.length == 1 || split[1].trim().isEmpty()) {
			if (classname.startsWith(ARR)) {
				return (T) Array.newInstance(Class.forName(classname.substring(1)), 0);
			}
			if (!classname.contains(ALIST)) {
				return (T) str;
			}
		} else {
			String[] split3 = split[1].split(fd);
			if (classname.startsWith(ARR)) {
				result = (T) Array.newInstance(Class.forName(classname.substring(1)), split3.length);
				String ccd = cd.replaceFirst(CLAZZ, CLAZZ1);
				String ffd = fd.replaceFirst(FIELD, FIELD1);
				for (int a = 0; a < split3.length; a++) {
					if (!split3[a].trim().isEmpty())
						Array.set(result, a, deserialize(split3[a], ccd, ffd));
				}
				return result;
			}
			if (classname.startsWith(ALIST)) {
				result = (T) Class.forName(classname.substring(2)).newInstance();
				String ccd = cd.replaceFirst(CLAZZ, CLAZZ1);
				String ffd = fd.replaceFirst(FIELD, FIELD1);
				List list = (List) result;
				for (int a = 0; a < split3.length; a++) {
					if (!split3[a].trim().isEmpty())
						list.add(deserialize(split3[a], ccd, ffd));
				}
				return result;
			}
			ClazzFieldHolder hh = getHolder(classname);
			try {
				result = (T) hh.clazz.newInstance();
			} catch (Throwable e) {
				LookLog.error(getClass().getName(), "***Can not instantiate class " + hh.clazz.getName());
				throw new InstantiationException(e.getMessage());
			}
			String ccd = cd.replaceFirst(CLAZZ, CLAZZ1);
			String ffd = fd.replaceFirst(FIELD, FIELD1);
			for (int as = 0; as < split3.length; as++) {
				String serialized = split3[as];
				if (serialized.trim().isEmpty()) {
					continue;
				}
				Field next = hh.fieldList.get(as);
				if (isPrimitive(next.getType())) {
					next.set(result, getValue(next.getType(), serialized));
				} else {
					next.set(result, deserialize(serialized, ccd, ffd));
					// if (List.class.isAssignableFrom(next.getType())) {
					// // if (!serialized.contains(FTYPE)) {
					// // continue;
					// // }
					// List list = (List) next.get(result);
					// if (list == null) {
					// list = new ArrayList();
					// next.set(result, list);
					// }
					// list.clear();
					// String ccd = cd.replaceFirst(CLAZZ, CLAZZ1);
					// String ffd = fd.replaceFirst(FIELD, FIELD1);
					// serialized = serialized.substring(serialized.indexOf(cd) + cd.length());
					// String[] split4 = serialized.split(ccd);
					// if (split4.length < 2) {
					// continue;
					// }
					// String[] ffp = split4[1].split(ffd);
					// for (int a = 0; a < ffp.length; a++) {
					// String string = ffp[a];
					// if (string.length() > 0) {
					// String[] split2 = string.split(FTYPE);
					// Class<?> forName = Class.forName(split2[0]);
					// if (isPrimitive(forName)) {
					// list.add(getValue(forName, split2[1]));
					// } else {
					// if (!split2[1].trim().isEmpty())
					// list.add(deserialize(split2[1], ccd.replaceFirst(CLAZZ, CLAZZ1), ffd.replaceFirst(FIELD, FIELD1)));
					// }
					// }
					// }
					// return result;
					// } else {
					// if (!serialized.trim().isEmpty())
					// next.set(result, deserialize(serialized, cd.replaceFirst(CLAZZ, CLAZZ1), fd.replaceFirst(FIELD, FIELD1)));
					// }
				}
			}
		}
		return result;
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private Object getValue(Class klass, String string) throws ParseException {
		if (string.equals(NULL)) {
			return null;
		}
		if (klass.equals(String.class)) {
			return string;
		} else if (klass.equals(Integer.class)) {
			return Integer.valueOf(string);
		} else if (klass.equals(Long.class)) {
			return Long.valueOf(string);
		} else if (klass.equals(Character.class)) {
			return string.charAt(0);
		} else if (klass.equals(Boolean.class)) {
			return Boolean.valueOf(string);
		} else if (klass.equals(BigDecimal.class)) {
			return new BigDecimal(string);
		} else if (klass.equals(Date.class)) {
			return new Date(Long.valueOf(string));
		} else if (klass.equals(Double.class)) {
			return Double.valueOf(string);
		} else if (klass.equals(Float.class)) {
			return Float.valueOf(string);
		} else if (klass.equals(BigInteger.class)) {
			return new BigInteger(string);
		} else if (klass.equals(Byte.class)) {
			return string.getBytes();
		} else if (klass.equals(int.class)) {
			return Integer.valueOf(string);
		} else if (klass.equals(double.class)) {
			return Double.valueOf(string);
		} else if (klass.equals(float.class)) {
			return Float.valueOf(string);
		} else if (klass.equals(boolean.class)) {
			return Boolean.valueOf(string);
		} else if (klass.equals(long.class)) {
			return Long.valueOf(string);
		} else if (klass.equals(char.class)) {
			return string.toCharArray();
		} else if (klass.equals(byte.class)) {
			return string.getBytes();
		} else if (klass.isEnum()) {
			return Enum.valueOf(klass, string);
		} else {
			return null;
		}
	}

	public static boolean isPrimitive(Class<?> klass) {
		return klass.isPrimitive() || klass.equals(String.class) || klass.equals(Integer.class) || klass.equals(Long.class) || klass.equals(Character.class)
			|| klass.equals(Boolean.class) || klass.equals(Object.class) || klass.equals(BigDecimal.class) || klass.equals(Date.class)
			|| klass.equals(Double.class) || klass.equals(Float.class) || klass.equals(BigInteger.class) || klass.equals(Byte.class)
			|| klass.equals(byte[].class) || klass.equals(char[].class) || klass.equals(void.class) || klass.isEnum();
	}

	public static boolean isPrimitiveClass(String str) {
		if (str.equals(INT)) {
			return true;
		} else if (str.equals(BOOLEAN)) {
			return true;
		} else if (str.equals(LONG)) {
			return true;
		} else if (str.equals(CHAR)) {
			return true;
		} else if (str.equals(DOUBLE)) {
			return true;
		} else if (str.equals(FLOAT)) {
			return true;
		} else if (str.equals(SHORT)) {
			return true;
		} else if (str.equals(BYTE)) {
			return true;
		}
		return false;
	}
}
