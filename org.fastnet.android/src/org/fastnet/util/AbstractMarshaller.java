package org.fastnet.util;

import java.io.IOException;
import java.net.Socket;

import org.fastnet.transport.server.MethodCall;

public abstract class AbstractMarshaller {

	public AbstractMarshaller(Socket socket) {
	}

	public abstract Object request(MethodCall call) throws Exception;

	public abstract void close() throws IOException;

	public abstract void responce() throws Exception;

	public abstract void responceError(Exception e) throws IOException;
}
