package org.fastnet;

import java.io.IOException;

import org.fastnet.transport.FastClient;
import org.fastnet.transport.server.FastServer;
import org.langcom.CommonProperties;

public class FastNet {

	protected CommonProperties properties;

	// server url=socket://localhost:5400/?timeout=10000&corePoolSize=250&maxPoolSize=500&keepAliveTime=100&backLog=100
	// client url=socket://localhost:5400/?proxyHost=&proxyPort=250&soTimeout=500&connectAttempts=1&connectAttemptsInterval=1000
	public FastNet(String propertiesFileName) throws IOException {
		properties = new CommonProperties(propertiesFileName);
		parseHostString(properties.getProperty("url", "socket://localhost:5400"));
	}

	public FastNet(String urlString, boolean local) throws IOException {
		properties = new CommonProperties();
		parseHostString(urlString);
	}

	private void parseHostString(String hostString) {
		String[] strings = hostString.split("://");
		properties.getPropertyMap().put(NetConstants.type, strings[0].replace("url=", ""));
		if (strings.length == 1) {
			return;
		}
		strings = strings[1].split("//?");
		String[] hostPort = strings[0].split(":");
		properties.getPropertyMap().put(NetConstants.host, hostPort[0]);
		if (hostPort.length == 1) {
			return;
		}
		properties.getPropertyMap().put(NetConstants.port, hostPort[1]);
		if (strings.length == 1) {
			return;
		}
		String[] params = strings[1].split("&");
		for (String paramBunch : params) {
			String[] split = paramBunch.split("=");
			properties.getPropertyMap().put(split[0], split[1]);
		}
	}

	public FastClient getClient() {
		return new FastClient(properties);
	}

	public FastServer getServer() {
		return new FastServer(properties);
	}

	public CommonProperties getProperties() {
		return properties;
	}
}
