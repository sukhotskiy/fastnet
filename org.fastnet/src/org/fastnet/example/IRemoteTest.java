package org.fastnet.example;


public interface IRemoteTest {
	
	public byte[] getData(int size);
	
	public byte[] pingData(byte[] data);
}


