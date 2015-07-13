package org.fastnet;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.fastnet.transport.server.MethodCall;
import org.fastnet.util.AbstractMarshaller;
import org.fastnet.util.StringMarshaller;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FastClient implements InvocationHandler {

	private boolean autoCloseSession = true;
	private boolean useProxy = false;
	private String proxyHost;
	private int proxyPort, soTimeout = 10000, sendBufferSize = 8192, recieveBufferSize = 8192;
	private Long connectAttemptsInterval = 1000L;
	private Socket socket;
	private Socket tunnel;
	private InetSocketAddress address;
	private String host = "localhost";
	private int port = 8083;
	private AbstractMarshaller marshaller;
	private String callTo;
	private Map<Method, MethodCall> callMap = new ConcurrentHashMap<Method, MethodCall>();

	public FastClient(String host, int port) {
		this.host = host;
		this.port = port;
		address = new InetSocketAddress(host, port);
	}

	@SuppressWarnings("unchecked")
	public <RemoteInterface> RemoteInterface lookup(Class<RemoteInterface> clazz, boolean autoCloseSession) {
		this.autoCloseSession = autoCloseSession;
		callTo = clazz.getName();
		return (RemoteInterface) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {
			clazz
		}, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (((socket == null) || (!socket.isConnected()) || (socket.isClosed()))) {
			try {
				createSocket();
			} catch (Throwable e) {
				Log.e("**FastClient", "Error by connection ", e);
				throw new Exception("Error by connection to " + host + ":" + port + " " + e.getMessage());
			}
		}
		try {
			MethodCall call = callMap.get(method);
			if (call == null) {
				call = new MethodCall(callTo, method, args);
				callMap.put(method, call);
			}
			call.params = args;
			return marshaller.request(call);
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
		socket.connect(address, 20000);
		marshaller = getMarshaller(socket);
	}

	public AbstractMarshaller getMarshaller(Socket socket) throws IOException {
		return new StringMarshaller(socket);
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

	public Long getConnectAttemptsInterval() {
		return connectAttemptsInterval;
	}

	public void setConnectAttemptsInterval(Long connectAttemptsInterval) {
		this.connectAttemptsInterval = connectAttemptsInterval;
	}
}
