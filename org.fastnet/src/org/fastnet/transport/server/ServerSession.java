package org.fastnet.transport.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fastnet.NetConstants;
import org.fastnet.transport.marshaller.AbstractMarshaller;
import org.fastnet.transport.marshaller.CommonMarshaller;
import org.fastnet.transport.marshaller.HTTPMarshaller;
import org.fastnet.transport.marshaller.StringMarshaller;

public class ServerSession implements Runnable, Comparable<ServerSession> {

	private final Socket socket;
	private Logger logger = Logger.getLogger(getClass().getName());
	private final String type;

	public ServerSession(Socket socket, String type) {
		this.socket = socket;
		this.type = type;
	}

	public AbstractMarshaller getMarshaller(Socket socket) throws IOException {
		if (type.equals(NetConstants.SOCKET)) {
			return new CommonMarshaller(socket);
		} else if (type.equals(NetConstants.HTTP)) {
			return new HTTPMarshaller(socket);
		} else if (type.equals(NetConstants.STRING)) {
			return new StringMarshaller(socket);
		}
		return null;
	}

	public void run() {
		try {
			AbstractMarshaller marshaller = getMarshaller(socket);
			try {
				while (socket.isConnected()) {
					// if (((socket == null) || (!socket.isConnected()) || (socket.isClosed()))) {
					marshaller.responce();
					socket.close();
					break;
				}
			} catch (EOFException e) {
				// close
			} catch (Exception e) {
				logger.log(Level.SEVERE, "***Error by receive ", e);
				try {
					marshaller.responceError(e);
				} catch (IOException e1) {
					logger.log(Level.SEVERE, "***Error by send ", e);
				}
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "***Error by close ", e);
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "***Error by start session ", e);
		}
	}

	@Override
	public int compareTo(ServerSession arg0) {
		return (int) (socket.hashCode() - arg0.socket.hashCode());
	}
}