package org.fastnet.transport.marshaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fastnet.transport.server.HolderFactory;
import org.fastnet.transport.server.MethodCall;

public class StringMarshaller extends AbstractMarshaller {

	private static final String NULL_RESPONCE = "null";
	private InputStream in;
	private OutputStream out;
	private ByteArrayOutputStream os = new ByteArrayOutputStream();
	private StringSerializer serializer;

	public StringMarshaller(Socket socket) throws IOException {
		super(socket);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		serializer = new StringSerializer();
	}

	@Override
	public Object request(MethodCall call) throws Exception {
		out.write((serializer.serialize(call)).getBytes());
		out.flush();
		String str = getStringFromStream();
		if (str.equals(NULL_RESPONCE)) {
			return null;
		}
		return serializer.deserialize(str);
	}

	@Override
	public void responce() throws Exception {
		String stringFromStream = getStringFromStream();
		if (stringFromStream.trim().isEmpty()) {
			return;
		}
		MethodCall deserialize = (MethodCall) serializer.deserialize(stringFromStream);
		if (deserialize == null) {
			return;
		}
		Object invoke = HolderFactory.invoke(deserialize);
		if (invoke != null) {
			byte[] bytes = serializer.serialize(invoke).getBytes();
			out.write(bytes);
		} else {
			out.write(NULL_RESPONCE.getBytes());
		}
		out.flush();
	}

	public String getStringFromStream() throws IOException {
		byte[] buffer = new byte[0xFFFF];
		os.reset();
		for (int len; (len = in.read(buffer)) != -1;) {
			os.write(buffer, 0, len);
			if (in.available() == 0) {
				break;
			}
		}
		return new String(os.toByteArray());
	}

	@Override
	public void close() throws IOException {
		out.close();
		in.close();
	}

	@Override
	public void responceError(Exception e) throws IOException {
		try {
			out.write(serializer.serialize(e).getBytes());
			out.flush();
		} catch (Exception e1) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "***Responce error", e);
			return;
		}
	}
}
