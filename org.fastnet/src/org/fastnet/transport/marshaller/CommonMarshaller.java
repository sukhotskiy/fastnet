package org.fastnet.transport.marshaller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.fastnet.transport.server.HolderFactory;
import org.fastnet.transport.server.MethodCall;

public class CommonMarshaller extends AbstractMarshaller {

	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public CommonMarshaller(Socket socket) throws IOException {
		super(socket);
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
	}

	@Override
	public Object request(MethodCall call) throws Exception {
		oos.writeObject(call);
		oos.flush();
		return ois.readObject();
	}

	@Override
	public void responce() throws Exception {
		oos.writeObject(HolderFactory.invoke((MethodCall) ois.readObject()));
		oos.flush();
	}

	@Override
	public void close() throws IOException {
		oos.close();
		ois.close();
	}

	@Override
	public void responceError(Exception e) throws IOException {
		oos.writeObject(e);
		oos.flush();
	}
}
