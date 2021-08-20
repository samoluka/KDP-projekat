package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import shared.MonitorAtomicBroadcastBuffer;
import shared.TextMessage;

public class ClientUpdateStocks implements Worker {

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in,
			ConcurrentHashMap<String, Integer> map, int id, AtomicBoolean needBalancing,
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger balanceNumber,
			AtomicInteger numberOfStocksServers, ConcurrentHashMap<Integer, AtomicBoolean> needUpdate,
			MonitorAtomicBroadcastBuffer<String> buff) throws IOException, Exception {
		while (true) {
			String[] msg = ((TextMessage) in.readObject()).getBody().split(";");
			map.put(msg[0], Integer.parseInt(msg[1]));
			AtomicBoolean n = null;
			for (Entry<Integer, List<String>> pair : stocksOn.entrySet()) {
				if (pair.getValue().contains(msg[0])) {
					n = needUpdate.get(pair.getKey());
				}
			}
			if (n == null) {
				continue;
			}
			synchronized (n) {
				n.getAndSet(true);
				n.notifyAll();
			}
		}
	}

}
