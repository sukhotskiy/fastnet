package org.fastnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.fastnet.server.ActionHandler;
import org.fastnet.server.HolderFactory;
import org.fastnet.server.ServerSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FastServer implements Runnable {

	protected ExecutorService executorService;
	protected ServerSocket serverSocket;
	private final int port;
	private final int timeout;
	private final int backLog;
	private final String host;
	protected boolean stop = false;
	private Logger logger = Logger.getLogger(getClass().getName());

	public FastServer(String hostIn, int portIn, int timeoutIn, int backlogIn, int corePoolSize, int maxPoolSize, int keepAliveTime) {
		host = hostIn;
		port = portIn;
		timeout = timeoutIn;
		backLog = backlogIn;
		executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
		new Thread(this).start();
	}

	@Override
	@SuppressWarnings("nls")
	public void run() {
		try {
			serverSocket = new ServerSocket(port, backLog, InetAddress.getByName(host));
			serverSocket.setReuseAddress(true);
			serverSocket.setPerformancePreferences(1, 0, 0);
			while (!stop) {
				Socket socket = serverSocket.accept();
				socket.setSoTimeout(timeout);
				executorService.execute(new ServerSession(socket));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "***Error by connect ", e);
		} finally {
			executorService.shutdown();
			try {
				serverSocket.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "***Error by shutdown ", e);
			}
		}
	}

	public void setStop() {
		stop = true;
	}

	public boolean isStopped() {
		return stop;
	}

	public Object putInstance(String key, Object value) {
		return HolderFactory.putInstance(key, value);
	}

	public Object putClass(String key, Class<?> clazz) throws InstantiationException, IllegalAccessException {
		return HolderFactory.putClass(key, clazz);
	}

	public Object putClass(String key, Class<?> clazz, boolean isAlwaysNew) throws InstantiationException, IllegalAccessException {
		return HolderFactory.putClass(key, clazz, isAlwaysNew);
	}

	public void putHandler(String key, ActionHandler handler) {
		HolderFactory.putHandler(key, handler);
	}
}
