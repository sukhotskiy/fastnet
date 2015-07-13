package org.fastnet.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Contains a set of utility methods
 * 
 * @author nemo
 */
public final class LangUtils {

	private static final String SPACE = "    ";
	private static int indexCount;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat);

	private LangUtils() {
	}

	public static List<Field> exploreClassFields(Object obj) throws ClassNotFoundException {
		List<Field> fieldList = new ArrayList<Field>();
		exploreFields(fieldList, getClazz(obj));
		return fieldList;
	}

	public static Class<? extends Object> getClazz(Object obj) throws ClassNotFoundException {
		Class<? extends Object> clazz = null;
		if (obj instanceof Class<?>) {
			clazz = (Class<?>) obj;
		} else if (obj instanceof String) {
			clazz = Class.forName((String) obj);
		} else {
			clazz = obj.getClass();
		}
		return clazz;
	}

	public static void exploreFields(List<Field> fieldList, Class<?> clazz) {
		fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		Iterator<Field> iterator = fieldList.iterator();
		while (iterator.hasNext()) {
			Field next = iterator.next();
			if (Modifier.isTransient(next.getModifiers()) || Modifier.isFinal(next.getModifiers())) {
				iterator.remove();
				continue;
			}
		}
		if (clazz.getSuperclass() != null) {
			exploreFields(fieldList, clazz.getSuperclass());
		}
	}

	public static boolean isPrimitive(Class<?> klass) {
		if (klass != null) {
			if (klass.equals(String.class) || klass.equals(Integer.class) || klass.equals(Long.class) || klass.equals(Character.class) || klass.equals(Boolean.class) || klass.equals(Object.class)
				|| klass.equals(BigDecimal.class) || klass.equals(Date.class) || klass.equals(Double.class) || klass.equals(Float.class) || klass.equals(BigInteger.class) || klass.equals(Byte.class)
				|| klass.equals(int.class) || klass.equals(double.class) || klass.equals(float.class) || klass.equals(boolean.class) || klass.equals(long.class) || klass.equals(char.class)
				|| klass.equals(byte.class) || klass.equals(byte[].class) || klass.equals(char[].class) || klass.equals(void.class) || klass.isEnum()) {
				return true;
			}
		}
		return false;
	}

	public static String formatSize(long longSize, int decimalPos) {
		NumberFormat fmt = NumberFormat.getNumberInstance();
		if (decimalPos >= 0) {
			fmt.setMaximumFractionDigits(decimalPos);
		}
		final double size = longSize;
		double val = size / (1024 * 1024 * 1024);
		if (val > 1) {
			return fmt.format(val).concat(" Gb");
		}
		val = size / (1024 * 1024);
		if (val > 1) {
			return fmt.format(val).concat(" MB");
		}
		val = size / 1024;
		if (val > 10) {
			return fmt.format(val).concat(" KB");
		}
		return fmt.format(val).concat(" bytes");
	}

	public static List<String> enumToList(Enum... ee) {
		List<String> result = new ArrayList<String>();
		for (Enum obj : ee) {
			result.add(obj.name());
		}
		return result;
	}

	public static final boolean equalsAnyWay(Object o1, Object o2) {
		if ((o1 == null) && (o2 != null)) {
			return false;
		} else if ((o1 != null) && (o2 == null)) {
			return false;
		} else if ((o1 == null) && (o2 == null)) {
			return true;
		} else if (!o1.equals(o2)) {
			return false;
		}
		return true;
	}

	public static final int compareAnyWay(Comparable o1, Comparable o2) {
		if ((o1 == null) && (o2 != null)) {
			return 1;
		} else if ((o1 != null) && (o2 == null)) {
			return -1;
		} else if ((o1 == null) && (o2 == null)) {
			return 0;
		}
		return o1.compareTo(o2);
	}

	public static final int compareAnyWay(Object o1, Object o2) {
		if ((o1 == null) && (o2 != null)) {
			return 1;
		} else if ((o1 != null) && (o2 == null)) {
			return -1;
		} else if ((o1 == null) && (o2 == null)) {
			return 0;
		}
		if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
			return ((Comparable) o1).compareTo(o2);
		}
		return o1.toString().compareTo(o2.toString());
	}

	public static <T> T getDefault(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	public static String defaultIfEmpty(Object value) {
		return defaultIfEmpty(value, "");
	}

	public static String defaultIfEmpty(Object value, String defaultString) {
		if (value == null) {
			return defaultString;
		}
		if (value.toString().trim().length() == 0) {
			return defaultString;
		}
		return value.toString();
	}

	public static boolean isEmptyString(Object value) {
		return (value == null) || (value.toString() == null) || (value.toString().trim().length() == 0);
	}

	public static String abbreviate(String string, int maxLength, String delimiter, int partLength) {
		if (maxLength == 0) {
			return string;
		}
		if (string.length() <= maxLength) {
			return string;
		}
		String result = ""; //$NON-NLS-1$
		if ((delimiter != null) && string.contains(delimiter)) {
			String[] split = string.split(delimiter);
			for (String str : split) {
				if (str.length() > 0) {
					if (str.length() > partLength) {
						if (result.length() > 0) {
							result += delimiter;
						}
						result += str.substring(0, partLength);
					} else {
						result += str;
					}
				}
			}
		} else {
			char[] charArray = string.toCharArray();
			for (int a = 0; a < charArray.length; a++) {
				Character character = charArray[a];
				if (Character.isUpperCase(character)) {
					if (result.length() > 0) {
						result += delimiter;
					}
					int endIndex = a + 1 + partLength;
					if (endIndex > string.length()) {
						endIndex = string.length();
					}
					result += character + string.substring(a + 1, endIndex);
				}
			}
		}
		if (result.length() == 0) {
			result = string;
		}
		if (result.length() > maxLength) {
			result = result.substring(0, maxLength);
		}
		return result;
	}

	public static String getRandomIndexString(int lenght) {
		String res = String.valueOf(new Random().nextInt());
		indexCount++;
		return indexCount + "_" + res.substring(res.length() - lenght, res.length()); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	public static Boolean parseToBoolean(String str) {
		if ((str != null) && (str.length() > 0)) {
			if (str.equalsIgnoreCase("0")) {
				return Boolean.FALSE;
			}
			if (str.equalsIgnoreCase("1")) {
				return Boolean.TRUE;
			}
			if (str.equalsIgnoreCase("true")) {
				return Boolean.TRUE;
			}
			if (str.equalsIgnoreCase("false")) {
				return Boolean.FALSE;
			}
		}
		return Boolean.FALSE;
	}

	@SuppressWarnings("nls")
	public static Integer parseToInteger(String str) {
		if ((str != null) && (str.length() > 0)) {
			try {
				return Integer.valueOf(str);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String listToStr(List<?> list, String delim) {
		StringBuilder builder = new StringBuilder();
		for (Object bj : list) {
			builder.append(bj);
			builder.append(delim);
		}
		return builder.toString();
	}

	public static List<String> parseToList(String str, String delim) {
		List<String> result = new ArrayList<String>();
		if ((str != null) && (str.length() > 0)) {
			try {
				String[] split = str.split(delim);
				for (String sstr : split) {
					result.add(sstr);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String format(String[] args) {
		String result = "";
		for (String string : args) {
			result += string;
		}
		return result;
	}

	public static <T> List<T> toList(T... objects) {
		List result = new ArrayList<T>();
		for (T item : objects) {
			result.add(item);
		}
		return result;
	}

	public static Object getDefault(String value) {
		return getDefault(value, "");
	}

	public static Object getDefault(Boolean current) {
		return getDefault(current == null ? "" : current.toString(), "");
	}

	public static String capitalize(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		return new StringBuffer(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1)).toString();
	}

	public static void removeDir(File file) throws IOException {
		cleanDir(file);
		if (file.exists()) {
			if (!file.delete()) {
				throw new IOException("***Unable to delete file: " + file.getPath());
			}
		}
	}

	public static void cleanDir(File file, String... namesToLive) throws IOException {
		if (file.exists()) {
			for (File item : file.listFiles()) {
				if (matchNames(file.getName(), namesToLive)) {
					continue;
				}
				if (item.isDirectory()) {
					cleanDir(item);
				}
				if (!item.delete()) {
					throw new IOException("***Unable to delete file: " + item.getPath());
				}
			}
		}
	}

	private static boolean matchNames(String name, String[] namesToLive) {
		for (String str : namesToLive) {
			if (str.contains("*")) {
				if (name.matches(str)) {
					return true;
				}
			} else {
				if (name.equals(str)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Object getTypeValue(String obj, Class<?> klass) {
		if (obj != null) {
			if (obj.trim().length() == 0) {
				return null;
			}
			if (klass.equals(String.class)) {
				return obj.toString();
			} else if (klass.equals(Integer.class)) {
				return Integer.valueOf(obj.toString());
			} else if (klass.equals(Long.class)) {
				return Long.valueOf(obj.toString());
			} else if (klass.equals(Character.class)) {
				return obj.toString().charAt(0);
			} else if (klass.equals(Boolean.class)) {
				return extracted(obj.toString());
			} else if (klass.equals(BigDecimal.class)) {
				return new BigDecimal(obj.toString());
			} else if (klass.equals(Date.class)) {
				try {
					return format.parse(obj.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (klass.equals(Double.class)) {
				return Double.valueOf(obj.toString());
			} else if (klass.equals(Float.class)) {
				return Float.valueOf(obj.toString());
			} else if (klass.equals(BigInteger.class)) {
				return new BigInteger(obj.toString());
			} else if (klass.equals(Byte.class)) {
				return obj.toString().getBytes();
			} else if (klass.equals(int.class)) {
				return Integer.valueOf(obj.toString()).intValue();
			} else if (klass.equals(double.class)) {
				return Double.valueOf(obj.toString()).doubleValue();
			} else if (klass.equals(float.class)) {
				return Float.valueOf(obj.toString()).floatValue();
			} else if (klass.equals(boolean.class)) {
				return extracted(obj.toString());
			} else if (klass.equals(long.class)) {
				return Long.valueOf(obj.toString()).longValue();
			} else if (klass.equals(char.class)) {
				return obj.toString().charAt(0);
			} else if (klass.equals(byte.class)) {
				return obj.toString().getBytes();
			} else if (klass.equals(byte[].class)) {
				return obj.toString().getBytes();
			}
		}
		return null;
	}

	private static Boolean extracted(String objectStr) {
		if (objectStr.equalsIgnoreCase("1")) {
			return Boolean.TRUE;
		} else if (objectStr.equalsIgnoreCase("0")) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(objectStr);
	}

	public static SimpleDateFormat getFormat() {
		return format;
	}
}
