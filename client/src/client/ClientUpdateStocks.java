package client;

import java.net.Socket;

public class ClientUpdateStocks {

	private static int port;
	private static String host;

	public static void main(String[] args) {
		port = Integer.parseInt(args[0]);
		host = args[1];
		try (Socket server = new Socket(host, port);) {
			WorkingThreadUpdate wt = new WorkingThreadUpdate(server, Integer.parseInt(args[2]));
			wt.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
