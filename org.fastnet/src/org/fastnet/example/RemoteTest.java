package org.fastnet.example;

import java.util.Arrays;

public class RemoteTest implements IRemoteTest {

	public RemoteTest() {
	}

	public byte[] getData(int size) {
		byte[] result = new byte[size];
		Arrays.fill(result, Character.CURRENCY_SYMBOL);
		// System.out.println("Context:  "+ ContextManager.get());
		// System.out.println("Return  "+size+"  bytes");
		return result;
	}

	public byte[] pingData(byte[] data) {
		System.out.println("Ping  " + data.length + "  bytes");
		return data;
	}
}
