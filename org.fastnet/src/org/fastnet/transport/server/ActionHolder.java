package org.fastnet.transport.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fastnet.TypeUtil;
import org.langcom.log.LookLog;

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
			if (call.signature != null && call.signature.length > 0) {
				Class[] parameterTypes = new Class[call.signature.length];
				for (int a = 0; a < call.signature.length; a++) {
					parameterTypes[a] = TypeUtil.getTypeByString(call.signature[a]);
				}
				method = clazz.getMethod(call.methodName, parameterTypes);
			} else {
				method = clazz.getMethod(call.methodName);
			}
			methodMap.put(call.key, method);
		}
		try {
			if (alwaysNew) {
				return method.invoke(clazz.newInstance(), call.params);
			} else {
				return method.invoke(instance, call.params);
			}
		} catch (IllegalArgumentException e) {
			LookLog.error(getClass().getName(), "***Error by invocztion " + call, e);
			throw e;
		}
	}
}