package org.fastnet.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SerialTest implements Serializable {

	private String className;
	private String methodName;
	private Object[] params;
	private Class<?>[] parameterTypes;
	private String string;
	private List<String> ss = new ArrayList<String>();
	public List<SerialTest> ddd = new ArrayList<SerialTest>();

	public SerialTest() {
		className = "3t4qrwet";
		methodName = "35421341";
		string = "sadfas";
		parameterTypes = new Class<?>[] {
				String.class, Integer.class
		};
		ss.add("11");
		ss.add("22");
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	@Override
	public String toString() {
		return "SerialTest [className=" + className + ", methodName=" + methodName + ", params=" + Arrays.toString(params) + ", parameterTypes="
			+ Arrays.toString(parameterTypes) + ", string=" + string + ", ss=" + ss + ", ddd=" + ddd + "]";
	}
}
