package org.fastnet.transport.server;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodCall implements Serializable {

	private static final String dash = "-";
	private transient static final long serialVersionUID = 3001554805523802488L;
	public String className;
	public String methodName;
	public Object[] params;
	public String[] signature;
	public String key;

	public MethodCall() {
	}

	public MethodCall(final String className, final Method method, final Object[] params) {
		this.className = className;
		methodName = method.getName();
		StringBuilder builder = new StringBuilder();
		builder.append(methodName);
		Class<?>[] parameterTypes = method.getParameterTypes();
		signature = new String[parameterTypes.length];
		for (int a = 0; a < parameterTypes.length; a++) {
			signature[a] = parameterTypes[a].getName();
			builder.append(dash);
			builder.append(signature[a]);
		}
		key = builder.toString();
		this.params = params;
	}

	public Object[] getParams() {
		return params;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "MethodCall [className=" + className + ", methodName=" + methodName + ", params=" + Arrays.toString(params) + ", signature="
			+ Arrays.toString(signature) + ", key=" + key + "]";
	}
}
