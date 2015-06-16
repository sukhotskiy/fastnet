package org.fastnet.transport.marshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;

import org.fastnet.transport.server.HolderFactory;
import org.fastnet.transport.server.MethodCall;
import org.langcom.crypt.CryptUtilities;

public class HTTPMarshaller extends AbstractMarshaller {

	private InputStream in;
	private OutputStream out;
	private String request = "POST / HTTP/1.1\nContent-Type: text/xml;charset=UTF-8\nHost: {0}\nContent-Length: {1}\n#";
	private String responce = "HTTP/1.1 200 OK\nContent-Type: text/xml; charset=utf-8\nContent-Length: {0}\n#";
	private String hostAddress;

	public HTTPMarshaller(Socket socket) throws IOException {
		super(socket);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		hostAddress = socket.getInetAddress().getHostAddress();
	}

	@Override
	public Object request(MethodCall call) throws Exception {
		writeObject(call, true);
		return readObject();
	}

	@Override
	public void responce() throws Exception {
		MethodCall call = (MethodCall) readObject();
		if (call == null) {
			return;
		}
		writeObject(HolderFactory.invoke(call), false);
	}

	private Object readObject() throws IOException, ClassNotFoundException {
		String fromStream = getStringFromStream();
		String[] split = fromStream.split("#");
		if (split.length == 1) {
			return null;
		}
		return new ObjectInputStream(new ByteArrayInputStream(CryptUtilities.decode64(split[1]))).readObject();
	}

	public String getStringFromStream() throws IOException {
		byte[] buffer = new byte[0xFFFF];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int len; (len = in.read(buffer)) != -1;) {
			out.write(buffer, 0, len);
			if (in.available() == 0) {
				break;
			}
		}
		return new String(out.toByteArray());
	}

	private void writeObject(Object invoke, boolean isRequest) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(stream);
		os.writeObject(invoke);
		os.flush();
		String encode64 = CryptUtilities.encode64(stream.toByteArray());
		StringBuilder builder = new StringBuilder();
		if (isRequest) {
			builder.append(MessageFormat.format(request, hostAddress, encode64.length()));
		} else {
			builder.append(MessageFormat.format(responce, encode64.length()));
		}
		builder.append(encode64);
		out.write(builder.toString().getBytes());
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void responceError(Exception e) throws IOException {
		writeObject(e, false);
	}
}
