package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import shared.MonitorAtomicBroadcastBuffer;

public class Client implements Worker {

	private static int x = 10000;

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in,
			ConcurrentHashMap<String, Integer> map, int id, AtomicBoolean needBalancing,
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger balanceNumber,
			AtomicInteger numberOfStocksServers, ConcurrentHashMap<Integer, AtomicBoolean> needUpdate,
			MonitorAtomicBroadcastBuffer<String> buff) throws IOException, Exception {

		buff.addListener(id);
		out.writeInt(id);
		out.flush();
		SocketBufferWorker sbw = new SocketBufferWorker(buff, client, in, out, id);
		sbw.run();
	}

}
