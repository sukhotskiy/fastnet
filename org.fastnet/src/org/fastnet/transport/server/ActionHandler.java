package org.fastnet.transport.server;


public abstract class ActionHandler {

	public abstract Object invoke(MethodCall call) throws Exception;

	public abstract String getKey();
}
