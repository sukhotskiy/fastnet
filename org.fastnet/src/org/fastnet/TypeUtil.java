package org.fastnet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class TypeUtil {

	public static Class<?> getTypeByString(String klass) throws ClassNotFoundException {
		if (klass.equals(String.class.getName())) {
			return String.class;
		} else if (klass.equals(Integer.class.getName())) {
			return Integer.class;
		} else if (klass.equals(Long.class.getName())) {
			return Long.class;
		} else if (klass.equals(Date.class.getName())) {
			return Date.class;
		} else if (klass.equals(Double.class.getName())) {
			return Double.class;
		} else if (klass.equals(Float.class.getName())) {
			return Float.class;
		} else if (klass.equals(Character.class.getName())) {
			return Character.class;
		} else if (klass.equals(Boolean.class.getName())) {
			return Boolean.class;
		} else if (klass.equals(BigDecimal.class.getName())) {
			return BigDecimal.class;
		} else if (klass.equals(BigInteger.class.getName())) {
			return BigInteger.class;
		} else if (klass.equals(Byte.class.getName())) {
			return Byte.class;
		} else if (klass.equals(int.class.getName())) {
			return int.class;
		} else if (klass.equals(double.class.getName())) {
			return double.class;
		} else if (klass.equals(float.class.getName())) {
			return float.class;
		} else if (klass.equals(boolean.class.getName())) {
			return boolean.class;
		} else if (klass.equals(long.class.getName())) {
			return long.class;
		} else if (klass.equals(char.class.getName())) {
			return char.class;
		} else if (klass.equals(byte.class.getName())) {
			return byte.class;
		} else if (klass.equals(byte[].class.getName())) {
			return byte[].class;
		} else if (klass.equals(char[].class.getName())) {
			return char[].class;
		}
		return Class.forName(klass);
	}

	public static boolean isPrimitive(Class<?> klass) {
		if (klass != null) {
			if (klass.equals(String.class) || klass.equals(Integer.class) || klass.equals(Long.class) || klass.equals(Character.class)
				|| klass.equals(Boolean.class) || klass.equals(Object.class) || klass.equals(BigDecimal.class) || klass.equals(Date.class)
				|| klass.equals(Double.class) || klass.equals(Float.class) || klass.equals(BigInteger.class) || klass.equals(Byte.class)
				|| klass.equals(int.class) || klass.equals(double.class) || klass.equals(float.class) || klass.equals(boolean.class)
				|| klass.equals(long.class) || klass.equals(char.class) || klass.equals(byte.class) || klass.equals(byte[].class) || klass.equals(char[].class)
				|| klass.equals(void.class) || klass.isEnum()) {
				return true;
			}
		}
		return false;
	}
}
