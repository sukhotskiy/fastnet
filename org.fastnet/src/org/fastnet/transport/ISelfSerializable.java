package org.fastnet.transport;

public interface ISelfSerializable {

	public String serialize();

	public void deserialize(String str);
}
