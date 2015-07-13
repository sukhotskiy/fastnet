package org.fastnet.server;

import org.fastnet.transport.server.MethodCall;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HolderFactory {

	public static Map<String, ActionHolder> hMap = new ConcurrentHashMap<String, ActionHolder>();

	private HolderFactory() {
	}

	public static Object putInstance(String key, Object value) {
		return hMap.put(key, new ActionHolder(value));
	}

	public static Object putClass(String key, Class<?> clazz) throws InstantiationException, IllegalAccessException {
		return hMap.put(key, new ActionHolder(clazz, true));
	}

	public static Object putClass(String key, Class<?> clazz, boolean isAlwaysNew) throws InstantiationException, IllegalAccessException {
		return hMap.put(key, new ActionHolder(clazz, isAlwaysNew));
	}

	public static Object invoke(MethodCall call) throws Exception {
		ActionHolder hh = hMap.get(call.className);
		if (hh == null) {
			hh = new ActionHolder(Class.forName(call.className), true);
			hMap.put(call.className, hh);
		}
		return hh.invoke(call);
	}

	public static void putHandler(String key, ActionHandler handler) {
		hMap.get(key).addHandler(handler);
	}
}
