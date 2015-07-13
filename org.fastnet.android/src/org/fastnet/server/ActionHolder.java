package org.fastnet.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fastnet.transport.server.MethodCall;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ActionHolder {

	private static final String INT = "int";
	private static final String BYTE = "byte";
	private static final String SHORT = "short";
	private static final String FLOAT = "float";
	private static final String DOUBLE = "double";
	private static final String CHAR = "char";
	private static final String LONG = "long";
	private static final String BOOLEAN = "boolean";
	private Class<?> clazz;
	private boolean alwaysNew = true;
	private Object instance;
	private Map<String, Method> methodMap = new ConcurrentHashMap<String, Method>();
	private Map<String, ActionHandler> handlerMap = new ConcurrentHashMap<String, ActionHandler>();

	ActionHolder(Class<?> klass, boolean isAlwaysNew) throws InstantiationException, IllegalAccessException {
		clazz = klass;
		alwaysNew = isAlwaysNew;
		if (!alwaysNew) {
			instance = clazz.newInstance();
		}
	}

	ActionHolder(Object value) {
		alwaysNew = false;
		instance = value;
		clazz = value.getClass();
	}

	void addHandler(ActionHandler handler) {
		handlerMap.put(handler.getKey(), handler);
	}

	Object invoke(MethodCall call) throws Exception {
		ActionHandler handler = handlerMap.get(call.key);
		if (handler == null) {
			return invokeByMethod(call);
		} else {
			return handler.invoke(call);
		}
	}

	@SuppressWarnings("rawtypes")
	private Object invokeByMethod(MethodCall call) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Method method = methodMap.get(call.key);
		if (method == null) {
			Class[] parameterTypes = new Class[call.signature.length];
			for (int a = 0; a < call.signature.length; a++) {
				String str = call.signature[a];
				if (str.equals(INT)) {
					parameterTypes[a] = int.class;
				} else if (str.equals(BOOLEAN)) {
					parameterTypes[a] = boolean.class;
				} else if (str.equals(LONG)) {
					parameterTypes[a] = long.class;
				} else if (str.equals(CHAR)) {
					parameterTypes[a] = char.class;
				} else if (str.equals(DOUBLE)) {
					parameterTypes[a] = double.class;
				} else if (str.equals(FLOAT)) {
					parameterTypes[a] = float.class;
				} else if (str.equals(SHORT)) {
					parameterTypes[a] = short.class;
				} else if (str.equals(BYTE)) {
					parameterTypes[a] = byte.class;
				} else {
					parameterTypes[a] = Class.forName(str);
				}
			}
			method = clazz.getMethod(call.methodName, parameterTypes);
			methodMap.put(call.key, method);
		}
		if (alwaysNew) {
			return method.invoke(clazz.newInstance(), call.params);
		} else {
			return method.invoke(instance, call.params);
		}
	}
}