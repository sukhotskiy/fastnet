package org.fastnet.transport;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fastnet.NetConstants;
import org.fastnet.transport.marshaller.AbstractMarshaller;
import org.fastnet.transport.marshaller.CommonMarshaller;
import org.fastnet.transport.marshaller.HTTPMarshaller;
import org.fastnet.transport.marshaller.StringMarshaller;
import org.fastnet.transport.server.MethodCall;
import org.langcom.CommonProperties;

public class FastClient implements InvocationHandler {

	private boolean autoCloseSession = true;
	private boolean useProxy = false;
	private String proxyHost;
	private int proxyPort, soTimeout = 1000, sendBufferSize = 8192, recieveBufferSize = 8192;
	private int connectAttempts = 1;
	private Long connectAttemptsInterval = 1000L;
	private Socket socket;
	private Socket tunnel;
	private InetSocketAddress address;
	private String host = "localhost";
	private int port = 8083;
	private AbstractMarshaller marshaller;
	private Logger logger = Logger.getLogger(getClass().getName());
	private String callTo;
	private Map<Method, MethodCall> callMap = new ConcurrentHashMap<Method, MethodCall>();
	private String type;

	public FastClient(String host, int port) {
		this.host = host;
		this.port = port;
		address = new InetSocketAddress(host, port);
	}

	public FastClient(CommonProperties props) {
		this(props.getString(NetConstants.host), props.getInteger(NetConstants.port));
		this.type = props.getString(NetConstants.type);
		this.proxyHost = props.getString(NetConstants.proxyHost);
		if (proxyHost != null && !proxyHost.isEmpty()) {
			useProxy = true;
		}
		this.proxyPort = props.getInteger(NetConstants.proxyPort);
		this.soTimeout = props.getInteger(NetConstants.soTimeout, "10000");
		this.connectAttempts = props.getInteger(NetConstants.connectAttempts, "1");
		this.connectAttemptsInterval = props.getLong(NetConstants.connectAttemptsInterval, "1000");
	}

	public void setProxySystemProperties(String fileName) throws IOException {
		CommonProperties props = new CommonProperties(fileName);
		System.setProperty(NetConstants.PROXY_ENABLED, "true");
		System.setProperty(NetConstants.HTTPS_PROXY_HOST, props.getString(NetConstants.proxyHost));
		System.setProperty(NetConstants.HTTPS_PROXY_PORT, props.getString(NetConstants.proxyPort));
		System.setProperty(NetConstants.HTTPS_PROXY_USER, props.getString(NetConstants.proxyUser));
		System.setProperty(NetConstants.HTTPS_PROXY_PASS, props.getString(NetConstants.proxyPassword));
		System.setProperty(NetConstants.HTTP_PROXY_HOST, props.getString(NetConstants.proxyHost));
		System.setProperty(NetConstants.HTTP_PROXY_PORT, props.getString(NetConstants.proxyPort));
		System.setProperty(NetConstants.HTTP_PROXY_USER, props.getString(NetConstants.proxyUser));
		System.setProperty(NetConstants.HTTP_PROXY_PASS, props.getString(NetConstants.proxyPassword));
	}

	@SuppressWarnings("unchecked")
	public <RemoteInterface> RemoteInterface lookup(Class<RemoteInterface> clazz, boolean autoCloseSession) {
		this.autoCloseSession = autoCloseSession;
		callTo = clazz.getName();
		return (RemoteInterface) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {
			clazz
		}, this);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (((socket == null) || (!socket.isConnected()) || (socket.isClosed()))) {
			try {
				createSocket();
			} catch (ConnectException e) {
				logger.log(Level.SEVERE, "***Error by connect ", e);
				// tryToConnect();
				throw e;
			}
		}
		try {
			MethodCall call = callMap.get(method);
			if (call == null) {
				call = new MethodCall(callTo, method, args);
				callMap.put(method, call);
			}
			call.params = args;
			Class<?> returnType = method.getReturnType();
			Object request = marshaller.request(call);
			if (request != null && !returnType.isAssignableFrom(request.getClass())) {
				throw new IllegalArgumentException("***Wrong return type: must be " + returnType + " - received " + request.getClass() + " - " + call);
			}
			return request;
		} finally {
			if (autoCloseSession) {
				socket.close();
			}
		}
	}

	public void close() {
		try {
			marshaller.close();
			if (socket != null) {
				socket.close();
			}
			if (tunnel != null) {
				tunnel.close();
			}
		} catch (Exception e) {
		}
	}

	// private void tryToConnect() throws InterruptedException, UnknownHostException, IOException {
	// int currentAttempt = 1;
	// while ((currentAttempt < connectAttempts)) {
	// try {
	// currentAttempt++;
	// createSocket();
	// } catch (ConnectException e) {
	// logger.warning("***Attempt to connect: " + currentAttempt);
	// Thread.sleep(connectAttemptsInterval);
	// }
	// }
	// }
	public void createSocket() throws UnknownHostException, IOException {
		if (useProxy) {
			tunnel = new Socket(proxyHost, proxyPort);
			socket = new Socket(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		} else {
			socket = new Socket();
		}
		socket.setPerformancePreferences(1, 0, 0);
		// socket.setSoTimeout(soTimeout);
		socket.setReceiveBufferSize(recieveBufferSize);
		socket.setSendBufferSize(sendBufferSize);
		socket.connect(address);
		marshaller = getMarshaller(socket);
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

	public boolean getUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public int getRecieveBufferSize() {
		return recieveBufferSize;
	}

	public void setRecieveBufferSize(int recieveBufferSize) {
		this.recieveBufferSize = recieveBufferSize;
	}

	public int getConnectAttempts() {
		return connectAttempts;
	}

	public void setConnectAttempts(int connectAttempts) {
		this.connectAttempts = connectAttempts;
	}

	public Long getConnectAttemptsInterval() {
		return connectAttemptsInterval;
	}

	public void setConnectAttemptsInterval(Long connectAttemptsInterval) {
		this.connectAttemptsInterval = connectAttemptsInterval;
	}
}
