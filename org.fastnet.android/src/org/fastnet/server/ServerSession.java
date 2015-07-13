package org.fastnet.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.fastnet.util.AbstractMarshaller;
import org.fastnet.util.StringMarshaller;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerSession implements Runnable, Comparable<ServerSession> {

	private final Socket socket;
	private Logger logger = Logger.getLogger(getClass().getName());

	public ServerSession(Socket socket) {
		this.socket = socket;
	}

	public AbstractMarshaller getMarshaller(Socket socket) throws IOException {
		return new StringMarshaller(socket);
	}

	@Override
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
		return (socket.hashCode() - arg0.socket.hashCode());
	}
}