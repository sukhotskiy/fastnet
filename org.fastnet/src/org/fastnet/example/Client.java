package org.fastnet.example;

import org.fastnet.FastNet;
import org.fastnet.transport.FastClient;
import org.fastnet.transport.server.FastServer;
import org.fastnet.transport.server.ActionHandler;
import org.fastnet.transport.server.MethodCall;

public class Client {

	private static final RemoteTest REMOTE_TEST = new RemoteTest();

	public static void main(String[] args) {
		try {
			FastServer server = new FastNet("url=string://localhost:5400/?timeout=10000&corePoolSize=5&maxPoolSize=7&keepAliveTime=100&backLog=100", true).getServer();
			FastClient clientIm = new FastNet("url=string://localhost:5400", true).getClient();
			server.putInstance(IRemoteTest.class.getName(), REMOTE_TEST);
			server.putHandler(IRemoteTest.class.getName(), new ActionHandler() {

				@Override
				public Object invoke(MethodCall call) throws Exception {
					return REMOTE_TEST.getData((Integer) call.getParams()[0]);
				}

				@Override
				public String getKey() {
					try {
						return new MethodCall(IRemoteTest.class.getName(), IRemoteTest.class.getMethod("getData", new Class[] {
							int.class
						}), null).getKey();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
					return null;
				}
			});
			IRemoteTest remoteTest = clientIm.lookup(IRemoteTest.class, false);
			long nanoTime = System.nanoTime();
			remoteTest.getData(1000);
			for (int a = 0; a < 1000; a++) {
				remoteTest.getData(1000);
			}
			clientIm.close();
			server.setStop();
			System.out.println("-------zzzz------------" + (System.nanoTime() - nanoTime));
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
