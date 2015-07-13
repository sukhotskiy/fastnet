package org.fastnet.server;

import org.fastnet.transport.server.MethodCall;


public abstract class ActionHandler {

	public abstract Object invoke(MethodCall call) throws Exception;

	public abstract String getKey();
}
